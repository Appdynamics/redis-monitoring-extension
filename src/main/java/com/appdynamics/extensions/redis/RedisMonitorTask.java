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
import java.util.Map;

public class RedisMonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RedisMonitorTask.class);
    private MonitorConfiguration configuration;
    private Map<String, String> server;

    protected RedisMonitorTask(MonitorConfiguration configuration, Map<String, String> server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        populateAndPrintMetrics();
    }

    private void populateAndPrintMetrics() {

        RedisStats redisStatistics = new RedisStats(configuration, server);
        redisStatistics.gatherMetrics();
        logger.info("Successfully printed the metrics for Redis server : " + server.get("name"));

    }
}






