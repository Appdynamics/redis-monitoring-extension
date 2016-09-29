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

import com.appdynamics.extensions.NumberUtils;
import com.appdynamics.extensions.redis.config.RedisMetrics;
import com.appdynamics.extensions.redis.config.Server;
import com.appdynamics.extensions.util.MetricUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisMonitorTask {
	private static final String COMMA_SEPARATOR = ",";
	private static final String COLON_SEPARATOR = ":";
	private static final String EQUALS_SEPARATOR = "=";
	public static final String METRIC_SEPARATOR = "|";
	private Logger logger = Logger.getLogger(RedisMonitorTask.class);
	private Server server;
    private BigDecimal keyspaceHits;
    private BigDecimal keyspaceMisses;

	public RedisMonitorTask(Server server) {
		this.server = server;
	}

	public RedisMetrics gatherMetricsForAServer() {
		RedisMetrics metricsForAServer = new RedisMetrics();
        Jedis client = null;
		try {
            client = buildJedisClient();

            // INFO stats
            String infoOutput = client.info();
            Map<String, String> infoMetrics = parseOutputString(infoOutput);
            metricsForAServer.getMetrics().putAll(infoMetrics);

            // INFO COMMANDSTATS
            String commandStatsOutput = client.info("commandstats");
            Map<String, String> commandStatMetrics = parseOutputString(commandStatsOutput);
            metricsForAServer.getMetrics().putAll(commandStatMetrics);

            if(keyspaceMisses != null && keyspaceHits != null) {
                String keySpaceHitRatio = computeKeySpaceHitRatio(keyspaceHits, keyspaceMisses);
                if(!Strings.isNullOrEmpty(keySpaceHitRatio)) {
                    metricsForAServer.getMetrics().put(server.getDisplayName() + METRIC_SEPARATOR + "keySpaceHitRatio",
                            keySpaceHitRatio);
                }
            }

			metricsForAServer.getMetrics().put(server.getDisplayName() + METRIC_SEPARATOR + RedisMonitorConstants.METRICS_COLLECTION_STATUS,
					RedisMonitorConstants.SUCCESS_VALUE);
		} catch (Exception e) {
			logger.error("Exception while gathering metrics for " + server.getDisplayName(), e);
			metricsForAServer.getMetrics().put(server.getDisplayName() + METRIC_SEPARATOR + RedisMonitorConstants.METRICS_COLLECTION_STATUS,
					RedisMonitorConstants.ERROR_VALUE);
		} finally {
            closeJedisClient(client);
        }
        return metricsForAServer;
	}

    private String computeKeySpaceHitRatio(BigDecimal keyspaceHits, BigDecimal keyspaceMisses) {
        if(keyspaceHits.add(keyspaceMisses).compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal keySpaceHitRatio = keyspaceHits.divide(keyspaceHits.add(keyspaceMisses),0, RoundingMode.HALF_UP);
            return keySpaceHitRatio.toString();
        }
        return null;
    }

    private Jedis buildJedisClient() {
		Jedis client;
		try {
			client = new Jedis(server.getHost(), server.getPort());
			if (!Strings.isNullOrEmpty(server.getPassword())) {
				client.auth(server.getPassword());
			} else {
				logger.debug("Redis Server not authenticated");
			}
		} catch (JedisConnectionException e) {
			logger.error("Error while connecting to Redis Server: ", e);
			throw e;
		} catch (JedisDataException e) {
			logger.error("Error while authenticating Redis Server: ", e);
			throw e;
		}
		return client;
	}

	private Map<String, String> parseOutputString(String info) {
		Map<String, String> metrics = Maps.newHashMap();
		String categoryName = "";
        Set<String> excludePatterns = server.getExcludePatterns();
        Set<String> includePatterns = server.getIncludePatterns();
		Splitter lineSplitter = Splitter.on(System.getProperty("line.separator")).omitEmptyStrings().trimResults();
        Splitter commaSplitter = Splitter.on(COMMA_SEPARATOR).omitEmptyStrings().trimResults();
		for (String currentLine : lineSplitter.split(info)) {
			if (currentLine.startsWith("#")) { // Every Category of metrics starts with # like #Memory
				categoryName = currentLine.substring(1).trim();
				continue; // No need to go through if the line starts with a #
			}

            String[] kv = currentLine.split(COLON_SEPARATOR);
            if(kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if(value.contains(COMMA_SEPARATOR) || value.contains(EQUALS_SEPARATOR)) {
                    List<String> keyValuePairs = Lists.newArrayList();
                    Iterables.addAll(keyValuePairs, commaSplitter.split(value));
                    for (String keyValue : keyValuePairs) {
                        String [] keyAndValue = keyValue.split(EQUALS_SEPARATOR);
                        String metricPath = getMetricPath(categoryName + METRIC_SEPARATOR + key, keyAndValue[0]);
                        if (isMetricToBeReported(metricPath, includePatterns, excludePatterns)) {
                            if (NumberUtils.isNumber(keyAndValue[1])) {
                                metrics.put(metricPath, MetricUtils.toWholeNumberString(Double.parseDouble(keyAndValue[1])));
                            }
                        }
                    }
                } else {
                    String metricPath = getMetricPath(categoryName, key);
                    if (isMetricToBeReported(metricPath, includePatterns, excludePatterns)) {
                        if (NumberUtils.isNumber(value)) {
                            if("keyspace_hits".equals(key)) {
                                keyspaceHits = new BigDecimal(value);
                            }
                            if("keyspace_misses".equals(key)) {
                                keyspaceMisses = new BigDecimal(value);
                            }
                            metrics.put(metricPath, MetricUtils.toWholeNumberString(Double.parseDouble(value)));
                            continue; // No need to procees further if the value is a Number
                        }
                        setRole(metrics, key, metricPath, value);
                        setMasterLinkStatus(metrics, key, metricPath, value);
                    }
                }
            }
		}
		return metrics;
	}

	private void setMasterLinkStatus(Map<String, String> metrics, String key, String metricPath, String value) {
		if ("master_link_status".equals(key)) {
			String status = "0";
			if (value.equals("up")) {
				status = "1";
			}
			metrics.put(metricPath, status);
		}
	}

	private void setRole(Map<String, String> metrics, String key, String metricPath, String value) {
		if ("role".equals(key)) {
			String roleValue = "0";
			if (value.equals("master")) {
				roleValue = "1";
			}
			metrics.put(metricPath, roleValue);
		}
	}

	private String getMetricPath(String categoryName, String metricName) {
		StringBuilder metricPath = new StringBuilder();
		metricPath.append(Strings.isNullOrEmpty(server.getDisplayName()) ? "" : server.getDisplayName() + METRIC_SEPARATOR);
		metricPath.append(Strings.isNullOrEmpty(categoryName) ? "" : categoryName + METRIC_SEPARATOR);
		metricPath.append(metricName);
		return metricPath.toString();
	}

	public boolean isMetricToBeReported(String metricKey, Set<String> includePatterns, Set<String> excludePatterns) {
        if(includePatterns != null && excludePatterns != null) {
            if(!includePatterns.isEmpty()) {
                for(String includePattern : includePatterns) {
                    if(metricKey.matches(escapeText(includePattern))) {
                        return true;
                    }
                }
                return false;
            } else if (!excludePatterns.isEmpty()) {
                for (String excludePattern : excludePatterns) {
                    if (metricKey.matches(escapeText(excludePattern))) {
                        return false;
                    }
                }
            }
        }
        return true;
	}


	private String escapeText(String excludePattern) {
		return excludePattern.replaceAll("\\|", "\\\\|");
	}

    public void closeJedisClient(Jedis client) {
        try {
            if (client.isConnected()) {
                client.close();
            }
        } catch (Exception e) {
           logger.error("Error while shutting the Jedis connection", e);
        }
    }
}