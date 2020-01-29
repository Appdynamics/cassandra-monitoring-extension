# AppDynamics Monitoring Extension for use with Cassandra ##
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Cassandra servers.
## Use case

**Apache Cassandra** is a distributed open source database management system which can handle a large amount of data with high availability. Cassandra is a highly scalable, decentralized system and does not have a single point of failure.  

The AppDynamics Monitoring Extension for Cassandra can monitor multiple Cassandra nodes and their resources (caches, client requests etc) as well as the changes in CPU and system data as a result of using Cassandra.  

The metrics reported by the extension can be modified as per a user's requirements. The extension was built using Cassandra 3.11 but it would work with previous and future releases as well.

## Prerequisites
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.
2.  By default, Cassandra starts with remote JMX enabled. If you have a custom script to start Cassandra, make sure the correct JMX parameters are enabled.
3.  More information on JMX parameters can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html) .

## Installation
1.  Unzip the contents of 'CassandraMonitor'-<version>.zip file and copy the directory to `<your-machine-agent-dir>/monitors</your-machine-agent-dir>`.</version>
2.  Please place the extension in the "monitors" directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.
3.  Edit the config.yml file. An example config.yml file follows these installation instructions.
4.  Restart the Machine Agent.
5. The extension needs to be able to connect to Cassandra in order to collect and send metrics. To do this, you will have to either establish a remote JMX connection in between the extension and the product in order for the extension to collect and send the metrics.
## Configuration

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the JMX connection parameters by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/CassandraMonitor/`. 

2. There are a few fields that you need to make sure are filled in correctly. 
Once done with them, they should allow you to establish a successful connection
 with your server. They are : 
    ```
       servers:
       -  displayName: ""
          host: ""
          port:
       #      serviceUrl: ""
          username: ""
          password: ""
          #encryptedPassword: ""
    ```

   * displayName: This will be the name of your server that you would like to see on the metric browser.
   * host: This is the HostURL that is used with a port to create a connection with the JMX Server.
   * serviceUrl: This is the full URL with host and port that is used to establish a connection. 

   **You should either use HOST AND PORT or just the SERVICEURL in order to establish a connection.**

   * username: List the username, if any, that is needed to establish a connection.
   * password: List the password associated with the username that is needed to establish a connection.
   * encryptedPassword: In case you would like to use an encrypted password, use this field.
   
 3. Configure the encyptionKey for encryptionPasswords(only if password encryption required).
    
    For example,
    ```
    #Encryption key for Encrypted password.
    encryptionKey: "axcdde43535hdhdgfiniyy576"
    ```
   * encryptionKey: If you use an encryptedPassword, please provide the key here as well in order for the system to decrypt your password.

   **You should either use the Normal PASSWORD or the encryptedPassword and encryptionKey in order to establish a connection. Please read the "credentials Encryption" section below to find more information on Password Encryption.**

 4. Configure the **numberOfThreads**
    For example,
    If number of servers that need to be monitored is 3, then number of threads required is 5 * 3 = 15
     ```
        numberOfThreads: 15
     ```  
5. The metricPrefix of the extension has to be configured as [specified here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695#Configuring%20an%20Extension). Please make sure that the right metricPrefix is chosen based on your machine agent deployment, otherwise this could lead to metrics not being visible in the controller.
Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER NAME OR TIER ID>` in
     metricPrefix: "Server|Component:`<TIER NAME OR TIER ID>`|Custom Metrics|Cassandra Monitor". For example,
    
```
     metricPrefix: "Server|Component:Extensions tier|Custom Metrics|Cassandra Monitor"
```
## Metrics

You can use this extension to get all metrics that are available through the JMX Messaging service. In order to do so though, you will have to make sure that all metrics are defined correctly.
Please follow the next few steps in order to get this right.
1. You will have to list each mBean separately in the config.yml file. 
For each mBean you will have to add an **objectName**, **mbeanKeys** and **metrics** tag.
The following example shows exactly how you should do that. 
    * You will have to each and every **mBeanKey** that is listed in the **objectName**.
    * Under **metrics** is where you have the ability to include all the metrics that you would like to monitor.

    <pre>
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
    </pre>

