## AppDynamics Monitoring Extension for use with Cassandra ##
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Cassandra servers.
#### Use case

**Apache Cassandra** is a distributed open source database management system which can handle a large amount of data with high availability. Cassandra is a highly scalable, decentralized system and does not have a single point of failure.  

The AppDynamics Monitoring Extension for Cassandra can monitor multiple Cassandra nodes and their resources (caches, client requests etc) as well as the changes in CPU and system data as a result of using Cassandra.  

The metrics reported by the extension can be modified as per a user's requirements. The extension was built using Cassandra 3.0.10 but it would work with previous and future releases as well.

#### Prerequisites

1.  This extension requires a AppDynamics Java Machine Agent installed and running.
2.  By default, Cassandra starts with remote JMX enabled. If you have a custom script to start Cassandra, make sure the correct JMX parameters are enabled.
3.  More information on JMX parameters can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html) .

#### Installation

1.  Unzip the contents of 'CassandraMonitor'-<version>.zip file and copy the directory to `<your-machine-agent-dir>/monitors</your-machine-agent-dir>`.</version>
2.  Edit the config.yaml file. An example config.yaml file follows these installation instructions.
3.  Restart the Machine Agent.

**Sample config.yaml:** The following is a sample config.yaml file that uses one Cassandra instance to monitor data from several object instances. These metrics are customizable and can be discarded by using an 'exclude' filter, similar to the 'include' filter shown below:

<pre># List of cassandra servers
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

#timeout for the thread
threadTimeout: 30

#prefix used to show up metrics in AppDynamics
#metricPathPrefix:  "Custom Metrics|Cassandra|"

metricPathPrefix: "Server|Component:8|Custom Metrics|Cassandra"

#Metric Overrides. Change this if you want to transform the metric key or value or its properties.
#metricOverrides:
#  - metricKey: ".*"
#    disabled: true

mbeans:
#mBeans for Cache Metrics
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Capacity"
    metrics:
      include:
        - Value : "Cache Capacity (MB)"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Size"
    metrics:
      include:
        - Value : "Cache Size (MB)"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Hits"
    metrics:
      include:
        - Count : "Number of Hits"
  - objectName: "org.apache.cassandra.metrics:type=Cache,scope=*,name=Requests"
    metrics:
      include:
        - Count : "Number of Requests"

#mBeans for ClientRequest Metrics
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Latency"
    metrics:
      include:
        - Count : "Latency"
        - OneMinuteRate : "One Minute Rate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Timeouts"
    metrics:
      include:
        - Count : "Number of Timeouts"
        - OneMinuteRate : "One Minute Rate"
  - objectName: "org.apache.cassandra.metrics:type=ClientRequest,scope=*,name=Unavailables"
    metrics:
      include:
        - Count : "Number of Unavailables"
        - OneMinuteRate : "One Minute Rate"

#mBeans for ColumnFamily Metrics
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=TotalDiskSpaceUsed"
    metrics:
      include:
        - Value : "Total Disk Space Used (MB)"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterDiskSpaceUsed"
    metrics:
      include:
        - Value : "Bloom Filter Disk Space Used (MB)"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalsePositives"
    metrics:
      include:
        - Value : "Bloom Filter False Positives"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=BloomFilterFalseRatio"
    metrics:
      include:
        - Value : "Bloom Filter False Ratio"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=CompressionRatio"
    metrics:
      include:
        - Value : "Compression Ratio"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveDiskSpaceUsed"
    metrics:
      include:
        - Value : "Live Disk Space Used (MB)"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=LiveSSTableCount"
    metrics:
      include:
        - Value : "Live SS Table Count"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MaxRowSize"
    metrics:
      include:
        - Value : "Max Row Size"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MeanRowSize"
    metrics:
      include:
        - Value : "Mean Row Size"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableColumnsCount"
    metrics:
      include:
        - Value : "Number of Memtable Columns"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableLiveDataSize"
    metrics:
      include:
        - Value : "Memtable Data Size"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MemtableSwitchCount"
    metrics:
      include:
        - Value : "Memtable Switch Count"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=MinRowSize"
    metrics:
      include:
        - Value : "Min Row Size"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=ReadLatency"
    metrics:
      include:
        - Count : "Latency"
        - Mean : "Mean"
        - Max : "Max"
        - 99thPercentile : "99thPercentile"
  - objectName: "org.apache.cassandra.metrics:type=ColumnFamily,name=WriteLatency"
    metrics:
      include:
        - Count : "Latency"
        - Mean : "Mean"
        - Max : "Max"
        - 99thPercentile : "99thPercentile"

# mBeans for Storage Metrics
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Load"
    metrics:
      include:
        - Count : "System Load"
  - objectName: "org.apache.cassandra.metrics:type=Storage,name=Exceptions"
    metrics:
      include:
        - Count : "Number of Exceptions"

