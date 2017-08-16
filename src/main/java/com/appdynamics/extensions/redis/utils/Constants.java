package com.appdynamics.extensions.redis.utils;

import com.singularity.ee.agent.systemagent.api.MetricWriter;

import java.math.BigDecimal;

public class Constants {
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Redis|";
    public static final String METRIC_SEPARATOR  = "|";
    public static final String AGGREGATION_DEFAULT = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
    public static final String TIME_ROLLUP_DEFAULT = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
    public static final String CLUSTER_ROLLUP_DEFAULT = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
    public static final BigDecimal DEFAULT_MULTIPLIER = BigDecimal.ONE;
}
