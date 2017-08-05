package com.appdynamics.extensions.redis.metrics;

import java.util.Map;

public class MetricPropertiesBuilder {

    Map<String, String> metricOptions;
    String infoValue;
    MetricProperties metricProperties;
    String sectionName;
    String actualMetricName;

    public MetricPropertiesBuilder(Map<String, String> metricOptions, String infoValue, String sectionName, String actualMetricName){
        this.metricOptions = metricOptions;
        this.infoValue = infoValue;
        this.sectionName = sectionName;
        this.actualMetricName = actualMetricName;
    }

    public MetricProperties buildMetricProperties(){
        metricProperties = new MetricProperties();
        metricProperties.setSectionName(sectionName);
        metricProperties.setValue(infoValue);
        metricProperties.setAlias(metricOptions.get("alias"), actualMetricName);
        metricProperties.setMultiplier(metricOptions.get("multiplier"));
        metricProperties.setAggregation(metricOptions.get("aggregation"));
        metricProperties.setTime(metricOptions.get("time"));
        metricProperties.setCluster(metricOptions.get("cluster"));
        metricProperties.setDelta(metricOptions.get("delta"));
        metricProperties.setIsCluster(metricOptions.get("isCluster"));
        return metricProperties;
    }



}
