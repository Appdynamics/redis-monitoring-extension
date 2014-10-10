/**
 * Copyright 2014 AppDynamics, Inc.
 * 
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
package com.appdynamics.extensions.redis.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigUtil<T> {

	private static Logger logger = Logger.getLogger("com.singularity.extensions.ConfigUtil");

	public T readConfig(String configFilename, Class<T> clazz) throws FileNotFoundException {
		logger.info("Reading config file::" + configFilename);
		Yaml yaml = new Yaml(new Constructor(Configuration.class));
		T config = (T) yaml.load(new FileInputStream(configFilename));
		return config;
	}

}
