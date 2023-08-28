/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.redis.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.Map;

public class InfoMapExtractor {

    public Map<String, String> extractInfoAsHashMap(String info, String sectionName) {
        Map<String, String> infoMap = Maps.newHashMap();
        Splitter sectionSplitter = Splitter.on('#')
                .omitEmptyStrings()
                .trimResults();
        for (String section : sectionSplitter.split(info)) {
            if (!section.toLowerCase().startsWith(sectionName.toLowerCase())) {
                continue;
            }
            return sectionMapGenerator(section, sectionName);
        }
        return infoMap;
    }

    private Map<String, String> sectionMapGenerator(String sectionData, String sectionName) {
        Map<String, String> infoMap = Maps.newHashMap();
        Splitter lineSplitter = Splitter.on(System.getProperty("line.separator"))
                .omitEmptyStrings()
                .trimResults();
        for (String line : lineSplitter.split(sectionData)) {
            if (line.toLowerCase().startsWith(sectionName)) {
                continue;
            }
            String[] infoLine = line.split(":");
            if (infoLine.length == 2 && positiveNumber(infoLine[1].trim())) {
                infoMap.put(infoLine[0].trim(), removeInvalidCharacters(infoLine[1].trim()));
            }

        }
        return infoMap;
    }

    private boolean positiveNumber(String trim) {
        return !trim.contains("-");
    }

    private String removeInvalidCharacters(String metric) {
        return metric.replaceAll("%", "");
    }
}
