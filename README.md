# Redis Monitoring Extension

This extension works only with the standalone machine agent.

## Use Case
Redis is an in memory key-value data store. The Redis monitoring extension gathers Redis server statistics and display them in AppDynamics Metric Browser.

##Installation
1. Run 'mvn clean install' from the redis-monitoring-extension directory and find the RedisMonitor.zip in the "target" folder.
2. Unzip as "RedisMonitor" and copy the "RedisMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`

## Configuration ##
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Redis instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/RedisMonitor/`. Specify the host, port of the Redis instance, password if authentication enabled and keyspaces.
   You can also add excludePatterns (regex) to exclude metrics from showing up in the AppDynamics controller.

   For eg.
   ```
        # List of Redis servers
        servers:
          - host: "localhost"
            port: 6379
            password: "admin"
            displayName: "localhost"
            includePatterns: [
                       ]
            excludePatterns: [
                              .*Persistence|.*
                             ]
        #prefix used to show up metrics in AppDynamics
        metricPrefix:  "Custom Metrics|Redis|"

   ```

3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/RedisMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/RedisMonitor/config.yml" />
          ....
     </task-arguments>
    ```

## Metrics
This extension uses [INFO](http://redis.io/commands/info) command to fetch metrics from Redis server.
Some of the metrics are listed below.
* Server: uptime_in_seconds, uptime_in_days
* clients: blocked_clients, connected_clients
* memory: mem_fragmentation_ratio, used_memory, used_memory_peak, used_memory_rss
* replication: role (MASTER:1, SLAVE:0),  master_link_status(UP:1, DOWN:0), connected_slaves, master_last_io_seconds_ago, master_link_down_since_seconds, master_sync_in_progress
* CPU
* Persistence
* Stats
* Keyspace: keys, expires
* commandstats: cmdstat_set, cmdstat_info, cmdstat_subscribe
* keyspace_hit_ratio = keyspace_hits/(keyspace_hits + keyspace_misses)
In addition to the above metrics, we also add a metric called "Metrics Collection Successful" with a value 0 when an error occurs and 1 when the metrics collection is successful.

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.  
```    
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```

## Custom Dashboard
![](https://raw.github.com/Appdynamics/redis-monitoring-extension/master/RedisDashboard.PNG)

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/redis-monitoring-extension).

##Community
Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/eXchange/Redis---Monitoring-Extension/idi-p/4505) community.

##Support
For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).

