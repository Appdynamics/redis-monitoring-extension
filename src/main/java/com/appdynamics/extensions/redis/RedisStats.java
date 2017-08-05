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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.util.Map;

public class RedisStats {
    public MonitorConfiguration configuration;
    public Map<String, String> server;
    private Logger logger = LoggerFactory.getLogger(RedisStats.class);
    public JedisPool jedisPool;

    RedisStats(MonitorConfiguration configuration, Map<String, String> server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void gatherMetrics() {
        String host = server.get("host");
        int port = Integer.parseInt(server.get("port"));
        String password = server.get("password");
        JedisPoolConfig jedisPoolConfig = buildJedisPoolConfig();
        if(password.trim().length() != 0){
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 2000, password);
        }
        else{
            jedisPool = new JedisPool(jedisPoolConfig, host, port);
        }
        getMetricsFromInfo(jedisPool);

    }

    public JedisPoolConfig buildJedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(3);
        return jedisPoolConfig;

    }

    void getMetricsFromInfo(JedisPool jedisPool) {
        //slowlog_slower_than = Integer.parseInt(server.get("slowlog-slower-than"));
        Map<String, ?> metricsMap = (Map<String, ?>) configuration.getConfigYml().get("metrics");
        RedisThreadsHandler redisThreadsHandler = new RedisThreadsHandler(metricsMap, jedisPool, configuration, server);
        redisThreadsHandler.parseMap();
        /*metrics.putAll(redisThreadsHandler.metrics);
        return metrics;*/
    }
}
