package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.redis.utils.ValidityChecker;
import com.appdynamics.extensions.util.AggregatorFactory;
import com.appdynamics.extensions.util.AggregatorKey;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

import static com.appdynamics.extensions.redis.utils.Constants.*;

public class ClusterMetricProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);

    public void collect(AggregatorFactory aggregatorFactory, Map<String, MetricProperties> metrics){
       if(metrics == null){
           return;
       }
       for(Map.Entry<String, MetricProperties> metric : metrics.entrySet()){
           String metricName = metric.getKey();
           MetricProperties currentMetricProperties = metric.getValue();
           if(currentMetricProperties.getIsCluster()){
               String metricType = getMetricType(currentMetricProperties);
               AggregatorKey aggregatorKey = new AggregatorKey(currentMetricProperties.getSectionName() + METRIC_SEPARATOR + currentMetricProperties.getAlias(), metricType );
               aggregatorFactory.getAggregator(metricType).add(aggregatorKey, currentMetricProperties.getModifiedFinalValue().toString());
           }

       }
    }

    public void collect(AggregatorFactory aggregatorFactory, String slowLogMetricName, int metricValue, Map<String, String> slowLogMetricProperties){
        if(slowLogMetricProperties == null){
            return;
        }
        if(slowLogMetricProperties.get("isCluster") != null && slowLogMetricProperties.get("isCluster").equalsIgnoreCase("true")){
            String metricType = getSlowLogMetricType(slowLogMetricProperties);
            String alias = slowLogMetricProperties.get("alias");
            AggregatorKey aggregatorKey = new AggregatorKey("Slowlog" + METRIC_SEPARATOR + ((alias == null) || (alias.trim().length() == 0) ? slowLogMetricName : alias), metricType) ;
            BigDecimal metricValueBigD = new BigDecimal(metricValue);
            aggregatorFactory.getAggregator(metricType).add(aggregatorKey, metricValueBigD.toString());
        }


    }

    private String getMetricType(MetricProperties metricProperties){
        String metricType = metricProperties.getAggregation() + "." + metricProperties.getTime();
        if(metricProperties.getCluster().equals(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL)){
            return metricType + "." + "IND";
        }
        return metricType + "." + "SUM";
    }

    private String getSlowLogMetricType(Map<String, String> slowLogMetricProperties){
        String aggregation = slowLogMetricProperties.get("aggregation");
        String timeRollUp = slowLogMetricProperties.get("time");
        String clusterRollUp = slowLogMetricProperties.get("cluster");
        ValidityChecker validityChecker = new ValidityChecker();
        aggregation = validityChecker.validAggregation(aggregation) ? aggregation : AGGREGATION_DEFAULT;
        timeRollUp = validityChecker.validTime(timeRollUp) ? timeRollUp : TIME_ROLLUP_DEFAULT;
        clusterRollUp = validityChecker.validCluster(clusterRollUp) ? clusterRollUp : CLUSTER_ROLLUP_DEFAULT;
        String metricType = aggregation + "." + timeRollUp + "." + clusterRollUp;
        return  metricType;
    }
}
