package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.redis.metrics.sectionMetrics.CommonMetricsModifier;
import com.appdynamics.extensions.redis.utils.Calculators;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.appdynamics.extensions.util.*;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import static com.appdynamics.extensions.redis.utils.Constants.*;

public class RedisMetrics implements Runnable {
    private JedisPool jedisPool;
    private String info;
    private Map<String, ?> metricsMap;
    private Map<String, MetricProperties> finalMetricMap;
    private MonitorConfiguration configuration;
    public Map<String, String> server;
    private final ClusterMetricProcessor clusterMetricProcessor = new ClusterMetricProcessor();
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);
    private CountDownLatch countDownLatch;

    public RedisMetrics(JedisPool jedisPool, Map<String,?> metricsMap, MonitorConfiguration configuration, Map<String, String> server, CountDownLatch countDownLatch) {
        this.jedisPool = jedisPool;
        this.metricsMap = metricsMap;
        this.configuration = configuration;
        this.server = server;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        finalMetricMap = Maps.newHashMap();
        info = extractInfo();
        countDownLatch.countDown();
        sectionMapExtractor();
        printNodeLevelMetrics(finalMetricMap);
        printClusterLevelMetrics(finalMetricMap);

    }

    public String extractInfo(){
        String infoFromRedis;
        try(Jedis jedis = jedisPool.getResource()){
            infoFromRedis = jedis.info();
        }
        return infoFromRedis;
    }

    private Map<String, MetricProperties> sectionMapExtractor(){
        InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
        for(Map.Entry entry : metricsMap.entrySet()) {
            String sectionName = entry.getKey().toString();
            List<Map<String, ?>> metricsInSectionConfig = (List<Map<String,?>>) entry.getValue();
            Map<String, String> sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, sectionName);
            CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSectionConfig, sectionInfoMap, sectionName);
            finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
        }

        return finalMetricMap;
    }

    private void printNodeLevelMetrics(Map<String, MetricProperties> metrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        for (Map.Entry<String, MetricProperties> metric : metrics.entrySet()) {
            String metricName = metric.getKey();
            MetricProperties currentMetricProperties = metric.getValue();
            if(metricName.equalsIgnoreCase("keyspace_hit_ratio")){
                keyspaceHitRatioCalculator(currentMetricProperties, metrics);
            }
            String metricPath = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + METRIC_SEPARATOR + currentMetricProperties.getSectionName() + METRIC_SEPARATOR + currentMetricProperties.getAlias();
            BigDecimal metricValue = currentMetricProperties.getInfoValue();
            if (metricValue != null) {
                Calculators calculators = new Calculators();
                metricValue = calculators.deltaCalculator(currentMetricProperties, metricPath, metricValue);
                metricValue = calculators.multiplier(currentMetricProperties, metricValue);
                currentMetricProperties.setModifiedFinalValue(metricValue);
                String aggregationType = currentMetricProperties.getAggregation();
                String timeRollupType = currentMetricProperties.getTime();
                String clusterRollupType = currentMetricProperties.getCluster();
                if (metricValue != null) {
                    metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
                }
            }
        }
    }


    private void keyspaceHitRatioCalculator(MetricProperties currentMetricProperties, Map<String, MetricProperties> metrics){
        MetricProperties keyspace_hit = metrics.get("keyspace_hits");
        MetricProperties keyspace_misses = metrics.get("keyspace_misses");
        if(keyspace_hit != null && keyspace_misses != null){
            BigDecimal sum = keyspace_hit.getInfoValue().add(keyspace_misses.getInfoValue());
            if(sum.compareTo(BigDecimal.ZERO) != 0){
                BigDecimal ratio = (keyspace_hit.getInfoValue()).divide(sum);

                currentMetricProperties.setInfoValue(ratio.toString());
            }

        }
    }

    private void printClusterLevelMetrics(Map<String, MetricProperties> metrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        AggregatorFactory aggregatorFactory = new AggregatorFactory();
        clusterMetricProcessor.collect(aggregatorFactory, metrics);
        Collection<Aggregator<AggregatorKey>> aggregators = aggregatorFactory.getAggregators();
        for(Aggregator<AggregatorKey> aggregator: aggregators) {
            Set<AggregatorKey> keys = aggregator.keys();
            for(AggregatorKey key : keys) {
                BigDecimal value = aggregator.getAggregatedValue(key);
                String path = configuration.getMetricPrefix() + METRIC_SEPARATOR + "Cluster" + METRIC_SEPARATOR + key.getMetricPath();
                String splits[] = key.getMetricType().split("\\.");
                if(splits.length == 3) {
                    metricWriter.printMetric(path, String.valueOf(value), splits[0], splits[1], splits[2].equals("IND") ? MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL : MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
                }
            }
        }
    }
}
