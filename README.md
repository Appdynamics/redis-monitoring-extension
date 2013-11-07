Redis Monitor
============
This extension works only with the Java agent.

## Use Case

Redis is an in memory key-value data store. The Redis monitoring extension gathers Redis server statistics and display them in AppDynamics Metric Browser.

Metrics include:
- Connection count, connected clients, slaves, rejected connection count
- Command count
- Keyspace hits and misses
- Memory usage
- Evicted keys
- Expired keys

## Installation
<ol>
	<li>Type 'ant package' in the command line from the redis-monitoring-extension directory.
	</li>
	<li>Deploy the file RedisMonitor.zip found in the 'dist' directory into the &lt;machineagent install dir&gt;/monitors/ directory.
	</li>
	<li>Unzip the deployed file.
	</li>
	<li>Open &lt;machineagent install dir&gt;/monitors/RedisMonitor/monitor.xml and configure the Redis credentials.
<p></p>
<pre>
	&lt;argument name="host" is-required="false" default-value="localhost" /&gt;          
	&lt;argument name="port" is-required="false" default-value="6379" /&gt;
	&lt;argument name="password" is-required="false" default-value="" /&gt;
</pre>
 
You can also set the 'keyspaces' argument to a comma-separated list of keyspaces, and the plugin will report the number of keys and number of expired keys:
<p>
</p>
<pre>
	&lt;argument name="keyspaces" is-required="false" default-value="db0,db1" /&gt;
</pre>
        </li>	
	<li> Restart the machine agent.
	</li>
	<li>In the AppDynamics Metric Browser, look for: Application Infrastructure Performance | &lt;Tier&gt; | Custom Metrics | Redis
	</li>
</ol>

## Directory Structure

| Directory/File | Description |
|----------------|-------------|
|conf            | Contains the monitor.xml |
|lib             | Contains third-party project references |
|src             | Contains source code of the Redis monitoring extension |
|dist            | Only obtained when using ant. Run 'ant build' to get binaries. Run 'ant package' to get the distributable .zip file |
|build.xml       | Ant build script to package the project (required only if changing Java code) |

## Metrics

|Metric Name           | Description     |
|----------------------|-----------------|
|commands_processed    | Number of commands processed per minute |
|connected_clients     | Number of currently connected clients |
|connected_slaves      | Number of currently connected slaves |
|connections_received  | Total number of connections received per minute |
|evicted_keys          | Number of evicted keys per minute |
|expired_keys          | Number of expired keys per minute |
|keyspace_hits         | Number of keyspace hits per minute |
|keyspace_misses       | Number of kepspace misses per minute |
|rejected_connections  | Number of rejected connections per minute |
|used_memory           | Total memory used (KB) |

Custom Dashboard Example
------------------------
![](https://raw.github.com/Appdynamics/redis-monitoring-extension/master/RedisDashboard.PNG)