# mBeans for ThreadPool Metrics
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=ActiveTasks"
    metrics:
      include:
        - Value : "Number of Active Tasks"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CompletedTasks"
    metrics:
      include:
        - Value : "Number of Completed Tasks"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=PendingTasks"
    metrics:
      include:
        - Value : "Number of Pending Tasks"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,path=*,scope=*,name=CurrentlyBlockedTasks"
    metrics:
      include:
        - Value : "Number of Currently Blocked Tasks"

# mBeans for CommitLog Metrics
  - objectName: "org.apache.cassandra.metrics:type=CommitLog,name=CompletedTasks"
    metrics:
      include:
        - Value : "Number of Completed Tasks"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=PendingTasks"
    metrics:
      include:
        - Value : "Number of Pending Tasks"
  - objectName: "org.apache.cassandra.metrics:type=ThreadPools,name=TotalCommitLogSize"
    metrics:
      include:
        - Value : "Total Commit Log Size (MB)"

#mBeans for Memory and System Metrics
  - objectName: "java.lang:type=OperatingSystem"
    metrics:
      include:
        - FreePhysicalMemorySize : "Free Physical Memory (MB)"
        - TotalPhysicalMemorySize : "Total Physical Memory (MB)"
        - SystemCpuLoad : "Total CPU System Load"
        - ProcessCpuLoad : "Process CPU Load"
        - AvailableProcessors : "Number of Available Processors"
        - SystemLoadAverage : "System Load Average"
  - objectName: "java.lang:type=Runtime"
    metrics:
      include:
        - Uptime : "System Uptime"
  - objectName: "java.lang:type=Threading"
    metrics:
      include:
        - PeakThreadCount : "Peak Thread Count"
        - DaemonThreadCount : "Daemon Thread Count"
        - TotalStartedThreadCount : "Total Started Thread Count"
</pre>

#### Metrics

The metrics will be reported under the tree `Application Infrastructure Performance|$TIER|Custom Metrics|Cassandra`. Instructions on how to report metrics to one specific tier can be found in the 'Metric Path' subtopic on [this](https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java) page.

#### Troubleshooting

Please check the `<machine-agent-dir>/logs/machine-agent.log*</machine-agent-dir>` for troubleshooting

1.  **Verify Machine Agent Data:** Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2.  **config.yaml:** Validate the file [here.](http://www.yamllint.com/)
3.  **The config cannot be null :**  
    This usually happens when on a windows machine in monitor.xml you give config.yaml file path with linux file path separator `/`. Use Windows file path separator `\` e.g. `monitors\CassandraMonitor\config.yaml` .
4.  **Metric Limit:** Please start the machine agent with the argument -Dappdynamics.agent.maxMetrics=5000 if there is a metric limit reached error in the logs. If you don't see the expected metrics, this could be the cause.
5.  **Debug Logs:** Edit the file, `<machine-agent-dir>/conf/logging/log4j.xml</machine-agent-dir>` and update the level of the appender `com.appdynamics` to debug . Let it run for 5-10 minutes and attach the logs to a support ticket
6.  **JMX Object Names :** While adding your own instances or metrics to the config.yamk, please make sure that the `objectName` matches the name for the metric of interest in JConsole. If you don't see any metrics that you have configured, this might be a probable cause.

#### Password Encryption Support

To avoid setting the clear text password in the config.yml, please follow the process to encrypt the password and set the encrypted password and the encryptionKey in the config.yml

1.  To encrypt password from the commandline go to <ma_dir>/monitors/CassandraMonitor dir and run the below common

    <pre>java -cp "cassandra-monitoring-extension.jar" com.appdynamics.extensions.crypto.Encryptor myKey myPassword</pre>

    </ma_dir>

#### Workbench

Workbench is a feature by which you can preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.  
To use the workbench

1.  Follow all the installation steps
2.  Start the workbench with the command

    <pre>      java -jar <machine-agent-dir>/monitors/CassandraMonitor/cassandra-monitoring-extension.jar</machine-agent-dir> </pre>

    This starts an http server at http://host:9090/. This can be accessed from the browser.
3.  If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.

    <pre>#Get the stats
        curl http://localhost:9090/api/stats
        #Get the registered metrics
        curl http://localhost:9090/api/metric-paths
    </pre>

4.  You can make the changes to config.yml and validate it from the browser or the API
5.  Once the configuration is complete, you can kill the workbench and start the Machine Agent.

#### Support

Please contact [help@appdynamics.com](mailto:help@appdynamics.com) with the following details

1.  config.yml
2.  debug logs

#### Compatibility

<table border="0" cellpadding="0">

<tbody>

<tr>

<td style="text-align: right; width: 210px">Version</td>

<td>2.1</td>

</tr>

<tr>

<td style="text-align: right">Machine Agent Compatibility</td>

<td>4.0+</td>

</tr>

<tr>

<td style="text-align: right">Last Update</td>

<td>11/19/19</td>

</tr>

</tbody>

</table>

#### Codebase

You can contribute your development ideas [here.](https://github.com/Appdynamics/websphere-mq-monitoring-extension)
