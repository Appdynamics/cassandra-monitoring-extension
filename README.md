## AppDynamics Monitoring Extension for use with Cassandra ##
==============================
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Cassandra servers.


## Use Case ##

Apache Cassandra is an open source distributed database management system. The Cassandra monitoring extension captures statistics from the Cassandra server and displays them in the AppDynamics Metric Browser.

## Prerequisites ##

By default, cassandra starts with remote JMX enabled. In case, you have a custom script that starts Cassandra, please make sure you have the JMX parameters enabled. For more information about JMX parameters see  http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html


## Troubleshooting steps ##
Before configuring the extension, please make sure to run the below steps to check if the set up is correct.

1. Telnet into your cassandra server from the box where the extension is deployed.
       telnet <hostname> <port>

       <port> - It is the jmxremote.port specified.
        <hostname> - IP address

    If telnet works, it confirm the access to the cassandra server.


2. Start jconsole. Jconsole comes as a utitlity with installed jdk. After giving the correct host and port , check if cassandra
mbean shows up.

## Metrics Provided ##

* Cache size, capacity, hit count, hit rate, request count
* Total latency, statistics, timeout requests, unavailable requests
* Bloom filter disk space used, false positives, false ratio
* SSTables compression ratio, live tables, disk space, compacted row size
* Row size histogram
* Column count histogram
* Memtable columns, data size, switch count
* Pending tasks
* Read latency
* Write latency
* Pending and completed tasks
* Compaction tasks pending and completed
* Timeouts
* Dropped messages
* Streams
* Total disk space used
* Thread pool tasks: active, completed, blocked, pending

In addition to the above metrics, we also add a metric called "Metrics Collection Successful" with a value -1 when an error occurs and 1 when the metrics collection is successful. 

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.  
```    
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```

## Installation ##

1. Run "mvn clean install" and find the CassandraMonitor.zip file in the "target" folder. You can also download the CassandraMonitor.zip from [AppDynamics Exchange][].
2. Unzip as "CassandraMonitor" and copy the "CassandraMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`



## Configuration ##

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the cassandra instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/CassandraMonitor/`.

   For eg.
   ```
# List of cassandra servers
servers:
  - host: "localhost"
    port: 7199
    #serviceUrl:
    username: ""
    password: ""
    displayName: "Cassandra Instance 1"
    #Metric Overrides. Change this if you want to transform the metric key or value or its properties.
    #metricOverrides:
    #  - metricKey: ".*"
    #    disabled: true


# number of concurrent tasks
numberOfThreads: 10

# timeout for the thread
threadTimeout: 30

# prefix used to show up metrics in AppDynamics
#metricPathPrefix:  "Custom Metrics|Cassandra|"

metricPathPrefix: "Server|Component:8|Custom Metrics|Cassandra"

# Metric Overrides. Change this if you want to transform the metric key or value or its properties.
# metricOverrides:
#  - metricKey: ".*"
#    disabled: true

mbeans:
# mBeans for Cache Metrics
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Capacity"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Size"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Hits"
    metrics:
      include:
        - Count : "IntegralCount"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Requests"
    metrics:
      include:
        - Count : "IntegralCount"

# mBeans for ClientRequest Metrics
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Latency"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Timeouts"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Unavailables"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"

# mBeans for ColumnFamily Metrics
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=TotalDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalsePositives"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalseRatio"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=CompressionRatio"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveSSTableCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MaxRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MeanRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableColumnsCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableLiveDataSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableSwitchCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MinRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=ReadLatency"
    metrics:
      include:
        - Count : "IntegralCount"
        - Mean : "IntegralMean"
        - Max : "IntegralMax"
        - 99thPercentile : "99thPercentile"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=WriteLatency"
    metrics:
      include:
        - Count : "IntegralCount"
        - Mean : "IntegralMean"
        - Max : "IntegralMax"
        - 99thPercentile : "99thPercentile"

# mBeans for Storage Metrics
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Load"
    metrics:
      include:
        - Count : "IntegralCount"
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Exceptions"
    metrics:
      include:
        - Count : "IntegralCount"

# mBeans for ThreadPool Metrics
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=ActiveTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CompletedTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=PendingTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CurrentlyBlockedTasks"
    metrics:
      include:
        - Value : "IntegralValue"

