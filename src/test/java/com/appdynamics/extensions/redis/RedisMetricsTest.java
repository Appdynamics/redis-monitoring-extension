package com.appdynamics.extensions.redis;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.redis.metrics.MetricProperties;
import com.appdynamics.extensions.redis.metrics.RedisMetrics;
import com.appdynamics.extensions.redis.metrics.sectionMetrics.CommonMetricsModifier;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.yml.YmlReader;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 8/4/17.
 */
public class RedisMetricsTest {
    private MonitorConfiguration configuration = mock(MonitorConfiguration.class);
    private Map<String, String> server;
    private MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
    private JedisPool jedisPool = mock(JedisPool.class);
    private JedisPoolConfig jedisPoolConfig = mock(JedisPoolConfig.class);
    private Jedis jedis = mock(Jedis.class);
    Map<String, ?> metricMap;
    Map<String, String> individualSectionInfoMap;
    List<Map<String, ?>> individualSectionFields;
    CommonMetricsModifier commonMetricsModifier;
    Map<String,MetricProperties> finalClientMap;
    String info;
    RedisMetrics redisMetrics;

    @Before
    public void init() throws IOException{
        InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
        info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
        individualSectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, "Clients");
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map<String,?> metrics = (Map<String, ?>)config.get("metrics");
        Map<String, ?> infoMap = (Map<String, ?>)metrics.get("Info");
        List<Map<String, ?>> client = (List<Map<String, ?>>)infoMap.get("Clients");
        individualSectionFields = client;
        commonMetricsModifier = new CommonMetricsModifier(individualSectionFields, individualSectionInfoMap, "Clients");
        finalClientMap = commonMetricsModifier.metricBuilder();
      //  MonitorConfiguration configuration = new MonitorConfiguration("",null,null);
        when(configuration.getMetricWriter()).thenReturn(metricWriteHelper);
        when(configuration.getMetricPrefix()).thenReturn("Server|Component:AppLevels|Custom Metrics|Redis");
        //when(server.get("name")).thenReturn("server1");
        //when(server.get("isCluster")).thenReturn("true");
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.info()).thenReturn(info);


        //redisMetrics = new RedisMetrics(jedisPool, metrics, configuration, server);
    }

    @Test
    public void jedisInfoTest(){
        //Assert.assertTrue(redisMetrics.extractInfo().equalsIgnoreCase(info));
    }


    /*@Test
    public void printNodeLevelMetricTest(){


    }

    @Test
    public void printClusterLevelMetricTest(){

    }*/
}
