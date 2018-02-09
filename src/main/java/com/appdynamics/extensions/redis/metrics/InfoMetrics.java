/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
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

public class InfoMetrics implements Runnable {

    private JedisPool jedisPool;
    private String info;
    private Map<String, ?> metricsMap;
    private Map<String, ?> infoMap;
    private List<Metric> finalMetricList;
    private MonitorConfiguration configuration;
    private Map<String, String> server;
    private MetricWriteHelper metricWriteHelper;
    private static final Logger logger = LoggerFactory.getLogger(InfoMetrics.class);
    private CountDownLatch countDownLatch;

    public InfoMetrics(MonitorConfiguration configuration, Map<String, String> server, MetricWriteHelper metricWriteHelper, JedisPool jedisPool, CountDownLatch countDownLatch) {
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
            logger.error(e.getMessage());
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
        Map<String, String> sectionInfoMap;
        String metricPrefix = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name");
        for(Map.Entry entry : infoMap.entrySet()) {
            String sectionName = entry.getKey().toString();
            if(sectionName.equalsIgnoreCase("commandstats")){
                String infoCommandStats = getCommandStatsInfo();
                sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(infoCommandStats, sectionName);
            }else {
                 sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, sectionName);
            }

            List<Map<String, ?>> metricsInSectionConfig = (List<Map<String,?>>) entry.getValue();
            CommonMetricsModifier commonMetricsModifier = new CommonMetricsModifier(metricsInSectionConfig, sectionInfoMap, metricPrefix, sectionName);
            finalMetricList.addAll(commonMetricsModifier.metricBuilder());
        }
        return finalMetricList;
    }

    private String getCommandStatsInfo(){
        String info;
        try(Jedis jedis = jedisPool.getResource()) {
            info = jedis.info("commandstats");
        }
        return info;
    }
}