# mBeans for CommitLog Metrics
  - objectName: "org.apache.cassandra.metrics:type=CommitLog,name=CompletedTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=PendingTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=TotalCommitLogSize"
    metrics:
      include:
        - Value : "IntegralValue"

   ```

3. MetricOverrides can be given at each server level or at the global level. MetricOverrides given at the global level will
   take precedence over server level.

   The following transformations can be done using the MetricOverrides

   a. metricKey: The identifier to identify a metric or group of metrics. Metric Key supports regex.
   b. metricPrefix: Text to be prepended before the raw metricPath. It gets appended after the displayName.
         Eg. Custom Metrics|cassandra|<displayNameForServer>|<metricPrefix>|<metricName>|<metricPostfix>

   c. metricPostfix: Text to be appended to the raw metricPath.
         Eg. Custom Metrics|cassandra|<displayNameForServer>|<metricPrefix>|<metricName>|<metricPostfix>

   d. multiplier: An integer or decimal to transform the metric value.

   e. timeRollup, clusterRollup, aggregator: These are AppDynamics specific fields. More info about them can be found
        https://docs.appdynamics.com/display/PRO41/Build+a+Monitoring+Extension+Using+Java

   f. disabled: This boolean value can be used to turn off reporting of metrics.

   # Please note that if more than one regex specified in metricKey satisfies a given metric, the metricOverride specified later will win.


4. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/CassandraMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/CassandraMonitor/config.yml" />
          ....
     </task-arguments>
    ```

### Cluster level metrics ###

As of 1.5.1+ version of this extension, we support cluster level metrics only if each node in the cluster have a separate machine agent installed on it. There are two configurations required for this setup 

1. Make sure that nodes belonging to the same cluster has the same <tier-name> in the <MACHINE_AGENT_HOME>/conf/controller-info.xml, we can gather cluster level metrics.  The tier-name here should be your cluster name. 

2. Make sure that in every node in the cluster, the <MACHINE_AGENT_HOME>/monitors/CassandraMonitor/config.yaml should emit the same metric path. To achieve this make the displayName to be empty string and remove the trailing "|" in the metricPrefix.  The config.yaml should be something as below

```
# List of cassandra servers
servers:
  - host: "localhost"
    port: 7199
    #serviceUrl:
    username: ""
    password: ""
    displayName: "Cassandra Instance 1"
    #Metric Overrides. Change this if you want to transform the metric key or value or its properties.
    #metricOverrides:
    #  - metricKey: ".*"
    #    disabled: true


# number of concurrent tasks
numberOfThreads: 10

# timeout for the thread
threadTimeout: 30

# prefix used to show up metrics in AppDynamics
# metricPathPrefix:  "Custom Metrics|Cassandra|"

metricPathPrefix: "Server|Component:8|Custom Metrics|Cassandra"

# Metric Overrides. Change this if you want to transform the metric key or value or its properties.
# metricOverrides:
#  - metricKey: ".*"
#    disabled: true

mbeans:
# mBeans for Cache Metrics
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Capacity"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Size"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Hits"
    metrics:
      include:
        - Count : "IntegralCount"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Requests"
    metrics:
      include:
        - Count : "IntegralCount"

# mBeans for ClientRequest Metrics
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Latency"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Timeouts"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Unavailables"
    metrics:
      include:
        - Count : "IntegralCount"
        - OneMinuteRate : "OneMinuteRate"

# mBeans for ColumnFamily Metrics
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=TotalDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalsePositives"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalseRatio"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=CompressionRatio"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveDiskSpaceUsed"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveSSTableCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MaxRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MeanRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableColumnsCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableLiveDataSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableSwitchCount"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MinRowSize"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=ReadLatency"
    metrics:
      include:
        - Count : "IntegralCount"
        - Mean : "IntegralMean"
        - Max : "IntegralMax"
        - 99thPercentile : "99thPercentile"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=WriteLatency"
    metrics:
      include:
        - Count : "IntegralCount"
        - Mean : "IntegralMean"
        - Max : "IntegralMax"
        - 99thPercentile : "99thPercentile"

# mBeans for Storage Metrics
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Load"
    metrics:
      include:
        - Count : "IntegralCount"
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Exceptions"
    metrics:
      include:
        - Count : "IntegralCount"

# mBeans for ThreadPool Metrics
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=ActiveTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CompletedTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=PendingTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CurrentlyBlockedTasks"
    metrics:
      include:
        - Value : "IntegralValue"

# mBeans for CommitLog Metrics
  - objectName: "org.apache.cassandra.metrics:type=CommitLog,name=CompletedTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=PendingTasks"
    metrics:
      include:
        - Value : "IntegralValue"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=TotalCommitLogSize"
    metrics:
      include:
        - Value : "IntegralValue"
```

To make it more clear,assume that Cassandra "Node A" and Cassandra "Node B" belong to the same cluster "ClusterAB". In order to achieve cluster level as well as node level metrics, you should do the following
        
