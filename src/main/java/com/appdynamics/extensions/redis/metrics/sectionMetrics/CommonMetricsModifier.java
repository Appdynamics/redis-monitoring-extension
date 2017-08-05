package com.appdynamics.extensions.redis.metrics.sectionMetrics;

import com.appdynamics.extensions.redis.metrics.MetricProperties;
import com.appdynamics.extensions.redis.metrics.MetricPropertiesBuilder;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CommonMetricsModifier {
    Map<String, MetricProperties> individualSectionMetrics;
    List<Map<String, ?>> individualSectionFields;
    Map<String, String> individualSectionInfoMap;
    String sectionName;
    private static final Logger logger = LoggerFactory.getLogger(CommonMetricsModifier.class);

    public CommonMetricsModifier(List<Map<String, ?>> individualSectionFields, Map<String, String> individualSectionInfoMap, String sectionName){
        this.individualSectionFields = individualSectionFields;
        this.individualSectionInfoMap = individualSectionInfoMap;
        this.sectionName = sectionName;
    }

    public Map<String, MetricProperties> metricBuilder(){
        individualSectionMetrics = Maps.newHashMap();
        for(Map<String, ?> individualMetricMap : individualSectionFields){ //Iterates through the list of metrics for the specific section("sectionName") and adds each metric in the individualSectionMetrics Map.
            String actualMetricName = individualMetricMap.entrySet().iterator().next().getKey();
            Map<String, String> metricModifierMap = (Map<String, String>)individualMetricMap.get(actualMetricName);
            String actualIndividualMetricValue =  individualSectionInfoMap.get(actualMetricName);
            MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(metricModifierMap, actualIndividualMetricValue, sectionName, actualMetricName);
            MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
            individualSectionMetrics.put(actualMetricName, metricProperties );
        }
        return individualSectionMetrics;
    }
}
