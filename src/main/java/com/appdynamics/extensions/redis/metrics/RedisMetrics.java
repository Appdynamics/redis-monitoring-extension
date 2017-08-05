package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.redis.metrics.sectionMetrics.CommonMetricsModifier;
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
import static com.appdynamics.extensions.redis.utils.Constants.*;

public class RedisMetrics implements Runnable {
    public JedisPool jedisPool;
    public String info;
    public Map<String, ?> metricsMap;
    public Map<String, MetricProperties> finalMetricMap;
    public MonitorConfiguration configuration;
    public Map<String, String> server;
    public static DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);
    private final ClusterMetricProcessor clusterMetricProcessor = new ClusterMetricProcessor();
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);

    public RedisMetrics(JedisPool jedisPool, Map<String,?> metricsMap, MonitorConfiguration configuration, Map<String, String> server) {
        this.jedisPool = jedisPool;
        this.metricsMap = metricsMap;
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        finalMetricMap = Maps.newHashMap();
        info = extractInfo();
        sectionMapExtractor();
        printNodeLevelMetrics(finalMetricMap);
        if(server.get("isCluster") != null && server.get("isCluster").equalsIgnoreCase("true")){

            printClusterLevelMetrics(finalMetricMap);
        }
    }

    public String extractInfo(){
        String infoFromRedis;
        try(Jedis jedis = jedisPool.getResource()){
            infoFromRedis = jedis.info();
        }
        return infoFromRedis;
    }

    public Map<String, MetricProperties> sectionMapExtractor(){
        InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
        for(Map.Entry entry : metricsMap.entrySet()) {
            String sectionName = entry.getKey().toString();
            List<Map<String, ?>> metricsInSection = (List<Map<String,?>>) entry.getValue();
            if(sectionName.equalsIgnoreCase("Clients")) {
                Map<String, String> clientsInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"Clients");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, clientsInfoMap, "Clients");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());

            }
            else if(sectionName.equalsIgnoreCase("Memory")) {
                Map<String, String> memoryInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"Memory");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, memoryInfoMap, "Memory");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
            }
            else if(sectionName.equalsIgnoreCase("Persistence")) {
                Map<String, String> persistenceInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"Persistence");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, persistenceInfoMap, "Persistence");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
            }
            else if(sectionName.equalsIgnoreCase("Stats")) {
                Map<String, String> statsInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"Stats");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, statsInfoMap, "Stats");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
            }
            else if(sectionName.equalsIgnoreCase("Replication")) {
                Map<String, String> replicationInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"Replication");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, replicationInfoMap, "Replication");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
            }
            else if(sectionName.equalsIgnoreCase("CPU")) {
                Map<String, String> cpuInfoMap = infoMapExtractor.extractInfoAsHashMap(info,"CPU");
                CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSection, cpuInfoMap, "CPU");
                finalMetricMap.putAll(commonMetricsModifier.metricBuilder());
            }
        }
        return finalMetricMap;
    }

    public void printNodeLevelMetrics(Map<String, MetricProperties> metrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        for (Map.Entry<String, MetricProperties> metric : metrics.entrySet()) {
            String metricName = metric.getKey();
            MetricProperties currentMetricProperties = metric.getValue();

            if(metricName.equalsIgnoreCase("keyspace_hit_ratio")){
                MetricProperties keyspace_hit = metrics.get("keyspace_hits");
                MetricProperties keyspace_misses = metrics.get("keyspace_misses");
                if(keyspace_hit != null && keyspace_misses != null){
                    BigDecimal sum = keyspace_hit.getInfoValue().add(keyspace_misses.getInfoValue());
                    if(sum.compareTo(BigDecimal.ZERO) != 0){
                        BigDecimal ratio = (keyspace_hit.getInfoValue()).divide(sum);

                        currentMetricProperties.setValue(ratio.toString());
                    }

                }
            }
            String metricPath = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + METRIC_SEPARATOR + currentMetricProperties.getSectionName() + METRIC_SEPARATOR + currentMetricProperties.getAlias();
            BigDecimal metricValue = currentMetricProperties.getInfoValue();
            if (currentMetricProperties.getDelta().equalsIgnoreCase("true")) {
                metricValue = deltaCalculator.calculateDelta(metricPath, metricValue);
            }
            String multiplier = currentMetricProperties.getMultiplier();
            if(multiplier != null) {
                BigDecimal multiplierBigD = (multiplier.trim().length() == 0) ? BigDecimal.ONE : new BigDecimal(multiplier.trim());
                metricValue = metricValue.multiply(multiplierBigD);
            }
            currentMetricProperties.setModifiedFinalValue(metricValue);
            String aggregationType = currentMetricProperties.getAggregation();
            String timeRollupType = currentMetricProperties.getTime();
            String clusterRollupType = currentMetricProperties.getCluster();

            if(metricValue != null) {
                metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
            }
        }
    }

    public void printClusterLevelMetrics(Map<String, MetricProperties> metrics) {
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
