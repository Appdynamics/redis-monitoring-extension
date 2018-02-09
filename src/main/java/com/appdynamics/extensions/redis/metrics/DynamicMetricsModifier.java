/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.redis.utils.InfoMapExtractor;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.redis.utils.Constants.METRIC_SEPARATOR;

/**
 * Created by venkata.konala on 1/9/18.
 */
public class DynamicMetricsModifier {

    private JedisPool jedisPool;
    private List<Metric> finalMetricsList;
    private String metricPrefix;
    private String sectionName;
    private List<Map<String, ?>> metricsInSectionConfig;
    private InfoMapExtractor infoMapExtractor;

    public DynamicMetricsModifier(JedisPool jedisPool, String metricPrefix, String sectionName, List<Map<String, ?>> metricInSectionConfig){
        this.jedisPool = jedisPool;
        this.metricPrefix = metricPrefix;
        this.sectionName = sectionName;
        this.metricsInSectionConfig = metricInSectionConfig;
        finalMetricsList = Lists.newArrayList();
        infoMapExtractor = new InfoMapExtractor();
    }


    public List<Metric> getMetricsList(){

        String infoCommandStats = getCommandStatsInfo();

        Multimap<String, String> commandStatsMap = infoMapExtractor.extractInfoAsMultiMap(infoCommandStats, sectionName);

        for(Map<String, ?> individualMetricFromConfig : metricsInSectionConfig){
            String name = individualMetricFromConfig.entrySet().iterator().next().getKey();
            Map<String, ?> properties = (Map<String, ?>)individualMetricFromConfig.entrySet().iterator().next().getValue();
            if(commandStatsMap.containsKey(name)){
                for(String value : commandStatsMap.get(name)) {
                    String[] metricCommandStat = value.split("=");
                    String metric = metricCommandStat[0];
                    String metricValue = metricCommandStat[1];
                    String metricPath = metricPrefix + METRIC_SEPARATOR + "Commandstats" + METRIC_SEPARATOR + name + METRIC_SEPARATOR + metric;
                    Metric metric1 = new Metric(metric, metricValue, metricPath);
                    finalMetricsList.add(metric1);
                }
            }
        }
        return finalMetricsList;
    }



    private String getCommandStatsInfo(){
        String info;
        try(Jedis jedis = jedisPool.getResource()) {
            info = jedis.info("commandstats");
        }
        return info;
    }
}
