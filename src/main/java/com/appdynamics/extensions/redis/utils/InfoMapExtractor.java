package com.appdynamics.extensions.redis.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class InfoMapExtractor {
    private static final Logger logger = LoggerFactory.getLogger(InfoMapExtractor.class);

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
