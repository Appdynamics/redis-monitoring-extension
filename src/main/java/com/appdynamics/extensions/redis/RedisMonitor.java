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

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.redis.utils.Constants.DEFAULT_METRIC_PREFIX;

public class RedisMonitor extends ABaseMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RedisMonitor.class);
    private static long previousTimeStamp = System.currentTimeMillis();
    private static long currentTimeStamp = System.currentTimeMillis();

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return "Redis Monitor";
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider serviceProvider) {
        List<Map<String,String>> servers = (List<Map<String,String>>)configuration.getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        for (Map<String, String> server : servers) {
            RedisMonitorTask task = new RedisMonitorTask(serviceProvider, server, previousTimeStamp, currentTimeStamp);
            serviceProvider.submit(server.get("name"),task);
        }
    }

    @Override
    protected int getTaskCount() {
        List<Map<String,String>> servers = (List<Map<String,String>>)configuration.getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }


}