1. Both Node A and Node B should have separate machine agents installed on them. Both the machine agent should have their own Cassandra extension.
    
2. In the Node A's and Node B's machine agents' controller-info.xml make sure that you have the tier name to be your cluster name , "ClusterAB" here. Also, nodeName in controller-info.xml is Node A and Node B resp.
        
3. The config.yaml for Node A and Node B should be

```
        
        # List of cassandra servers 
        servers: 
        - host: "localhost" 
        port: 7199 
        username: "" 
        password: "" 
        displayName: ""
        

        
        # number of concurrent tasks 
        numberOfThreads: 10
        
        # timeout for the thread 
        threadTimeout: 30
        
       # prefix used to show up metrics in AppDynamics
       metricPathPrefix:  "Custom Metrics|Cassandra|"

       # Metric Overrides. Change this if you want to transform the metric key or value or its properties.
       metricOverrides:
         - metricKey: ".*Ratio.*"
           postfix: "Percent"
           multiplier: 100
           disabled: false
           timeRollup: "AVERAGE"
           clusterRollup: "COLLECTIVE"
           aggregator: "SUM"


         - metricKey: ".*Cache.*Rate.*"
           postfix: "Percent"
           multiplier: 100
```      

( Note :: This extension would report a lot of metrics. If you don't want to show some metrics in your dashboard, use an exclude filter similar to the include filters used in the config.yaml. Also, by default, a Machine agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned http://docs.appdynamics.com/display/PRO14S/Metrics+Limits.)
        
Now, if Node A and Node B are reporting say a metric called ReadLatency to the controller, with the above configuration they will be reporting it using the same metric path.
        
Node A reports Custom Metrics | ClusterAB | ReadLatency = 50 
Node B reports Custom Metrics | ClusterAB | ReadLatency = 500
        
The controller will automatically average out the metrics at the cluster (tier) level as well. So you should be able to see the cluster level metrics under
        
Application Performance Management | Custom Metrics | ClusterAB | ReadLatency = 225
        
Also, now if you want to see individual node metrics you can view it under
        
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node A | ReadLatency = 50 
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node B | ReadLatency = 500



Please note that for now the cluster level metrics are obtained by the averaging all the individual node level metrics in a cluster.

##Metrics

###Cache

|**Metric Name**|**Description**|
|:-------------|:-------------|
|Capacity In Bytes|Cache capacity in bytes|
|Hits|Cache hit count|
|Hit Rate|Cache hit rate|
|Requests|Cache request count|
|Size|Cache size in bytes|

###Client Request
|**Metric Name**|**Description**|
|:-------------|:-------------|
|Latency|Latency statistics|
|Total Latency|Total latency in micro seconds|
|Timeouts|Total number of timeout requests. More precisely, total number of TimeoutException thrown|
|Unavailables|Total number of unavailable requests. More precisely, total number of UnavailableException thrown|


###Column Family
|**Metric Name**|**Description**|
|:-------------|:-------------|
|Bloom Filter Disk Space Used|Disk space used by bloom filter|
|Bloom Filter False Positives|Number of false positives for bloom filter|
|Bloom Filter False Ratio|False positive ratio of bloom filter|
|Compression Ratio|Current compression ratio for all SSTables|
|Estimated Row Size Histogram|Histogram of estimated row size (in bytes)|
|Estimated Column Count Histogram|Histogram of estimated number of columns|
|Live Disk Space Used|Disk space used by 'live' SSTables|
|Live SS Table Count|Number of 'live' SSTables|
|Max Row Size|Size of the largest compacted row|
|Mean Row Size|Mean size of compacted rows|
|Memtable Columns Count|Total number of columns present in memtable|
|Memtable Data Size|Total amount of data stored in memtable, including column-related overhead|
|Memtable Switch Count|Number of times flushing has resulted in memtable being switched out|
|Min Row Size|Size of the smallest compacted row|
|Pending Tasks|Estimated number of tasks pending|
|Read Latency|Read latency statistics|
|Read Total Latency|Total latency in micro seconds for reads|
|Recent Bloom Filter False Positives|Number of false positives since last check|
|Recent Bloom Filter False Ratio|False positive ratio since last check|
|SSTables Per Read Histogram|Histogram of the number of SSTables accessed per read|
|Total Disk Space Used|Total disk space used by SSTables, including obsolete ones waiting to be GC'd|
|Write Latency|Write latency statistics|
|Write Total Latency|Total latency for writes, in microseconds|

###Commit Log
|**Metric Name**|**Description**|
|:-------------|:-------------|
|Completed Tasks|Approximate number of completed tasks|
|Pending Tasks|Approximate number of pending tasks|
|Total Commit Log Size|Current data size of all commit log segments|


###Compaction

<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Completed Tasks </td>
<td class='confluenceTd'> Estimated number of completed compaction tasks. </td>
</tr>
<tr>
<td class='confluenceTd'> Pending Tasks </td>
<td class='confluenceTd'> Estimated number of pending compaction tasks. </td>
</tr>
<tr>
<td class='confluenceTd'> Bytes Compacted </td>
<td class='confluenceTd'> Number of bytes compacted since node started. </td>
</tr>
<tr>
<td class='confluenceTd'> Total Compactions Completed </td>
<td class='confluenceTd'> Estimated number of completed compaction tasks. </td>
</tr>
</tbody>
</table>


###Connection

<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Total Timeouts </td>
<td class='confluenceTd'> Total number of timeouts occurred for this node. </td>
</tr>
</tbody>
</table>


###Dropped Message

<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Dropped </td>
<td class='confluenceTd'> Total number of dropped message for this verb. </td>
</tr>
</tbody>
</table>


###Streaming

<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> ActiveOutboundStreams </td>
<td class='confluenceTd'> Currently active outbound streams. </td>
</tr>
<tr>
<td class='confluenceTd'> TotalIncomingBytes </td>
<td class='confluenceTd'> Total incoming bytes received since node started. </td>
</tr>
<tr>
<td class='confluenceTd'> TotalOutgoingBytes </td>
<td class='confluenceTd'> Total outgoing bytes sent since node started. </td>
</tr>
</tbody>
</table>


###Storage


<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Load </td>
<td class='confluenceTd'> Total disk space used (in bytes) for this node. </td>
</tr>
</tbody>
</table>



###Thread Pool

<table class='confluenceTable'><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Active Tasks </td>
<td class='confluenceTd'> Approximate number of tasks thread pool is actively executing. </td>
</tr>
<tr>
<td class='confluenceTd'> Completed Tasks </td>
<td class='confluenceTd'> Approximate total number of tasks thread pool has completed execution. </td>
</tr>
<tr>
<td class='confluenceTd'> Currently Blocked Tasks </td>
<td class='confluenceTd'> Number of currently blocked tasks. </td>
</tr>
<tr>
<td class='confluenceTd'> Pending Tasks </td>
<td class='confluenceTd'> Approximate number of pending tasks thread pool has. </td>
</tr>
<tr>
<td class='confluenceTd'> Total Blocked Tasks </td>
<td class='confluenceTd'> Total number of blocked tasks since node start up. </td>
</tr>
</tbody>
</table>


## Troubleshooting
1. Verify Machine Agent Data: Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. config.yml: Validate the file [here](http://www.yamllint.com/) 
3. Metric Limit: Please start the machine agent with the argument -Dappdynamics.agent.maxMetrics=5000 if there is a metric limit reached error in the logs. If you don't see the expected metrics, this could be the cause.
4. Check Logs: There could be some obvious errors in the machine agent logs. Please take a look.
5. `The config cannot be null` error.
   This usually happenes when on a windows machine in monitor.xml you give config.yaml file path with linux file path separator `/`. Use Windows file path separator `\` e.g. `monitors\MQMonitor\config.yaml` .
6. Collect Debug Logs: Edit the file, <MachineAgent>/conf/logging/log4j.xml and update the level of the appender com.appdynamics to debug Let it run for 5-10 minutes and attach the logs to a support ticket
7. While adding new metrics to your config, please make sure to use the correct object name as per JMX conventions. These can be found by checking the mBeans section of your JConsole. 

## WorkBench Mode

Workbench is a feature by which you can preview the metrics before registering them with the controller. This is useful if you wish to fine tune the configurations. Workbench is embedded into the extension jar.

Please refer to the following steps to use the Workbench mode for your extension: 

1. Install the extension using the guidelines from this document
2. Start the workbench with the command

java -jar /Path_To_MachineAgent/monitors/CassandraMonitor/cassandra-mq-monitoring-extension.jar

  This starts an http server at http://host:9090/. This can be accessed from the browser.
3. If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.

#Get the stats
curl http://localhost:9090/api/stats
#Get the registered metrics
curl http://localhost:9090/api/metric-paths
4. You can make the changes to config.yml and validate it from the browser or the API
5. Once the configuration is complete, you can kill the workbench and start the Machine Agent



## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 2.0.1
**Controller Compatibility:** 3.7+
**Cassandra Versions Tested On:** 3.0.10

[Github]: https://github.com/Appdynamics/cassandra-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com
