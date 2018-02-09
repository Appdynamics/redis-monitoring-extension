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
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Slowlog;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.appdynamics.extensions.redis.utils.Constants.METRIC_SEPARATOR;

public class SlowLogMetrics implements Runnable {

    private JedisPool jedisPool;
    private Map<String, ?> metricsMap;
    private MonitorConfiguration configuration;
    private Map<String, String> server;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, String> metricPropertiesMap;
    private static final Logger logger = LoggerFactory.getLogger(SlowLogMetrics.class);
    private CountDownLatch countDownLatch;
    private long previousTimeStamp;
    private long currentTimeStamp;
    private List<Map<String, ?>> slowLogMetricsList;
    private List<Metric> finalMetricList;

    public SlowLogMetrics(MonitorConfiguration configuration, Map<String, String> server, MetricWriteHelper metricWriteHelper, JedisPool jedisPool, CountDownLatch countDownLatch, long previousTimeStamp, long currentTimeStamp){
        this.configuration = configuration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
        this.jedisPool = jedisPool;
        this.countDownLatch = countDownLatch;
        this.previousTimeStamp = previousTimeStamp / 1000L;
        this.currentTimeStamp = currentTimeStamp / 1000L;
    }

    public void run() {
        try {
            metricsMap = (Map<String, ?>)configuration.getConfigYml().get("metrics");
            AssertUtils.assertNotNull(metricsMap, "There is no 'metrics' section in config.yml");
            slowLogMetricsList = (List<Map<String, ?>>)metricsMap.get("Slowlog");
            AssertUtils.assertNotNull(slowLogMetricsList, "There is no 'Slowlog' metrics section under 'metrics' in config.yml");
            extractSlowLogPropertiesMap(slowLogMetricsList);
            finalMetricList = extractSlowLogMetricsList();
            logger.debug("Printing SlowLog metrics for server {}", server.get("name"));
            metricWriteHelper.transformAndPrintMetrics(finalMetricList);
        }
        catch(Exception e){

        }
        finally {
            countDownLatch.countDown();
        }
    }

    private void extractSlowLogPropertiesMap(List<Map<String, ?>> slowLogMetricsList) {
        ListIterator<Map<String, ?>> list = slowLogMetricsList.listIterator();
        while(list.hasNext()){
            Map<String, ?> metricEntries = list.next();
            for(Map.Entry<String, ?> metricEntry : metricEntries.entrySet()) {
                if(metricEntry.getKey().equalsIgnoreCase("no_of_new_slow_logs")){
                    metricPropertiesMap = (Map<String, String>) metricEntry.getValue();
                    return;
                }
            }
        }
    }

    private List<Metric> extractSlowLogMetricsList() {
        List<Metric> finalMetricList = Lists.newArrayList();
        int slowLogCount;
        List<Slowlog> slowlogs;
        try(Jedis jedis = jedisPool.getResource()) {
            slowlogs = jedis.slowlogGet(jedis.slowlogLen());
        }
        slowLogCount = countNumberOfNewSlowLogs(slowlogs);
        String metricName = "no_of_new_slow_logs";
        String metricValue = String.valueOf(slowLogCount);
        String metricPath = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + METRIC_SEPARATOR + "SlowLog" + METRIC_SEPARATOR + "no_of_new_slow_logs";
        Metric metric;
        if(metricPropertiesMap != null) {
            metric = new Metric(metricName, metricValue, metricPath, metricPropertiesMap);
        }
        else{
            metric = new Metric(metricName, metricValue, metricPath);
        }
        finalMetricList.add(metric);
        return finalMetricList;
    }

    private int countNumberOfNewSlowLogs(List<Slowlog> slowlogs) {
        int count = 0;
        if(previousTimeStamp != currentTimeStamp) {
            for (Slowlog individualLog : slowlogs) {
                Long tempTimeStamp = individualLog.getTimeStamp();
                if (tempTimeStamp > previousTimeStamp && tempTimeStamp <= currentTimeStamp) {
                    count++;
                }
            }
        }
        logger.debug("The number of new slow logs between {} and {} are : {}", previousTimeStamp, currentTimeStamp, count);
        return count;
    }
}
