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
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class RedisStats {
    private MonitorConfiguration configuration;
    public Map<String, String> server;
    private Logger logger = LoggerFactory.getLogger(RedisStats.class);
    private JedisPool jedisPool;

    RedisStats(MonitorConfiguration configuration, Map<String, String> server) {
        this.configuration = configuration;
        this.server = server;
    }

    protected void gatherMetrics() {
        String host = server.get("host");
        int port = Integer.parseInt(server.get("port"));
        String password = getPassword(server);
        JedisPoolConfig jedisPoolConfig = buildJedisPoolConfig();
        if(password.trim().length() != 0){
            try {
                jedisPool = new JedisPool(jedisPoolConfig, host, port, 2000, password);
            }
            catch(Exception e){
                logger.info("Exception while creating JedisPool" + e);
            }
        }
        else{
            try {
                jedisPool = new JedisPool(jedisPoolConfig, host, port);
            }
            catch(Exception e){
                logger.info("Exception while creating JedisPool" + e);
            }
        }
        getMetricsFromInfo(jedisPool);

    }

    public String getPassword(Map<String, String> server){
        String password = server.get("password");
        String encryptedPassword = server.get("encryptedPassword");
        Map<String, ?> configMap = configuration.getConfigYml();

        String encryptionKey =    configMap.get("encryptionKey").toString();
        if(!Strings.isNullOrEmpty(password)){
            return password;
        }
        if(!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)){
            Map<String,String> cryptoMap = Maps.newHashMap();
            cryptoMap.put("password-encrypted", encryptedPassword);
            cryptoMap.put("encryption-key", encryptionKey);
            logger.debug("Decrypting the ecncrypted password........");
            return CryptoUtil.getPassword(cryptoMap);
        }
        return "";

    }

    private JedisPoolConfig buildJedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(3);
        return jedisPoolConfig;

    }

    private void getMetricsFromInfo(JedisPool jedisPool) {
        Map<String, ?> metricsMap = (Map<String, ?>) configuration.getConfigYml().get("metrics");
        RedisCommandHandler redisCommandHandler = new RedisCommandHandler(metricsMap, jedisPool, configuration, server);
        //configuration.getMetricWriter().on
        redisCommandHandler.parseMap();
    }
}
