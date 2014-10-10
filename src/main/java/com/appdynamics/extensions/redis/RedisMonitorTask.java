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

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import com.appdynamics.extensions.NumberUtils;
import com.appdynamics.extensions.redis.config.RedisMetrics;
import com.appdynamics.extensions.redis.config.Server;
import com.appdynamics.extensions.util.MetricUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class RedisMonitorTask {
	public static final String METRIC_SEPARATOR = "|";
	private Logger logger = Logger.getLogger("com.singularity.extensions.RedisMonitorTask");
	private Server server;
	private final Pattern keyspacePattern = Pattern.compile("^keys=(\\d+),expires=(\\d+),avg_ttl=(\\d+)");

	public RedisMonitorTask(Server server) {
		this.server = server;
	}

	public RedisMetrics gatherMetricsForAServer() {
		RedisMetrics metricsForAServer = new RedisMetrics();
		try {
			String info = connectAndGetInfoResponse();
			metricsForAServer = parseInfoString(info);
			metricsForAServer.getMetrics().put(server.getDisplayName() + METRIC_SEPARATOR + RedisMonitorConstants.METRICS_COLLECTION_STATUS, RedisMonitorConstants.SUCCESS_VALUE);
		} catch (Exception e) {
			logger.error(e);
			metricsForAServer.getMetrics().put(server.getDisplayName() + METRIC_SEPARATOR + RedisMonitorConstants.METRICS_COLLECTION_STATUS, RedisMonitorConstants.ERROR_VALUE);
		}
		return metricsForAServer;
	}

	private String connectAndGetInfoResponse() {
		Jedis client = null;
		String info = "";
		try {
			client = new Jedis(server.getHost(), server.getPort());
			if (!Strings.isNullOrEmpty(server.getPassword())) {
				client.auth(server.getPassword());
			} else {
				logger.debug("Redis Server not authenticated");
			}
			info = client.info();
		} catch (JedisConnectionException e) {
			logger.error("Error while connecting to Redis Server: ", e);
			throw new RuntimeException();
		} catch (JedisDataException e) {
			logger.error("Error while authenticating Redis Server: ", e);
			throw new RuntimeException();
		} catch (Exception e) {
			logger.error("Exception: ", e);
			throw new RuntimeException();
		} finally {
			try {
				if (client.isConnected()) {
					client.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
		return info;
	}

	private RedisMetrics parseInfoString(String info) {
		RedisMetrics metricsForAServer = new RedisMetrics();
		Map<String, String> metrics = Maps.newHashMap();
		String categoryName = "";
		Set<String> excludePatterns = server.getExcludePatterns();
		for (String line : info.split("\r\n")) {
			if (line.startsWith("#")) {             // Every Category of metrics starts with # like #Memory
				categoryName = line.substring(1).trim();
				continue; // No need to go through if the line starts with a #
			}
			if (line.length() != 0) {
				String[] kv = line.split(":");
				if (kv.length == 2) { 
					String key = kv[0].trim();
					String metricPath = getMetricPath(categoryName, key);
					if (!isKeyExcluded(metricPath, excludePatterns)) { // 
						String value = kv[1].trim();
						if (NumberUtils.isNumber(value)) {
							metrics.put(metricPath, MetricUtils.toWholeNumberString(Double.parseDouble(value)));
							continue; // No need to procees further if the value is a Number
						}
						setRole(metrics, key, metricPath, value);
						setMasterLinkStatus(metrics, key, metricPath, value);
						setKeySpaceMetrics(metrics, metricPath, value);
					}
				}
			}
		}
		metricsForAServer.setMetrics(metrics);
		return metricsForAServer;
	}

	private void setMasterLinkStatus(Map<String, String> metrics, String key, String metricPath, String value) {
		if(key.equals("master_link_status")) {
			String status = "0";
			if(value.equals("up")) {
				status = "1";
			}
			metrics.put(metricPath, status);
		}
	}

	private void setRole(Map<String, String> metrics, String key, String metricPath, String value) {
		if("role".equals(key)) {
			String roleValue = "0";
			if(value.equals("master")) {
				roleValue = "1";
			}
			metrics.put(metricPath, roleValue);
		}
	}

	private void setKeySpaceMetrics(Map<String, String> metrics, String metricPath, String value) {
		for (String keyspace : server.getKeyspaces()) {
			logger.debug("gathering stats for keyspace " + keyspace);
			Matcher m = keyspacePattern.matcher(value);
			if (m.matches()) {
				String keysValue = m.group(1);
				String expiresValue = m.group(2);
				String keySpaceMetricPath = metricPath + METRIC_SEPARATOR;
				metrics.put(keySpaceMetricPath + "keys", keysValue);
				metrics.put(keySpaceMetricPath + "expires", expiresValue);
			}
		}
	}

	private String getMetricPath(String categoryName, String metricName) {
		StringBuilder metricPath = new StringBuilder();
		metricPath.append(Strings.isNullOrEmpty(server.getDisplayName()) ? "" : server.getDisplayName() + METRIC_SEPARATOR);
		metricPath.append(Strings.isNullOrEmpty(categoryName) ? "" : categoryName + METRIC_SEPARATOR);
		metricPath.append(metricName);
		return metricPath.toString();
	}

	private boolean isKeyExcluded(String metricKey, Set<String> excludePatterns) {
		for (String excludePattern : excludePatterns) {
			if (metricKey.matches(escapeText(excludePattern))) {
				return true;
			}
		}
		return false;
	}

	private String escapeText(String excludePattern) {
		return excludePattern.replaceAll("\\|", "\\\\|");
	}
}
