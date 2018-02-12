# Redis Monitoring Extension for AppDynamics

## Use Case
Redis is an in memory key-value data store used as a database, cache and message broker. It supports data structures such as strings, hashes, lists, sets,
sorted sets with range queries, bitmaps, hyperloglogs and geospatial indexes with radius queries.

The Redis monitoring extension can monitor multiple Redis servers and display the statistics in AppDynamics Metric Browser.

## Prerequisites
1. This extension works only with the standalone Java machine agent. The extension requires the machine agent to be up and running.
2. This extension creates a java client to the Redis server that needs to be monitored. So the Redis server that has to be monitored, should be available
   for access from the machine that has the extension installed.

## Installing the extension
1. Unzip the contents of "RedisMonitor.zip" as "RedisMonitor" and copy the "RedisMonitor" directory to `<MACHINE_AGENT_HOME>/monitors/`

## Recommendations
It is recommended that a single Redis monitoring extension be used to monitor multiple Redis servers belonging to a single cluster.

## Configuring the extension using config.yml
Configure the Redis monitoring extension by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/RedisMonitor/`

  1. Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER NAME OR TIER ID>` in
     metricPrefix: "Server|Component:`<TIER NAME OR TIER ID>`|Custom Metrics|Redis".

     For example,
     ```
     metricPrefix: "Server|Component:Extensions tier|Custom Metrics|Redis"
     ```

  2. Configure the Redis instances by specifying the name(required), host(required), port(required), sslEnabled(required) of the Redis instance, password (only if authentication enabled),
     encryptedPassword(only if password encryption required).

     For example,
     ```
      #Add your list of Redis servers here.
      servers:
        - name: "Server1"
          host: "localhost"
          port: "6379"
          password: ""
          sslEnabled: "false"
          encryptedPassword: ""
        - name: "Server2"
          host: "localhost"
          port: "6380"
          password: ""
          sslEnabled: "false"
          encryptedPassword: ""
     ```

  3. Configure the encyptionKey for encryptionPasswords(only if password encryption required).

     For example,
     ```
     #Encryption key for Encrypted password.
     encryptionKey: "axcdde43535hdhdgfiniyy576"
     ```

  4. Configure the numberOfThreads(only if the number of Redis servers need to be monitored is greater than 7).

     For example,
     
     If number Redis servers that need to be monitored is 10, then number of threads required is 10 * 3 = 30
     ```
     numberOfThreads: 30
     ```

  5. Configure the metrics section.

     For configuring the metrics, the following properties can be used:

     |     Property      |   Default value |         Possible values         |                                              Description                                                                                                |
     | :---------------- | :-------------- | :------------------------------ | :------------------------------------------------------------------------------------------------------------- |
     | alias             | metric name     | Any string                      | The substitute name to be used in the metric browser instead of metric name.                                   |
     | aggregationType   | "AVERAGE"       | "AVERAGE", "SUM", "OBSERVATION" | [Aggregation qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)    |
     | timeRollUpType    | "AVERAGE"       | "AVERAGE", "SUM", "CURRENT"     | [Time roll-up qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)   |
     | clusterRollUpType | "INDIVIDUAL"    | "INDIVIDUAL", "COLLECTIVE"      | [Cluster roll-up qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)|
     | multiplier        | 1               | Any number                      | Value with which the metric needs to be multiplied.                                                            |
     | convert           | null            | Any key value map               | Set of key value pairs that indicates the value to which the metrics need to be transformed. eg: UP:0, DOWN:1  |
     | delta             | false           | true, false                     | If enabled, gives the delta values of metrics instead of actual values.                                        |

     For example,
     ```
     - total_connections_received:  #Total number of connections accepted by the server
         alias: "connectionsReceived"
         multiplier: 1
         aggregationType: "SUM"
         timeRollUpType: "CURRENT"
         clusterRollUpType: "INDIVIDUAL"
         delta: true
     - role:  #Role of Redis server(master or slave)
         convert:
           master: 1
           slave: 0
     ```
     **All these metric properties are optional, and the default value shown in the table is applied to the metric(if a property has not been specified) by default.**
     
