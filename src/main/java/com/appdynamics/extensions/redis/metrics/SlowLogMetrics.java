package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.NumberUtils;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.redis.utils.Calculators;
import com.appdynamics.extensions.util.*;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Slowlog;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import static com.appdynamics.extensions.redis.utils.Constants.*;

public class SlowLogMetrics implements Runnable {
    private JedisPool jedisPool;
    private Map<String, ?> metricsMap;
    private MonitorConfiguration configuration;
    private Map<String, String> server;
    private Map<String, String> metricPropertiesMap;
    //#TODO Check this with Kunal
    //private static SlowLogTimeCache slowLogTimeCache = new SlowLogTimeCache(10);........
    private static Long mostRecentTimeStampVariable;
    private final ClusterMetricProcessor clusterMetricProcessor = new ClusterMetricProcessor();
    private static final Logger logger = LoggerFactory.getLogger(RedisMetrics.class);
    private CountDownLatch countDownLatch;
    private static DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);

    public SlowLogMetrics(JedisPool jedisPool, Map<String, ?> metricsMap, MonitorConfiguration configuration, Map<String, String> server, CountDownLatch countDownLatch){
        this.jedisPool = jedisPool;
        this.metricsMap = metricsMap;
        this.server = server;
        this.configuration = configuration;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        List<Map<String,?>> slowLogMetricsList = (List<Map<String, ?>>)metricsMap.get("Slowlog");
        if(slowLogMetricsList != null) {
            metricMapExtractor(slowLogMetricsList);
            slowLogExtractor();
        }
    }

    private void metricMapExtractor(List<Map<String, ?>> slowLogMetricsList) {
        ListIterator<Map<String, ?>> list = slowLogMetricsList.listIterator();
        while(list.hasNext()){
            Map<String, ?> metricEntries = list.next();
            for(Map.Entry<String, ?> metricEntry : metricEntries.entrySet()) {
                if(metricEntry.getKey().equalsIgnoreCase("no_of_new_slow_logs")){
                    metricPropertiesMap = (Map<String, String>) metricEntry.getValue();
                    return;
                }
            }
        }
    }

    private void slowLogExtractor() {
        int slowLogCount;
        if(metricPropertiesMap != null){
            List<Slowlog> slowlogs;
            try(Jedis jedis = jedisPool.getResource()) {
                slowlogs = jedis.slowlogGet(jedis.slowlogLen());
            }
            countDownLatch.countDown();
            slowLogCount = countNumberOfNewSlowLogs(slowlogs);
            BigDecimal modifiedMetricValue = printNodeLevelMetrics(slowLogCount);
            printClusterLevelMetrics(modifiedMetricValue);
        }
    }

    private int countNumberOfNewSlowLogs(List<Slowlog> slowlogs) {
        int count = 0;
        //Long mostRecentTimeStampFromCache = slowLogTimeCache.getMostRecentTimeStamp();
        Long mostRecentTimeStampFromCache = mostRecentTimeStampVariable;
        logger.info("========================Time from Cache============================" +mostRecentTimeStampFromCache);
        if(mostRecentTimeStampFromCache == null){
            mostRecentTimeStampFromCache = Calendar.getInstance().getTimeInMillis() / 1000L;
            logger.info("===================if null, then get it from calender instance==========" + mostRecentTimeStampFromCache);
        }
        logger.info("================Slowlog===================\n");
        logger.info("List :" + slowlogs);
        Long mostRecentTimeStamp = mostRecentTimeStampFromCache;
        for(Slowlog individualLog : slowlogs){
            Long tempTimeStamp = individualLog.getTimeStamp();
            //individualLog.get
            if(tempTimeStamp > mostRecentTimeStampFromCache){
                count++;
                if(tempTimeStamp > mostRecentTimeStamp){
                    mostRecentTimeStamp = tempTimeStamp;
                }

            }
        }
        //slowLogTimeCache.setMostRecentTimeStamp("mostRecentTimeStamp", mostRecentTimeStamp);
        mostRecentTimeStampVariable = mostRecentTimeStamp;
        return count;
    }

    private BigDecimal printNodeLevelMetrics(int slowLogValue){
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPath = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + METRIC_SEPARATOR + "SlowLog" + METRIC_SEPARATOR + metricPropertiesMap.get("alias");
        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, String.valueOf(slowLogValue), "SlowLog", "no_of_new_slow_logs");
        MetricProperties currentMetricProperties = metricPropertiesBuilder.buildMetricProperties();
        BigDecimal metricValue = currentMetricProperties.getInfoValue();
        Calculators calculators = new Calculators();
        metricValue = calculators.deltaCalculator(currentMetricProperties, metricPath, metricValue);
        metricValue = calculators.multiplier(currentMetricProperties, metricValue);
        currentMetricProperties.setModifiedFinalValue(metricValue);
        String aggregationType = currentMetricProperties.getAggregation();
        String timeRollupType = currentMetricProperties.getTime();
        String clusterRollupType = currentMetricProperties.getCluster();
        if(metricValue != null) {
            metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
        }
        return metricValue;

    }

    private void printClusterLevelMetrics(BigDecimal modifiedSlowLogvalue){
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        AggregatorFactory aggregatorFactory = new AggregatorFactory();
        clusterMetricProcessor.collect(aggregatorFactory,"no_of_new_slow_logs" ,modifiedSlowLogvalue, metricPropertiesMap);
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
