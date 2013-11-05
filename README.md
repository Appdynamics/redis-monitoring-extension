RedisMonitor
============

## Use Case

AppDynamics Machine Agent plugin to monitor a Redis server.

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


## Metrics

- Commands processed
- Connected clients
- Connected slaves
- Connections received
- Evicted keys
- Expired keys
- Kepspace hits
- Kepspace misses
- Rejected connections
- Used memory (KB)

Custom Dashboard
-----------------
![](https://raw.github.com/Appdynamics/redis-monitoring-extension/master/RedisDashboard.PNG)
