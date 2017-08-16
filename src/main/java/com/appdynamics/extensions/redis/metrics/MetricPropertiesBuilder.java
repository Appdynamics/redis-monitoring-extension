package com.appdynamics.extensions.redis.metrics;

import java.util.Map;

public class MetricPropertiesBuilder {

    private Map<String, String> metricOptions;
    private String infoValue;
    private String sectionName;
    private String actualMetricName;

    public MetricPropertiesBuilder(Map<String, String> metricOptions, String infoValue, String sectionName, String actualMetricName){
        this.metricOptions = metricOptions;
        this.infoValue = infoValue;
        this.sectionName = sectionName;
        this.actualMetricName = actualMetricName;
    }

    public MetricProperties buildMetricProperties(){
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setSectionName(sectionName);
        metricProperties.setInfoValue(infoValue);
        metricProperties.setAlias(metricOptions.get("alias"), actualMetricName);
        metricProperties.setMultiplier(metricOptions.get("multiplier"));
        metricProperties.setAggregation(metricOptions.get("aggregation"));
        metricProperties.setTime(metricOptions.get("time"));
        metricProperties.setCluster(metricOptions.get("cluster"));
        metricProperties.setDelta(metricOptions.get("delta"));
        metricProperties.setAggregateAtCluster(metricOptions.get("aggregateAtCluster"));
        return metricProperties;
    }
}
