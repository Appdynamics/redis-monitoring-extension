package com.singularity.ee.agent.systemagent.monitors;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: tradel
 * Date: 10/11/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */

// TODO: add host/port/timeout config
// TODO: add keyspace stats
// TODO: number of gets/puts?
// TODO: number of keys?


public class RedisMonitor extends AManagedMonitor {

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private boolean isFirstConfig = true;
    private ArrayList<String> keyspaces = new ArrayList<String>();
    private HashMap<String, String> currentMap = new HashMap<String, String>();
    private HashMap<String, String> lastMap = new HashMap<String, String>();
    private final Pattern keyspacePattern = Pattern.compile("^keys=(\\d+),expires=(\\d+)$");
    private static final Logger logger = Logger.getLogger(RedisMonitor.class);

    public long getDelta(String key) {
        try {
            long currentVal = Long.parseLong(currentMap.get(key));
            long lastVal = Long.parseLong(lastMap.get(key));
            return currentVal - lastVal;
        } catch (NumberFormatException e) {
          //  e.printStackTrace();
            return 0;
        }
    }

    public String getDeltaString(String key) {
        return Long.toString(getDelta(key));
    }

    @Override
    public MetricWriter getMetricWriter(String metricName, String metricAggregationType, String timeRollupType, String clusterRollupType) throws IllegalArgumentException {
        return super.getMetricWriter("Custom Metrics|Redis|" + host + "|" + metricName, metricAggregationType, timeRollupType, clusterRollupType);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void getTaskParams(Map<String, String> taskParams) {
        if (taskParams == null) {
            return;
        }
        if (taskParams.containsKey("host")) {
            host = taskParams.get("host");
        }
        if (taskParams.containsKey("port")) {
            port = Integer.parseInt(taskParams.get("port"));
        }
        if (taskParams.containsKey("password")) {
            password = taskParams.get("password");
        }
        if (taskParams.containsKey("keyspaces")) {
            keyspaces.clear();
            for (String space : taskParams.get("keyspaces").split(",")) {
                keyspaces.add(space.trim());
            }
        }

        if (isFirstConfig) {
            logger.info("Host:      " + host);
            logger.info("Port:      " + Integer.toString(port));
            logger.info("Keyspaces: " + StringUtils.join(keyspaces, ", "));
            isFirstConfig = false;
        }
    }

    @Override
    public TaskOutput execute(Map<String, String> taskParams, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        getTaskParams(taskParams);

        Jedis jedis = new Jedis(host, port);
        if (password != null && ! password.isEmpty()) {
            jedis.auth(password);
        }

        currentMap.clear();
        for (String info : jedis.info().split("\r\n")) {
            if (! info.startsWith("#") && ! info.isEmpty()) {
                String[] kv = info.split(":");
                currentMap.put(kv[0], kv[1]);
            }
        }

        logger.info("gathering stats from Redis server");

        getMetricWriter("used_memory", "OBSERVATION", "CURRENT", "INDIVIDUAL").printMetric(currentMap.get("used_memory"));
        getMetricWriter("connections_received", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("total_connections_received"));
        getMetricWriter("commands_processed", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("total_commands_processed"));
        getMetricWriter("rejected_connections", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("rejected_connections"));
        getMetricWriter("expired_keys", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("expired_keys"));
        getMetricWriter("evicted_keys", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("evicted_keys"));
        getMetricWriter("keyspace_hits", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("keyspace_hits"));
        getMetricWriter("keyspace_misses", "SUM", "SUM", "COLLECTIVE").printMetric(getDeltaString("keyspace_misses"));
        getMetricWriter("connected_slaves", "OBSERVATION", "CURRENT", "COLLECTIVE").printMetric(currentMap.get("connected_slaves"));
        getMetricWriter("connected_clients", "OBSERVATION", "CURRENT", "COLLECTIVE").printMetric(currentMap.get("connected_clients"));

        for (String keyspace : keyspaces) {
            logger.info("gathering stats for keyspace " + keyspace);
            if (currentMap.containsKey(keyspace)) {
                Matcher m = keyspacePattern.matcher(currentMap.get(keyspace));
                if (m.matches()) {
                    int keyCurrent = Integer.parseInt(m.group(1));
                    int expireCurrent = Integer.parseInt(m.group(2));
                    if (lastMap.containsKey(keyspace)) {
                        m = keyspacePattern.matcher(lastMap.get(keyspace));
                        if (m.matches()) {
                            int keyLast = Integer.parseInt(m.group(1));
                            int expireLast = Integer.parseInt(m.group(2));
                            int keyDelta = keyCurrent - keyLast;
                            int expireDelta = expireCurrent - expireLast;
                            getMetricWriter(keyspace + "|keys", "SUM", "SUM", "COLLECTIVE").printMetric(Integer.toString(keyDelta));
                            getMetricWriter(keyspace + "|expired", "SUM", "SUM", "COLLECTIVE").printMetric(Integer.toString(expireDelta));
                        }
                    }
                }
            }

        }

        lastMap = new HashMap<String, String>(currentMap);
        return new TaskOutput("Success");
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put("host", "localhost");
        taskParams.put("keyspaces", "db0,db1");

        RedisMonitor m = new RedisMonitor();
        try {
            m.execute(taskParams, null);
            Thread.sleep(5000);
            m.execute(taskParams, null);
        } catch (TaskExecutionException e) {
            e.printStackTrace();
        }
    }
}
