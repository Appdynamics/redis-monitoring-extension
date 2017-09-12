package com.appdynamics.extensions.redis.utils;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.Metric;
import com.appdynamics.extensions.util.transformers.Transformer;

import java.util.List;

public class Constants {
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Redis|";
    public static final String METRIC_SEPARATOR  = "|";
    public static final void transformAndPrintNodeLevelMetrics(MonitorConfiguration configuration, List<Metric> metrics){
        Transformer transformer = new Transformer(metrics);
        transformer.transform();
        configuration.getMetricWriter().printMetric(metrics);
    }
}
