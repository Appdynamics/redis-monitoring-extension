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
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import static com.appdynamics.extensions.redis.utils.Constants.DEFAULT_METRIC_PREFIX;


public class RedisMonitor extends AManagedMonitor {
    private static final Logger logger = LoggerFactory.getLogger(RedisMonitor.class);
    public MonitorConfiguration configuration;

    public RedisMonitor(){
        logger.info("Using Redis Monitor Version [" + getImplementationVersion() + "]");
    }

    public TaskOutput execute(Map<String, String> var1, TaskExecutionContext var2) throws TaskExecutionException{
        logger.debug("The raw arguments are {}" + var1);
        initialize(var1);
        configuration.executeTask();
        return new TaskOutput("Redis monitor run completed successfully.");
    }

    protected void initialize(Map<String, String> var1) {
        if(configuration == null){
            MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration(DEFAULT_METRIC_PREFIX, new TaskRunner(), metricWriteHelper);
            conf.setConfigYml(var1.get("config-file"));
            this.configuration = conf;
        }
    }

    private class TaskRunner implements Runnable {
        public void run(){
            List<Map<String,String>> servers = (List<Map<String, String>>) configuration.getConfigYml().get("servers");
            for(Map<String, String> server : servers) {
                RedisMonitorTask task = new RedisMonitorTask(configuration, server);
                configuration.getExecutorService().execute(task);
            }
        }
    }

    private static String getImplementationVersion() {
        return RedisMonitor.class.getPackage().getImplementationVersion();
    }
}
