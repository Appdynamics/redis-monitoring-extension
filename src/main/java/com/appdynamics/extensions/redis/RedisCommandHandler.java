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

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.redis.metrics.RedisMetrics;
import com.appdynamics.extensions.redis.metrics.SlowLogMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RedisCommandHandler {
    private Map<String, ?> metricsMap;
    private MonitorConfiguration configuration;
    public Map<String, String> server;
    private JedisPool jedisPool;
    private Logger logger = LoggerFactory.getLogger(RedisCommandHandler.class);
    private CountDownLatch countDownLatch = new CountDownLatch(2);


    protected RedisCommandHandler(Map<String, ?> metricsMap, JedisPool jedisPool, MonitorConfiguration configuration, Map<String, String> server) {
        this.metricsMap = metricsMap;
        this.jedisPool = jedisPool;
        this.configuration = configuration;
        this.server = server;

    }

    protected void parseMap() {

        SlowLogMetrics slowLogMetricsTask = new SlowLogMetrics(jedisPool, metricsMap, configuration, server, countDownLatch);
        configuration.getExecutorService().execute(slowLogMetricsTask);
        Map<String, ? > infoMetricsConfigMap = (Map<String, ?>)metricsMap.get("Info");
        RedisMetrics redisMetricsTask = new RedisMetrics(jedisPool, infoMetricsConfigMap, configuration, server, countDownLatch);
        configuration.getExecutorService().execute(redisMetricsTask);
        try{
            countDownLatch.await();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        finally {
            jedisPool.destroy();
        }

    }
}
