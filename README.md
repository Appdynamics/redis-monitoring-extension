RedisMonitor
============

AppDynamics Machine Agent plugin to monitor a Redis server.

# Installation

Unzip RedisMonitor.zip into your machine agent's `monitors` folder. Edit the `monitor.xml` file and set the
task arguments to connect to your Redis server. 

``` xml
            <argument name="host" is-required="false" default-value="localhost" />
            <argument name="port" is-required="false" default-value="6379" />
            <argument name="password" is-required="false" default-value="" />
```

You can also set the `keyspaces` argument to a comma-separated list of keyspaces, and the plugin will report the
number of keys and number of expired keys:

``` xml
            <argument name="keyspaces" is-required="false" default-value="db0,db1" />
```
