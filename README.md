RedisMonitor
============

## Use Case

AppDynamics Machine Agent plugin to monitor a Redis server.

## Installation

1. Run ```ant package``` from the redis-monitoring-extension directory
2. Deploy the file RedisMonitor.zip found in the 'dist' directory into ```<machineagent install dir>/monitors/```
3. Unzip the deployed file
4. Open ```<machineagent install dir>/monitors/RedisMonitor/monitor.xml``` and configure the Redis credentials

 ``` xml
            <argument name="host" is-required="false" default-value="localhost" />
            <argument name="port" is-required="false" default-value="6379" />
            <argument name="password" is-required="false" default-value="" />
 ```
 You can also set the `keyspaces` argument to a comma-separated list of keyspaces, and the plugin will report the number of keys and number of expired keys:
 ``` xml
            <argument name="keyspaces" is-required="false" default-value="db0,db1" />
 ```
5. Restart the machineagent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance | \<Tier\> | Custom Metrics | Redis


Custom Dashboard
-----------------
![](https://raw.github.com/Appdynamics/redis-monitoring-extension/master/RedisDashboard.PNG)
