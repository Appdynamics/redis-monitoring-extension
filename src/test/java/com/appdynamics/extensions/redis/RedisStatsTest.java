package com.appdynamics.extensions.redis;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 8/9/17.
 */
public class RedisStatsTest {
    private MonitorConfiguration monitorConfiguration = mock(MonitorConfiguration.class);
    private Map<String, String> server = Maps.newHashMap();
    private JedisPool jedisPool = mock(JedisPool.class);
    RedisStats redisStats;

    @Before
    public void init(){
        server.put("password", "hello");
        Map<String, String> configMap = Maps.newHashMap();
        configMap.put("encryptionKey", "");
        when(monitorConfiguration.getConfigYml()).thenReturn(configMap);
        redisStats = new RedisStats(monitorConfiguration,server);
    }

    @Test
    public void getPasswordTest(){
        //when(monitorConfiguration.getConfigYml().get("encryptionKey").toString()).thenReturn("");

        Assert.assertTrue(redisStats.getPassword(server).equals("hello"));
    }

}
