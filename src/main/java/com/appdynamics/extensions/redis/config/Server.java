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

import java.util.HashSet;
import java.util.Set;

public class Server {

	private String host;
	private int port;
	private String password;
	private Set<String> keyspaces = new HashSet<String>();
	private String displayName;
	private Set<String> excludePatterns = new HashSet<String>();

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getKeyspaces() {
		return keyspaces;
	}

	public void setKeyspaces(Set<String> keyspaces) {
		this.keyspaces = keyspaces;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Set<String> getExcludePatterns() {
		return excludePatterns;
	}

	public void setExcludePatterns(Set<String> excludePatterns) {
		this.excludePatterns = excludePatterns;
	}
}
