/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Slowlog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

public class SlowLogMetricsTest {


    @Test
    public void sampleTest() {
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(3);
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379, 2000, null, false, null, null, null);
            Jedis jedis = jedisPool.getResource();
            String info = jedis.info();
            System.out.println(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void slowLogMetricsTest() {

        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        AMonitorJob aMonitorJob = mock(AMonitorJob.class);
        MonitorContextConfiguration monitorContextConfiguration = mock(MonitorContextConfiguration.class);
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        when(monitorContextConfiguration.getMetricPrefix()).thenReturn("Server|Component:AppLevels|Custom Metrics|Redis");
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml"));
        doReturn(rootElem).when(monitorContextConfiguration).getConfigYml();
        MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
        JedisPool jedisPool = mock(JedisPool.class);
        Jedis jedis = mock(Jedis.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        when(jedisPool.getResource()).thenReturn(jedis);
        List<Object> objectList = Lists.newArrayList();
        List list1 = new ArrayList();
        list1.add(15L);
        list1.add(1505158204L);
        list1.add(18L);
        List list11 = new ArrayList();
        list11.add("slowlog".getBytes());
        list11.add("get".getBytes());
        list1.add(list11);
        List list2 = new ArrayList();
        list2.add(14L);
        list2.add(1505158203L);
        list2.add(18L);
        List list21 = new ArrayList();
        list21.add("slowlog".getBytes());
        list21.add("get".getBytes());
        list2.add(list21);
        objectList.add(list1);
        objectList.add(list2);
        when(jedis.slowlogGet(jedis.slowlogLen())).thenReturn(Slowlog.from(objectList));
        Map<String, String> server = Maps.newHashMap();
        server.put("host", "localhost");
        server.put("port", "6379");
        server.put("name", "Server1");
        SlowLogMetrics slowLogMetrics = new SlowLogMetrics(monitorContextConfiguration, server, metricWriteHelper, jedisPool, countDownLatch, 1505158200000L, 1505158220000L);
        slowLogMetrics.run();
        verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());
        for (Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|SlowLog|no_of_new_slow_logs"));
            Assert.assertTrue(metric.getMetricValue().equals("2"));
        }
    }
}
