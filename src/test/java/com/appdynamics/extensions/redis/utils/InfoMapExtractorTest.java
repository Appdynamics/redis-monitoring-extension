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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;



public class InfoMapExtractorTest {

    InfoMapExtractor infoMapExtractor = new InfoMapExtractor();
    String info;
    @Before
    public void init() throws IOException{
        info = FileUtils.readFileToString(new File("src/test/resources/info.txt"));
    }

    @Test
    public void sectionDataParseTest() throws IOException{
        Map<String, String> sectionInfoMap = infoMapExtractor.extractInfoAsHashMap(info, "Clients");
        Assert.assertTrue(sectionInfoMap.get("connected_clients").equals("1"));
        Assert.assertTrue(sectionInfoMap.get("client_longest_output_list").equals("0"));
        Map<String, String> sectionInfoMap2 = infoMapExtractor.extractInfoAsHashMap(info, "Memory");
        Assert.assertTrue(sectionInfoMap2.get("used_memory").equals("1031856"));
        Assert.assertTrue(sectionInfoMap2.get("used_memory_human").equals("1007.67K"));
    }

    @Test
    public void invalidSectionDataParseTest() throws IOException{
        Map<String, String> sectionInfoMap3 = infoMapExtractor.extractInfoAsHashMap(info, "No");
        Assert.assertTrue(sectionInfoMap3.size() == 0);
    }
}
