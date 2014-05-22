cassandra-monitoring-extension
==============================
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Cassandra servers.


## Use Case ##

Apache Cassandra is an open source distributed database management system. The Cassandra monitoring extension captures statistics from the Cassandra server and displays them in the AppDynamics Metric Browser.

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


## Installation ##

1. Run "mvn clean install" and find the CassandraMonitor.zip file in the "target" folder. You can also download the CassandraMonitor.zip from [AppDynamics Exchange][].
2. Unzip as "CassandraMonitor" and copy the "CassandraMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`



## Configuration ##
1. Configure the cassandra instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/CassandraMonitor/`.
2. Configure the MBeans in the config.yml. By default, "org.apache.cassandra.metrics" is all that you may need. But you can add more mbeans as per your requirement.
   You can also add excludePatterns (regex) to exclude any metric tree from showing up in the AppDynamics controller.

   For eg.
   ```
        # List of cassandra servers
        servers:
          - host: "localhost"
            port: 7199
            username: ""
            password: ""
            displayName: "localhost"


        # cassandra mbeans. Exclude patterns with regex can be used to exclude any unwanted metrics.
        mbeans:
          - domainName: "org.apache.cassandra.metrics"
            excludePatterns: [
              "Cache|.*",
              "ClientRequest|RangeSlice|.*",
              "Client|connectedNativeClients",
              "ColumnFamily|system|IndexInfo|.*"
            ]

          - domainName: "org.apache.cassandra.db"

        # number of concurrent tasks
        numberOfThreads: 10

        #timeout for the thread
        threadTimeout: 30

        #prefix used to show up metrics in AppDynamics
        metricPrefix:  "Custom Metrics|Cassandra|"

   ```
   In the above config file, metrics are being pulled from two mbean domains. Note that the patterns mentioned in the "excludePatterns" will be excluded from showing up in the AppDynamics dashboard.


3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/CassandraMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/CassandraMonitor/config.yml" />
          ....
     </task-arguments>
    ```

    ###Note### : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a yaml validator http://yamllint.com/

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



## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.0
**Controller Compatibility:** 3.7+

[Github]: https://github.com/Appdynamics/cassandra-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com