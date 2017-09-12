package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.Metric;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Slowlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

public class SlowLogMetricsTest {
    private class TaskRunner implements Runnable{
        public void run(){

        }
    }
    AManagedMonitor aManagedMonitor = new AManagedMonitor() {
        @Override
        public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
            return null;
        }
    };
    private static final Logger logger = LoggerFactory.getLogger(SlowLogMetricsTest.class);

    @Test
    public void slowogMetricsTest() throws IOException {
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new SlowLogMetricsTest.TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/conf/config.yml");
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
        SlowLogMetrics slowLogMetrics = new SlowLogMetrics(monitorConfiguration, server, jedisPool, countDownLatch, 1505158200000L, 1505158220000L);
        slowLogMetrics.run();
        verify(metricWriteHelper).printMetric(pathCaptor.capture());
        for (Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|SlowLog|no_of_new_slow_logs"));
            Assert.assertTrue(metric.getMetricValue().equals("2"));
        }
    }

    @Test
    public void slowLogMetricsWithClusterTest() throws IOException {
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new SlowLogMetricsTest.TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/conf/config_WithClusterMetrics.yml");
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

        List list3 = new ArrayList();
        list3.add(13L);
        list3.add(1505158201L);
        list3.add(18L);
        List list31 = new ArrayList();
        list31.add("slowlog".getBytes());
        list31.add("get".getBytes());
        list3.add(list31);

        objectList.add(list1);
        objectList.add(list2);
        objectList.add(list3);

        when(jedis.slowlogGet(jedis.slowlogLen())).thenReturn(Slowlog.from(objectList));
        Map<String, String> server = Maps.newHashMap();
        server.put("host", "localhost");
        server.put("port", "6379");
        server.put("name", "Server1");
        SlowLogMetrics slowLogMetrics = new SlowLogMetrics(monitorConfiguration, server, jedisPool, countDownLatch, 1505158200000L, 1505158220000L);
        slowLogMetrics.run();


        List<Object> objectList2 = Lists.newArrayList();
        List list4 = new ArrayList();
        list4.add(15L);
        list4.add(1505158204L);
        list4.add(18L);
        List list41 = new ArrayList();
        list41.add("slowlog".getBytes());
        list41.add("get".getBytes());
        list4.add(list41);

        List list5 = new ArrayList();
        list5.add(14L);
        list5.add(1505158203L);
        list5.add(18L);
        List list51 = new ArrayList();
        list51.add("slowlog".getBytes());
        list51.add("get".getBytes());
        list5.add(list51);


        objectList2.add(list4);
        objectList2.add(list5);

        when(jedis.slowlogGet(jedis.slowlogLen())).thenReturn(Slowlog.from(objectList2));
        Map<String, String> server2 = Maps.newHashMap();
        server2.put("host", "localhost");
        server2.put("port", "6380");
        server2.put("name", "Server2");
        SlowLogMetrics slowLogMetrics2 = new SlowLogMetrics(monitorConfiguration, server2, jedisPool, countDownLatch, 1505158200000L, 1505158220000L);
        slowLogMetrics2.run();

        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper,times(3)).printMetric(pathCaptor.capture());
        int count = 0;

        for (List<Metric> metricList : pathCaptor.getAllValues()){
            for(Metric metric : metricList){
                if(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Cluster|SlowLog|no_of_new_slow_logs")){
                    Assert.assertTrue(metric.getMetricValue().equals("3") || metric.getMetricValue().equals("2"));
                    count++;
                }
            }

        }
        Assert.assertTrue(count == 2);
    }
}
