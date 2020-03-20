/**
 * Copyright 2017 AppDynamics, Inc.
 *
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
package com.appdynamics.extensions.redis;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.redis.metrics.InfoMetrics;
import com.appdynamics.extensions.redis.metrics.SlowLogMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

class RedisCommandHandler {

    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, ?> server;
    private MetricWriteHelper metricWriteHelper;
    private JedisPool jedisPool;
    private Logger logger = LoggerFactory.getLogger(RedisCommandHandler.class);
    private CountDownLatch countDownLatch;
    private long previousTimeStamp;
    private long currentTimeStamp;

    RedisCommandHandler(MonitorContextConfiguration monitorContextConfiguration, Map<String, ?> server, MetricWriteHelper metricWriteHelper, JedisPool jedisPool, long previousTimeStamp, long currentTimeStamp) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
        this.jedisPool = jedisPool;
        countDownLatch = new CountDownLatch(2);
        this.previousTimeStamp = previousTimeStamp;
        this.currentTimeStamp = currentTimeStamp;
    }

     void triggerCommandsToRedisServer() {
        SlowLogMetrics slowLogMetricsTask = new SlowLogMetrics(monitorContextConfiguration, server, metricWriteHelper, jedisPool, countDownLatch, previousTimeStamp, currentTimeStamp);
        monitorContextConfiguration.getContext().getExecutorService().execute("SlowLogMetricsTask",slowLogMetricsTask);
        InfoMetrics infoMetricsTask = new InfoMetrics(monitorContextConfiguration, server, metricWriteHelper, jedisPool, countDownLatch);
        monitorContextConfiguration.getContext().getExecutorService().execute("InfoMetricsTask", infoMetricsTask);
        try{
            countDownLatch.await();
        }
        catch(InterruptedException e){
            logger.error(e.toString());
        }
        finally {
            jedisPool.destroy();
        }
    }
}
