Redis Monitor
============
This extension works only with the standalone machine agent.

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
	<li>Type 'mvn clean install' in the command line from the redis-monitoring-extension directory.
	</li>
	<li>Deploy the file RedisMonitor.zip found in the 'target' directory into the &lt;machineagent install dir&gt;/monitors/ directory.
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


##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/redis-monitoring-extension).

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/eXchange/Redis---Monitoring-Extension/idi-p/4505) community.

##Support

For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).

