/**
 * Copyright 2014 AppDynamics, Inc.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.redis.config.ConfigUtil;
import com.appdynamics.extensions.redis.config.Configuration;
import com.appdynamics.extensions.redis.config.RedisMetrics;
import com.appdynamics.extensions.redis.config.Server;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class RedisMonitor extends AManagedMonitor {

	public static final String CONFIG_ARG = "config-file";
	public static final String METRIC_SEPARATOR = "|";
	private static final Logger logger = Logger.getLogger(RedisMonitor.class);

	private final static ConfigUtil<Configuration> configUtil = new ConfigUtil<Configuration>();

	public RedisMonitor() {
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		logger.info(msg);
		System.out.println(msg);
	}

	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
		if (taskArguments != null) {
			logger.info("Starting Redis Monitoring Task");
			if (logger.isDebugEnabled()) {
				logger.debug("Task Arguments Passed ::" + taskArguments);
			}
			String configFilename = getConfigFilename(taskArguments.get(CONFIG_ARG));
			try {
				Configuration config = configUtil.readConfig(configFilename, Configuration.class);
				List<RedisMetrics> metrics = collectMetrics(config);
				printStats(config, metrics);
				logger.info("Redis Monitoring Task completed");
				return new TaskOutput("Redis Monitoring Task completed");
			} catch (FileNotFoundException e) {
				logger.error("Config file not found :: " + configFilename, e);
			} catch (Exception e) {
				logger.error("Metrics collection failed", e);
			}
		}
		throw new TaskExecutionException("Redis monitoring task completed with failures.");
	}

	private List<RedisMetrics> collectMetrics(Configuration config) {
		List<RedisMetrics> metrics = Lists.newArrayList();
		if (config != null && config.getServers() != null) {
			for (Server server : config.getServers()) {
				RedisMonitorTask monitorTask = new RedisMonitorTask(server);
				metrics.add(monitorTask.gatherMetricsForAServer());
			}
		}
		return metrics;
	}

	private void printStats(Configuration config, List<RedisMetrics> metrics) {
		for (RedisMetrics redisMetrics : metrics) {
			StringBuilder metricPath = new StringBuilder();
			metricPath.append(config.getMetricPrefix());
			Map<String, String> metricsForAServer = redisMetrics.getMetrics();
			for (Map.Entry<String, String> entry : metricsForAServer.entrySet()) {
				printAverageAverageIndividual(metricPath.toString() + entry.getKey(), entry.getValue());
			}
		}
	}

	private void printAverageAverageIndividual(String metricPath, String metricValue) {
		printMetric(metricPath, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION, MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
	}

	private void printMetric(String metricPath, String metricValue, String aggregation, String timeRollup, String cluster) {
		MetricWriter metricWriter = super.getMetricWriter(metricPath, aggregation, timeRollup, cluster);
		if (metricValue != null) {
			if(logger.isDebugEnabled()) {
				logger.debug(metricPath + "   " + metricValue);
			}
			metricWriter.printMetric(metricValue);
		}
	}

	/**
	 * Returns a config file name,
	 * 
	 * @param filename
	 * @return String
	 */
	private String getConfigFilename(String filename) {
		if (filename == null) {
			return "";
		}
		// for absolute paths
		if (new File(filename).exists()) {
			return filename;
		}
		// for relative paths
		File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
		String configFileName = "";
		if (!Strings.isNullOrEmpty(filename)) {
			configFileName = jarPath + File.separator + filename;
		}
		return configFileName;
	}

	private static String getImplementationVersion() {
		return RedisMonitor.class.getPackage().getImplementationTitle();
	}
}
