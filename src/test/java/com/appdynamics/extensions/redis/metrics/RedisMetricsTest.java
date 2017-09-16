package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.MetricWriteHelperFactory;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import static org.mockito.Mockito.*;
/**
 * Created by venkata.konala on 8/4/17.
 */
public class RedisMetricsTest {

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

    @Test
    public void redisMetricsTest() throws IOException{
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        JedisPool jedisPool = mock(JedisPool.class);
        Jedis jedis = mock(Jedis.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        when(jedisPool.getResource()).thenReturn(jedis);
        String info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
        when(jedis.info()).thenReturn(info);
        Map<String, String> server = Maps.newHashMap();
        server.put("host", "localhost");
        server.put("port", "6379");
        server.put("name", "Server1");
        RedisMetrics redisMetrics = new RedisMetrics(monitorConfiguration, server, jedisPool, countDownLatch);
        redisMetrics.run();
        verify(metricWriteHelper).printMetric(pathCaptor.capture());
        List<String> metricPathsList = Lists.newArrayList();
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|connected_clients");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|client_longest_output_list");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|client_biggest_input_buf");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_rss");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_peak");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_lua");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|mem_fragmentation_ratio");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_changes_since_last_save");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_last_bgsave_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_current_bgsave_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|aof_last_rewrite_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|aof_current_rewrite_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|total_connections_received");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|total_commands_processed");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|instantaneous_ops_per_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|rejected_connections");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|expired_keys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|evicted_keys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_hits");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_misses");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|pubsub_channels");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|pubsub_patterns");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|latest_fork_usec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Replication|connected_slaves");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_sys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_user");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_sys_children");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_user_children");
        for (Metric metric : (List<Metric>)pathCaptor.getValue()){
            org.junit.Assert.assertTrue(metricPathsList.contains(metric.getMetricPath()));
        }

    }

    @Test
    public void redisMetricsWithDerivedTest() throws IOException{
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/conf/config_WithDerivedMetrics.yml");
        JedisPool jedisPool = mock(JedisPool.class);
        Jedis jedis = mock(Jedis.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        when(jedisPool.getResource()).thenReturn(jedis);
        String info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
        when(jedis.info()).thenReturn(info);
        Map<String, String> server = Maps.newHashMap();
        server.put("host", "localhost");
        server.put("port", "6379");
        server.put("name", "Server1");
        RedisMetrics redisMetrics = new RedisMetrics(monitorConfiguration, server, jedisPool, countDownLatch);
        redisMetrics.run();
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(2)).printMetric(pathCaptor.capture());
        List<String> metricPathsList = Lists.newArrayList();
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|connected_clients");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|client_longest_output_list");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Clients|client_biggest_input_buf");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_rss");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_peak");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|used_memory_lua");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|mem_fragmentation_ratio");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_changes_since_last_save");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_last_bgsave_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|rdb_current_bgsave_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|aof_last_rewrite_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Persistence|aof_current_rewrite_time_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|total_connections_received");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|total_commands_processed");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|instantaneous_ops_per_sec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|rejected_connections");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|expired_keys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|evicted_keys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_hits");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_misses");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_ratio");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|pubsub_channels");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|pubsub_patterns");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|latest_fork_usec");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Replication|connected_slaves");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_sys");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_user");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_sys_children");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|used_cpu_user_children");
        metricPathsList.add("Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Memory|total_used_memory");
        for (List<Metric> metricList : pathCaptor.getAllValues()){
            for(Metric metric : metricList){
                org.junit.Assert.assertTrue(metricPathsList.contains(metric.getMetricPath()));
                if(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Stats|keyspace_ratio")){
                    org.junit.Assert.assertTrue(metric.getMetricValue().equals("1"));
                }
                if(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory|total_used_memory")){
                    org.junit.Assert.assertTrue(metric.getMetricValue().equals("2559664"));
                }

            }
        }
    }

    @Test
    public void redisMetricsWithClusterTest() throws IOException{
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/conf/config_WithClusterMetrics.yml");
        JedisPool jedisPool = mock(JedisPool.class);
        Jedis jedis = mock(Jedis.class);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        when(jedisPool.getResource()).thenReturn(jedis);
        String info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
        when(jedis.info()).thenReturn(info);
        Map<String, String> server1 = Maps.newHashMap();
        server1.put("host", "localhost");
        server1.put("port", "6379");
        server1.put("name", "Server1");
        RedisMetrics redisMetrics1 = new RedisMetrics(monitorConfiguration, server1, jedisPool, countDownLatch);
        redisMetrics1.run();
        String info2 = FileUtils.readFileToString(new File("src/test/resources/info2.txt"));
        when(jedis.info()).thenReturn(info2);
        Map<String, String> server2 = Maps.newHashMap();
        server2.put("host", "localhost");
        server2.put("port", "6380");
        server2.put("name", "Server2");
        RedisMetrics redisMetrics2 = new RedisMetrics(monitorConfiguration, server2, jedisPool, countDownLatch);
        redisMetrics2.run();
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(3)).printMetric(pathCaptor.capture());
        int count = 0;
        for (List<Metric> metricList : pathCaptor.getAllValues()){
            for(Metric metric : metricList){
                if(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Stats|keyspace_ratio")){
                    org.junit.Assert.assertTrue(metric.getMetricValue().equals("2") || metric.getMetricValue().equals("3"));
                    count++;
                }
            }
        }
        org.junit.Assert.assertTrue(count == 2);
    }
}