## Password encryption
To avoid setting the clear text password in the config.yml, please follow the steps below to encrypt the password and set the encrypted password and the key in the config.yml:
1. Download the util jar to encrypt the password from [here](https://github.com/Appdynamics/maven-repo/raw/master/releases/com/appdynamics/appd-exts-commons/2.0.0/appd-exts-commons-2.0.0.jar).
2. Encrypt password from the command line using the following command :
   ```
   java -cp "appd-exts-commons-2.0.0.jar" com.appdynamics.extensions.crypto.Encryptor myKey myPassword
   ```
   where "myKey" is any random key,
         "myPassword" is the actual password that needs to be encrypted
3. Add the values for "encryptionKey", "encryptedPassword" in the config.yml. 
   The value for "encryptionKey" is the value substituted for "myKey" in the above command.
   The value for "encryptedPassword" is the result of the above command.     

## Metrics
     This extension uses [INFO](http://redis.io/commands/info) command to fetch metrics from Redis server. Some of the metrics are listed below:
      * Clients: connected_clients, blocked_clients
      * Memory: used_memory, used_memory_rss, used_memory_peak, used_memory_lua, mem_fragmentation_ratio
      * Stats: total_connections_received, total_commands_processed, keyspace_hits, keyspace_misses, keyspace_hit_ratio
      * Persistence: rdb_changes_since_last_save, aof_last_rewrite_time_sec
      * replication: role (MASTER:1, SLAVE:0), connected_slaves
      * CPU: used_cpu_sys, used_cpu_user, used_cpu_sys_children, used_cpu_user_children
      * Commandstats: cmdstat_info

     This extension also uses [SLOWLOG](https://redis.io/commands/slowlog) to fetch metrics from Redis server.
      * no_of_new_slow_logs -> This metric represents the number of new logs that were recorded as slowlogs(log queries that exceeded a specified
                               execution time) since the extension has recorded in its previous run.
        To use this metric, the "slowlog-log-slower-than" config parameter has to be set for the Redis server.

     In addition to the above metrics, there is a metric called "connectionStatus" with a value 0 when the connection to Redis server failed and 1 when the
     connection to the Redis server is successful.

## Workbench
Workbench is a feature by which you can preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.
To use the workbench, follow all the steps in installation and configuration.

1. Start the workbench with the following command if you are in &lt;MACHINE_AGENT_HOME&gt;

```
      java -jar /monitors/RedisMonitor/redis-monitoring-extension.jar 
```      
This starts an http server at http://host:9090/. This can be accessed from the browser.

2. If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.

    Get the stats:
    ```
    curl http://localhost:9090/api/stats
    ```
    Get the registered metrics:
    ```
    curl http://localhost:9090/api/metric-paths
    ```
You can make the changes to config.yml and validate it from the browser or the API

3. Once the configuration is complete, you can kill the workbench and start the Machine Agent.

## Version

|                          |           |      
|--------------------------|-----------|
|Current version           |2.0.0      |
|Redis version tested on   |3.9        |
|Last Update               |11/07/2017 |

1.0.0 - Release version

1.0.1 - Code Optimization. 

1.0.2 - Support for role metrics

1.0.3 - Revamped and Added more metrics. 

1.0.4 - JDK 1.6 compatible

1.0.5 - Added commandstats and keyspace_hit_ratio

1.0.6 - Added code fixes

1.0.7 - Fix for includePatterns

2.0.0 - Revamped the extension to support new extensions framework(2.0.0), Added new metrics like "no_of_new_slow_logs", "connectionStatus

2.0.1 - Added license, updated to extensions framework 2.0.2, SSL support, "Commandstats" metrics

## Troubleshooting
Please follow the steps specified in the [TROUBLESHOOTING](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) document to debug problems faced while using the extension.

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/redis-monitoring-extension).

## Community
Find out more in the [AppSphere](https://www.appdynamics.com/community/exchange/extension/redis-monitoring-extension/) community.

## Support
For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).
