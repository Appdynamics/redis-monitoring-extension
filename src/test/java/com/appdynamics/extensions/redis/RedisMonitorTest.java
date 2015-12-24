package com.appdynamics.extensions.redis;

import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by balakrishnav on 20/10/15.
 */
public class RedisMonitorTest {

    @Test
    public void testRedisMonitor() throws TaskExecutionException {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "src/test/resources/conf/config.yml");

        RedisMonitor monitor = new RedisMonitor();
        monitor.execute(taskArgs, null);
    }
}
