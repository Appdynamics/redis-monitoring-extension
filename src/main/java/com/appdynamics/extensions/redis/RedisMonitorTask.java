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

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

import static com.appdynamics.extensions.redis.utils.Constants.connectionStatus;

class RedisMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = LoggerFactory.getLogger(RedisMonitorTask.class);
    private MonitorConfiguration configuration;
    private Map<String, String> server;
    private MetricWriteHelper metricWriteHelper;
    private long previousTimeStamp;
    private long currentTimeStamp;
    private JedisPool jedisPool;

    RedisMonitorTask(TasksExecutionServiceProvider serviceProvider, Map<String, String> server, long previousTimeStamp, long currentTimeStamp) {
        this.configuration = serviceProvider.getMonitorConfiguration();
        this.server = server;
        this.metricWriteHelper = serviceProvider.getMetricWriteHelper();
        this.previousTimeStamp = previousTimeStamp;
        this.currentTimeStamp = currentTimeStamp;
    }

    public void run() {
        populateAndPrintMetrics();
    }

    private void populateAndPrintMetrics() {
        String host = server.get("host");
        String port = server.get("port");
        String name = server.get("name");
        if(!Strings.isNullOrEmpty(host) && !Strings.isNullOrEmpty(port) && !Strings.isNullOrEmpty(name)) {
            int portNumber = Integer.parseInt(port);
            String password = getPassword(server);
            JedisPoolConfig jedisPoolConfig = buildJedisPoolConfig();
            if (password.trim().length() != 0) {
                try {
                    jedisPool = new JedisPool(jedisPoolConfig, host, portNumber, 2000, password);
                    connectionStatus = 1;
                }
                catch (Exception e) {
                    logger.error("Exception while creating JedisPool" + e);
                }
            }
            else {
                try {
                    jedisPool = new JedisPool(jedisPoolConfig, host, portNumber);
                    connectionStatus = 1;
                }
                catch (Exception e) {
                    logger.error("Exception while creating JedisPool" + e);
                }
            }
            getMetricsFromInfo(jedisPool);
        }
        else{
            logger.debug("The host, port and name fields of the server : {} need to be specified", server);
        }
    }

    private String getPassword(Map<String, String> server){
        String password = server.get("password");
        String encryptedPassword = server.get("encryptedPassword");
        Map<String, ?> configMap = configuration.getConfigYml();
        String encryptionKey = configMap.get("encryptionKey").toString();
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
        RedisCommandHandler redisCommandHandler = new RedisCommandHandler(configuration, server, metricWriteHelper, jedisPool, previousTimeStamp, currentTimeStamp);
        redisCommandHandler.triggerCommandsToRedisServer();
    }

    @Override
    public void onTaskComplete() {
        metricWriteHelper.printMetric(configuration.getMetricPrefix() + "|" + server.get("name") + "|" + "connectionStatus", String.valueOf(connectionStatus), "AVERAGE", "AVERAGE", "INDIVIDUAL");

    }
}






