/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
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

public class InfoMetrics implements Runnable {

    private JedisPool jedisPool;
    private String info;
    private Map<String, ?> metricsMap;
    private Map<String, ?> infoMap;
    private List<Metric> finalMetricList;
    private MonitorContextConfiguration configuration;
    private Map<String, ?> server;
    private MetricWriteHelper metricWriteHelper;
    private static final Logger logger = LoggerFactory.getLogger(InfoMetrics.class);
    private CountDownLatch countDownLatch;

    public InfoMetrics(MonitorContextConfiguration configuration, Map<String, ?> server, MetricWriteHelper metricWriteHelper, JedisPool jedisPool, CountDownLatch countDownLatch) {
        this.configuration = configuration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
        this.jedisPool = jedisPool;
        this.countDownLatch = countDownLatch;
        finalMetricList = Lists.newArrayList();
    }

    public void run() {
        try {
            metricsMap = (Map<String, ?>)configuration.getConfigYml().get("metrics");
            AssertUtils.assertNotNull(metricsMap, "There is no 'metrics' section in config.yml");
            infoMap = (Map<String, ?>)metricsMap.get("Info");
            AssertUtils.assertNotNull(infoMap, "There is no 'Info' metrics section under 'metrics' in config.yml");
            info = extractInfo();
            finalMetricList = extractMetricsList();
            logger.debug("Printing Info metrics for server {}", server.get("name"));
            metricWriteHelper.transformAndPrintMetrics(finalMetricList);
        }
        catch(Exception e){
            logger.error("Error while collecting and printing info metrics", e);
        }
        finally {
            countDownLatch.countDown();
        }
    }

    private String extractInfo(){
        int connectionStatus = 0;
        String infoFromRedis = null;
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            infoFromRedis = jedis.info();
            connectionStatus = 1;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        finally {
            if(jedis != null) {
                jedis.close();
            }
        }
        metricWriteHelper.printMetric(configuration.getMetricPrefix() + "|" + server.get("name") + "|" + "connectionStatus", String.valueOf(connectionStatus), "AVERAGE", "AVERAGE", "INDIVIDUAL");
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
