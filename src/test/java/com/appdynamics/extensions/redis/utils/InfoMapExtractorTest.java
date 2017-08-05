package com.appdynamics.extensions.redis.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by venkata.konala on 8/4/17.
 */
public class InfoMapExtractorTest {
    InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
    Map<String, String> sectionInfoMap;
    String info;
    @Before
    public void init() throws IOException{
        info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
    }

    @Test
    public void sectionDataParseTest() throws IOException{

        sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, "Clients");
        Assert.assertTrue(sectionInfoMap.get("connected_clients").equals("1"));
        Assert.assertTrue(sectionInfoMap.get("client_longest_output_list").equals("0"));
        sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, "Memory");
        Assert.assertTrue(sectionInfoMap.get("used_memory").equals("1031856"));
        Assert.assertTrue(sectionInfoMap.get("used_memory_human").equals("1007.67K"));

    }


}