2. There are several properties that are associated with each metric. They are: 
    * alias
    * aggregationType
    * timeRollUpType
    * clusterRollUpType
    * multiplier
    * convert
    * delta
   
   This format enables you to change some of the metric properties from what the default configurations are.

    In Order to use them for each metric, please use the following example.
    ```
      - objectName: "org.apache.cassandra:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"
        mbeanKeys: ["type", "brokerName","destinationType","destinationName"]
        metrics:
          include:
            - name: "AverageEnqueueTime"
              alias: "Average Enqueue Time"
              clusterRollUpType: "AVERAGE"
              timeRollUpType: "SUM"
              aggregationType: "SUM"
    ```
3. This extension can  be used to get values from **composite objects**. 
In order to do so, you have to list the metric name as is and then specify the path with a **"|"** followed my the composite attribute.
In this example we see that HeapMemoryUsage is a composite object that has 4 values associated with it. 
Now in order to monitor them, you list the property and then in the alias name, add the **"|"** followed by the attribute name in order to get all of the attributes associated with HeapMemoryUsage under one folder in the metric browser.

    ```
      - objectName: "java.lang:type=Memory"
        mbeanKeys: ["type"]
        metrics:
          include:
            - name: "HeapMemoryUsage.committed"
              alias: "Heap Memory Usage|Committed"
            - name: "HeapMemoryUsage.used"
              alias: "Heap Memory Usage|Used"
    ```

4. This extension can be used to get values from Map Objects as well. 
To do so, list the metrics you would like to retrieve from the map in the following manner. 
     ```
      - objectName: "java.lang:type=Memory"
        mbeanKeys: ["type"]
        metrics:
          include:
             # Map Metric Level 1
             - name: "MapOfString.key1"
               alias: "Map 1|Key 1"
             - name: "MapOfString.key2"
               alias: "Map 1|Key 2"
     
             # Map Metric Level 2
             - name: "MapOfString.map2.key2"
               alias: "Map 1|Map 2|Key 2"
     
             # Map Metric Level 3
             - name: "MapOfString.map2.map3.key32"
               alias: "Map 1|Map 2|Map 3|Key 32"
               multiplier: "20"
               delta: false
               aggregationType: "OBSERVATION"
               timeRollUpType: "AVERAGE"
               clusterRollUpType: "INDIVIDUAL"
               convert : {
                 "ENDANGERED" : "1",
                 "NODE-SAFE" : "2",
                 "MACHINE-SAFE" : "3"
               }
    ```
    
5. This extension can be used to get data from List Objects as well. 
    To do so, the metric in the list should be separated with a separator such as a ":" and should be in a key value pair form.
    If your metric is not in the form listed about, the extension will not be able to extract that data. 

    ``` 
     - objectName: "java.lang:type=Memory"
        mbeanKeys: ["type"]
        metrics:
          include:
            # List Metrics Can be set in the following ways:
            - name: "listOfString.metric one"
              alias: "listOfString|metric one"
            - name: "listOfString.metric two"
              alias: "listOfString|metric two"
            - name: "listOfString.metric three" 
              alias: "listOfString|metric three"
    ```    

## metricPathReplacements
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/Metric-Path-CharSequence-Replacements-in-Extensions/ta-p/35412) page to get detailed instructions on configuring Metric Path Character sequence replacements in Extensions.
    

### Credentials Encryption

Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

### Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

### Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

### Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

    1. Stop the running machine agent.
    2. Delete all existing logs under <MachineAgent>/logs.
    3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
        <logger name="com.singularity">
        <logger name="com.appdynamics">
    4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
    5. Attach the zipped <MachineAgent>/conf/* directory here.
    6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.

For any support related questions, you can also contact help@appdynamics.com.



### Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/cassandra-monitoring-extension/).

### Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.1       |
|Controller Compatibility  |3.7 or Later|
|Product Tested On         |3.1|
|Last Update               |12/10/2019|

[How to use the Extensions WorkBench ]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130
[Changelog.md]: https://github.com/Appdynamics/cassandra-monitoring-extension/blob/master/Changelog.md
[Encryption Guidelines]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397
[troubleshooting-document]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695
[AppDynamics Exchange]: https://www.appdynamics.com/community/exchange/extension/cassandra-monitoring-extension/
[GitHub]: https://github.com/Appdynamics/cassandra-monitoring-extension