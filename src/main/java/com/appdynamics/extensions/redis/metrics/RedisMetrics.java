package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import static com.appdynamics.extensions.redis.utils.Constants.METRIC_SEPARATOR;

public class RedisMetrics implements Runnable {

    private JedisPool jedisPool;
    private String info;
    private Map<String, ?> metricsMap;
    private Map<String, ?> infoMap;
    private List<Metric> finalMetricList;
    private MonitorConfiguration configuration;
    private Map<String, String> server;
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);
    private CountDownLatch countDownLatch;

    public RedisMetrics(MonitorConfiguration configuration, Map<String, String> server, JedisPool jedisPool, CountDownLatch countDownLatch) {
        this.configuration = configuration;
        this.server = server;
        this.jedisPool = jedisPool;
        this.countDownLatch = countDownLatch;
        finalMetricList = Lists.newArrayList();
        metricsMap = (Map<String, ?>)configuration.getConfigYml().get("metrics");
        AssertUtils.assertNotNull(metricsMap, "There is no 'metrics' section in config.yml");
        infoMap = (Map<String, ?>)metricsMap.get("Info");
        AssertUtils.assertNotNull(infoMap, "There is no 'Info' metrics section under 'metrics' in config.yml");
    }

    public void run() {
        info = extractInfo();
        finalMetricList = extractMetricsList();
        logger.debug("Printing Info metrics for server {}", server.get("name"));
        configuration.getMetricWriter().transformAndPrintNodeLevelMetrics(finalMetricList);
        countDownLatch.countDown();
    }

    private String extractInfo(){
        String infoFromRedis;
        try(Jedis jedis = jedisPool.getResource()){
            infoFromRedis = jedis.info();
        }
        return infoFromRedis;
    }

    private List<Metric> extractMetricsList(){
        List<Metric> finalMetricList = Lists.newArrayList();
        InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
        String metricPrefix = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name");
        for(Map.Entry entry : infoMap.entrySet()) {
            String sectionName = entry.getKey().toString();
            List<Map<String, ?>> metricsInSectionConfig = (List<Map<String,?>>) entry.getValue();
            Map<String, String> sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, sectionName);
            CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSectionConfig, sectionInfoMap, metricPrefix, sectionName);
            finalMetricList.addAll(commonMetricsModifier.metricBuilder());
        }
        return finalMetricList;
    }
}
