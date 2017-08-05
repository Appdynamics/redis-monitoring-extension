package com.appdynamics.extensions.redis;

import com.appdynamics.extensions.redis.metrics.MetricProperties;
import com.appdynamics.extensions.redis.metrics.sectionMetrics.CommonMetricsModifier;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.appdynamics.extensions.yml.YmlReader;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class CommonMetricsModifierTest {
    Map<String, String> individualSectionInfoMap;
    List<Map<String, ?>> individualSectionFields;
    CommonMetricsModifier commonMetricsModifier;
    Map<String,MetricProperties> finalClientMap;
    @Before
    public void init() throws IOException{
        InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
        String info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
        individualSectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, "Clients");
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map<String,?> metrics = (Map<String, ?>)config.get("metrics");
        List<Map<String, ?>> client = (List<Map<String, ?>>)metrics.get("Clients");
        individualSectionFields = client;
        commonMetricsModifier = new CommonMetricsModifier(individualSectionFields, individualSectionInfoMap, "Clients");

    }

    @Test
    public void clientMapWithMetricPropertiesTest(){
        finalClientMap = commonMetricsModifier.metricBuilder();
        Assert.assertTrue(finalClientMap.get("connected_clients").getAlias().equalsIgnoreCase("connected_clients"));
        Assert.assertTrue(finalClientMap.get("connected_clients").getMultiplier().trim().length() == 0);
        Assert.assertTrue(finalClientMap.get("connected_clients").getIsCluster() == true);
        Assert.assertTrue(finalClientMap.get("connected_clients").getInfoValue().equals(new BigDecimal("1")));
    }



}
