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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.redis.utils.Constants.METRIC_SEPARATOR;

class CommonMetricsModifier {

    private List<Map<String, ?>> individualSectionFields;
    private Map<String, String> individualSectionInfoMap;
    private String metricPrefix;
    private String sectionName;
    private static final Logger logger = LoggerFactory.getLogger(CommonMetricsModifier.class);

    CommonMetricsModifier(List<Map<String, ?>> individualSectionFields, Map<String, String> individualSectionInfoMap, String metricPrefix, String sectionName){
        this.individualSectionFields = individualSectionFields;
        this.individualSectionInfoMap = individualSectionInfoMap;
        this.metricPrefix = metricPrefix;
        this.sectionName = sectionName;
    }

    List<Metric> metricBuilder(){
        List<Metric> individualSectionMetricsList = Lists.newArrayList();
        for(Map<String, ?> individualMetricMap : individualSectionFields){ //Iterates through the list of metrics for the specific section("sectionName") and adds each metric in the individualSectionMetrics Map.
            String actualMetricName = individualMetricMap.entrySet().iterator().next().getKey();
            Map<String, ?> metricModifierMap = (Map<String, ?>) individualMetricMap.get(actualMetricName);
            String actualIndividualMetricValue = individualSectionInfoMap.get(actualMetricName);
            String metricPathWithoutMetricName = metricPrefix + METRIC_SEPARATOR + sectionName;
            if(!Strings.isNullOrEmpty(actualIndividualMetricValue)){
                Metric metric;
                String metricPath = metricPathWithoutMetricName + METRIC_SEPARATOR + actualMetricName;
                if(metricModifierMap.size() == 0) {
                    metric = new Metric(actualMetricName, actualIndividualMetricValue, metricPath);
                }
                else{
                    metric = new Metric(actualMetricName, actualIndividualMetricValue, metricPath, metricModifierMap);
                }
                logger.debug("Value for {} under {} is : {}", actualMetricName, metricPathWithoutMetricName, actualIndividualMetricValue);
                individualSectionMetricsList.add(metric);
            }
            else{
                logger.debug("Value for {} under {} not available", actualMetricName, metricPathWithoutMetricName);
            }
        }
        return individualSectionMetricsList;
    }
}
