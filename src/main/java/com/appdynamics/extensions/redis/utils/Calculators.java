package com.appdynamics.extensions.redis.utils;

import com.appdynamics.extensions.redis.metrics.MetricProperties;
import com.appdynamics.extensions.util.DeltaMetricsCalculator;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/8/17.
 */
public class Calculators {
    private static DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);

    public BigDecimal deltaCalculator(MetricProperties currentMetricProperties, String metricPath, BigDecimal metricValue){
        if(currentMetricProperties.getDelta().equalsIgnoreCase("true")){
            metricValue = deltaCalculator.calculateDelta(metricPath, metricValue);
        }
        return metricValue;
    }

    public BigDecimal multiplier(MetricProperties currentMetricProperties, BigDecimal metricValue){
        metricValue = metricValue.multiply(currentMetricProperties.getMultiplier());
        return metricValue;
    }
}
