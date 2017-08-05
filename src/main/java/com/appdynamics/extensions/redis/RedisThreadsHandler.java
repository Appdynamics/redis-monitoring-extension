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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class RedisThreadsHandler {
    public Map<String, ?> metricsMap;
    //public int slowlog_log_slower_than;
    public MonitorConfiguration configuration;
    public Map<String, String> server;
    public JedisPool jedisPool;
    private Logger logger = LoggerFactory.getLogger(RedisThreadsHandler.class);

    public RedisThreadsHandler(Map<String, ?> metricsMap, JedisPool jedisPool, MonitorConfiguration configuration, Map<String, String> server) {
        this.metricsMap = metricsMap;
        //this.slowlog_log_slower_than = slowlog_log_slower_than;
        this.jedisPool = jedisPool;
        this.configuration = configuration;
        this.server = server;

    }

    public void parseMap() {
        SlowLogMetrics slowLogMetricsTask = new SlowLogMetrics(jedisPool, metricsMap, configuration, server);
        configuration.getExecutorService().execute(slowLogMetricsTask);
        RedisMetrics redisMetricsTask = new RedisMetrics(jedisPool, metricsMap, configuration, server);
        configuration.getExecutorService().execute(redisMetricsTask);
        //logger.info("========================================Pool closed!!!!!!!!!!!!!!!==========================================");
        //jedisPool.close();

    }
}
