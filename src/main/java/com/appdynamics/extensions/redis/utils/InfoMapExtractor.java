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

package com.appdynamics.extensions.redis.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.Map;

public class InfoMapExtractor {

    public Map<String, String> extractInfoAsHashMap(String info, String sectionName){
        Map<String, String> infoMap = Maps.newHashMap();
        Splitter sectionSplitter = Splitter.on('#')
                                           .omitEmptyStrings()
                                           .trimResults();
        for(String section : sectionSplitter.split(info)){
            if(!section.toLowerCase().startsWith(sectionName.toLowerCase())) {
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
            String infoLine[] = line.split(":");
            if (infoLine.length == 2) {
                infoMap.put(infoLine[0].trim(), infoLine[1].trim());
            }
        }
        return infoMap;
    }
}
