# AppDynamics Apache Cassandra Monitoring Extension

-   [Use Case](#Cassandra-use-case)
-   [Files](#Cassandra-FilesFolders)
-   [Installation](#Cassandra-InstallationSteps)
-   [Rebuilding the Project](#Cassandra-RebuildingProject)
-   [Configuration](#Cassandra-Configuration)
     *    [Example Monitor XML](#Cassandra-ExampleMonitorXML)
-   [Metrics](#Cassandra-Metrics)
     *    [Cache](#Cassandra-Cache)
     *    [Client Request](#Cassandra-ClientRequest)
     *    [Column Family](#Cassandra-ColumnFamily)
     *    [Commit Log](#Cassandra-CommitLog)
     *    [Compaction](#Cassandra-Compaction)
     *    [Connection](#Cassandra-Connection)
     *    [Dropped Message](#Cassandra-DroppedMessage)
     *    [Streaming](#Cassandra-Streaming)
     *    [Storage](#Cassandra-Storage)
     *    [Thread Pool](#Cassandra-ThreadPool)
-   [Contributing](#Cassandra-Contributing)
-   [Support](#Cassandra-Support)

##<a name = "Cassandra-use-case"></a>Use Case

Apache Cassandra is an open source distributed database management system.
The Cassandra monitoring extension captures statistics from the Cassandra server
and displays them in the AppDynamics Metric Browser.

Metrics include:

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


##<a name = "Cassandra-FilesFolders"></a>Files

Files/Folders Included:

|**Directory/File** | **Description**|
| ------------- |:-------------|
|conf|Contains the monitor.xml|
|lib|Contains Third-party project references|
|src|Contains source code to Cassandra Custom Monitor|
|dist|Only obtained when using ant. Run 'ant build' to get binaries. Run 'ant package' to get the distributable .zip file|
|build.xml|Ant build script to package the project (required only if changing Java code)|



![](images/emoticons/information.gif)
Main Java File:
src/com/appdynamics/monitors/cassandra/CassandraMonitor.java  -\> This
file contains the metric parsing and printing.

##<a name = "Cassandra-Installation"></a>Installation

1. Run 'ant package' from the cassandra-monitoring-extension directory
2. Deploy the file CassandraMonitor.zip found in the 'dist' directory into \<machineagent install dir\>/monitors/
3. Unzip the deployed file
4. Open \<machineagent install dir\>/monitors/CassandraMonitor/monitor.xml and configure the Cassandra credentials
5. Restart the machineagent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Cassandra | Status


##<a name = "Cassandra-Configuration"></a>Configuration


|**Parameter** | **Description**|
| ------------- |:-------------|
|Host|Mongo DB Host|
|Port|Mongo DB Port|
|User|Username to access cassandra jmx server|
|Pass|Password to access cassandra jmx server|

###<a name = "Cassandra-ExampleMonitorXML"></a>Example Monitor XML
```
<monitor>
        <name>CassandraMonitor</name>
        <type>managed</type>
        <description>Cassandra monitor</description>
        <monitor-configuration></monitor-configuration>
        <monitor-run-task>
                <execution-style>periodic</execution-style>
                <execution-frequency-in-seconds>60</execution-frequency-in-seconds>
                <name>Cassandra Monitor Run Task</name>
                <display-name>Cassandra Monitor Task</display-name>
                <description>Cassandra Monitor Task</description>
                <type>java</type>
                <execution-timeout-in-secs>60</execution-timeout-in-secs>
                <task-arguments>
                        <argument name="host" is-required="true" default-value="localhost" />
                        <argument name="port" is-required="true" default-value="80" />
                        <argument name="user" is-required="true" default-value="username" />
                        <argument name="pass" is-required="true" default-value="password" />
                </task-arguments>
                <java-task>
                        <classpath>CassandraMonitor.jar</classpath>
                        <impl-class>com.appdynamics.monitors.cassandra.CassandraMonitor</impl-class>
                </java-task>
        </monitor-run-task>
</monitor>

```

##<a name = "Cassandra-Metrics"></a>Metrics

###<a name = "Cassandra-Cache"></a>Cache

|**Metric Name**|**Description**|
|:-------------|:-------------|
|Capacity In Bytes|Cache capacity in bytes|
|Hits|Cache hit count|
|Hit Rate|Cache hit rate|
|Requests|Cache request count|
|Size|Cache size in bytes|

###<a name = "Cassandra-ClientRequest"></a>Client Request
|**Metric Name**|**Description**|
|:-------------|:-------------|
|Latency|Latency statistics|
|Total Latency|Total latency in micro seconds|
|Timeouts|Total number of timeout requests. More precisely, total number of TimeoutException thrown|
|Unavailables|Total number of unavailable requests. More precisely, total number of UnavailableException thrown|


###<a name = "Cassandra-ColumnFamily"></a>Column Family
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

###<a name = "Cassandra-CommitLog"></a>Commit Log
|**Metric Name**|**Description**|
|:-------------|:-------------|
|Completed Tasks|Approximate number of completed tasks|
|Pending Tasks|Approximate number of pending tasks|
|Total Commit Log Size|Current data size of all commit log segments|


###<a name = "Cassandra-Compaction"></a>Compaction

<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
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


###<a name = "Cassandra-Connection"></a>Connection

<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Total Timeouts </td>
<td class='confluenceTd'> Total number of timeouts occurred for this node. </td>
</tr>
</tbody>
</table>


###<a name = "Cassandra-DroppedMessage"></a>Dropped Message

<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Dropped </td>
<td class='confluenceTd'> Total number of dropped message for this verb. </td>
</tr>
</tbody>
</table>


###<a name = "Cassandra-Streaming"></a>Streaming

<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
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


###<a name = "Cassandra-Storage"></a>Storage


<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Load </td>
<td class='confluenceTd'> Total disk space used (in bytes) for this node. </td>
</tr>
</tbody>
</table>



###<a name = "Cassandra-ThreadPool"></a>Thread Pool

<table class='confluenceTable'><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
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


##<a name = "Cassandra-Contributing"></a>Contributing


Always feel free to fork and contribute any changes directly via GitHub.


##<a name = "Cassandra-Support"></a>Support

For any support questions, please contact ace@appdynamics.com.
