package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.Aggregator;
import com.appdynamics.extensions.util.AggregatorFactory;
import com.appdynamics.extensions.util.AggregatorKey;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Slowlog;
import java.math.BigDecimal;
import java.util.*;

import static com.appdynamics.extensions.redis.utils.Constants.*;

public class SlowLogMetrics implements Runnable {
    JedisPool jedisPool;
    //int slowlog_log_slower_than;
    Map<String, ?> metricsMap;
    MonitorConfiguration configuration;
    Map<String, String> server;
    Map<String, String> no_of_new_slow_logs_MetricPropertiesMap;
    public static SlowLogTimeCache slowLogTimeCache = new SlowLogTimeCache(10);
    private final ClusterMetricProcessor clusterMetricProcessor = new ClusterMetricProcessor();
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);
    int slowLogCount;

    public SlowLogMetrics(JedisPool jedisPool, Map<String, ?> metricsMap, MonitorConfiguration configuration, Map<String, String> server){
        this.jedisPool = jedisPool;
        this.metricsMap = metricsMap;
        //this.slowlog_log_slower_than = slowlog_log_slower_than;
        this.server = server;
        this.configuration = configuration;
    }

    public void run() {
        //logger.info("============================I am here=====================================");
        List<Map<String,?>> slowLogMetricsList = (List<Map<String, ?>>)metricsMap.get("Slowlog");
        //logger.info("==============================================================" + slowLogMetricsList.toString());
        if(slowLogMetricsList != null) {
            no_of_new_slow_logs_MapExtractor(slowLogMetricsList);
            slowLogExtractor();
        }
    }

    public void no_of_new_slow_logs_MapExtractor(List<Map<String, ?>> slowLogMetricsList) {
        ListIterator<Map<String, ?>> list = slowLogMetricsList.listIterator();
        while(list.hasNext()){
            Map<String, ?> metricEntries = list.next();
            for(Map.Entry<String, ?> metricEntry : metricEntries.entrySet()) {
                if(metricEntry.getKey().equalsIgnoreCase("no_of_new_slow_logs")){
                    no_of_new_slow_logs_MetricPropertiesMap = (Map<String, String>) metricEntry.getValue();
                    return;
                }
            }
        }
    }

    public void slowLogExtractor() {
        if(no_of_new_slow_logs_MetricPropertiesMap != null){
            List<Slowlog> slowlogs;
            try(Jedis jedis = jedisPool.getResource()) {
                //jedis.configSet("slowlog-log-slower-than", no_of_new_slow_logs_MetricMap.get("slowlog-log-slower-than"));
                //jedis.configSet( "slowlog-max-len", no_of_new_slow_logs_MetricMap.get("slowlog-max-len"));
                slowlogs = jedis.slowlogGet(jedis.slowlogLen());
            }
            slowLogCount = countNumberOfNewSlowLogs(slowlogs);
            printNodeLevelMetrics(slowLogCount);
            if(no_of_new_slow_logs_MetricPropertiesMap.get("isCluster") != null && no_of_new_slow_logs_MetricPropertiesMap.get("isCluster").equalsIgnoreCase("true")){
                printClusterLevelMetrics(slowLogCount);
            }
        }
    }

    public int countNumberOfNewSlowLogs(List<Slowlog> slowlogs) {
        int count = 0;
        Long mostRecentTimeStampFromCache = slowLogTimeCache.getMostRecentTimeStamp();
        if(mostRecentTimeStampFromCache == null){
            mostRecentTimeStampFromCache = Calendar.getInstance().getTimeInMillis() / 1000L;
        }
        Long mostRecentTimeStamp = mostRecentTimeStampFromCache;
        for(Slowlog individualLog : slowlogs){
            Long tempTimeStamp = individualLog.getTimeStamp();
            if(tempTimeStamp > mostRecentTimeStampFromCache){
                count++;
                if(tempTimeStamp > mostRecentTimeStamp){
                    mostRecentTimeStamp = tempTimeStamp;
                }

            }
        }
        slowLogTimeCache.setMostRecentTimeStamp("mostRecentTimeStamp", mostRecentTimeStamp);
        return count;
    }

    public void printNodeLevelMetrics(int slowLogValue){
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String aggregationType = MetricWriter.METRIC_AGGREGATION_TYPE_SUM;
        String timeRollupType = MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM;
        String clusterRollupType = CLUSTER_ROLLUP_DEFAULT;
        String metricPath = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + METRIC_SEPARATOR + "SlowLog" + METRIC_SEPARATOR + "no_of_new_slow_logs";
        BigDecimal metricValue = new BigDecimal(slowLogValue);
        if(metricValue != null) {
            metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
        }

    }
    public void printClusterLevelMetrics(int slowLogvalue){
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        AggregatorFactory aggregatorFactory = new AggregatorFactory();
        clusterMetricProcessor.collect(aggregatorFactory,"no_of_new_slow_logs" ,slowLogvalue, no_of_new_slow_logs_MetricPropertiesMap);
        Collection<Aggregator<AggregatorKey>> aggregators = aggregatorFactory.getAggregators();
        for(Aggregator<AggregatorKey> aggregator: aggregators){
            Set<AggregatorKey> keys = aggregator.keys();
            for(AggregatorKey key : keys){
                BigDecimal value = aggregator.getAggregatedValue(key);
                String path = configuration.getMetricPrefix() + METRIC_SEPARATOR + "Cluster" + METRIC_SEPARATOR + key.getMetricPath();
                String splits[] = key.getMetricType().split("\\.");
                if(splits.length == 3)
                metricWriter.printMetric(path, value.toString(), splits[0], splits[1], splits[2]);
            }
        }
    }
}
