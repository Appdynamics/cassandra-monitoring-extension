package com.appdynamics.monitors.cassandra;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import com.appdynamics.monitors.cassandra.metrics.Metric;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class CassandraMonitor extends AManagedMonitor
{
	private MBeanServerConnection connection = null;
	private HashMap<String, Metric> cassandraMetrics = new HashMap<String, Metric>();
	private ObjectName cassandraBean = null;

	private static Logger logger = Logger.getLogger(CassandraMonitor.class);

	/**
	 * Main execution method that uploads the metrics to the AppDynamics Controller
	 * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	@Override
	public TaskOutput execute(Map<String, String> args, TaskExecutionContext arg1)
			throws TaskExecutionException
	{
		try
		{
			String host = args.get("host");
			String port = args.get("port");
			String username = args.get("user");
			String password = args.get("pass");

			connect(host, port, username, password);
			populate();

			printMetric("Uptime", 1, MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);

			for (Iterator<Entry<String, Metric>> it = cassandraMetrics.entrySet().iterator();it.hasNext();) {
				Map.Entry<String, Metric> metricMap = (Map.Entry<String, Metric>)it.next(); 
				Metric metric = metricMap.getValue();
				if (metric != null) {
					for (MBeanAttributeInfo attr : metric.attrs) {
						printMetric(
							getMetricPrefix() 
								+ metricMap.getKey()
								+ "|"
								+ metric.name
								+ "|"
								+ getTileCase(attr.getName(), true),
							connection.getAttribute(metric.bean, attr.getName()),
							MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
							MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
							MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
						);
					}
				}
			}

			return new TaskOutput("Cassandra Metric Upload Complete");

		} catch (Exception e)
		{
			logger.error(e.toString());
			return new TaskOutput("Cassandra Metric Upload Failed!");
		}
	}

	/**
	 * Connects to JMX Remote Server to access Cassandra JMX Metrics
	 * 
	 * @param 	host				Host of the remote jmx server.
	 * @param 	port				Port of the remote jmx server.
	 * @param 	username			Username to access the remote jmx server.
	 * @param 	password			Password to access the remote jmx server.
	 * @throws 	IOException			Failed to connect to server.
	 */
	public void connect(String host, String port, String username, String password)
			throws IOException
	{
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ "/jmxrmi");
		Map<String, Object> env = new HashMap<String, Object>();
		JMXConnector connector = null;
		if (!username.equals("")) {
			env.put(JMXConnector.CREDENTIALS, new String[] { username, password });
			connector = JMXConnectorFactory.connect(url, env);
		}
		else {
			connector = JMXConnectorFactory.connect(url);
		}
		connection = connector.getMBeanServerConnection();
	}

	/**
	 * Fetches all the attributes from Cassandra RegionServer JMX
	 * @throws Exception
	 */
	public void populate() throws Exception
	{
		cassandraMetrics.put(
			"Cache", 
			getStats(new String[]{
				"CapacityInBytes", 
				"Hits", 
				"HitRate", 
				"Requests", 
				"Size"
			 })
		);
		cassandraMetrics.put(
			"Client Request",
			getStats(new String[]{
				"Latency", 
				"Total latency", 
				"Timeouts", 
				"Unavailables"
			})
		);
		cassandraMetrics.put(
			"Column Family",
			getStats(new String[]{
				"BloomFilterDiskSpaceUsed",
				"BloomFilterFalsePositives",
				"BloomFilterFalseRatio",
				"CompressionRatio",
				"EstimatedRowSizeHistogram",
				"EstimatedColumnCountHistogram",
				"LiveDiskSpaceUsed",
				"LiveSSTableCount",
				"MaxRowSize",
				"MeanRowSize",
				"MemtableColumnsCount",
				"MemtableColumnsCount",
				"MemtableSwitchCount",
				"MinRowSize",
				"PendingTasks",
				"ReadLatency," +
				"ReadTotalLatency",
				"RecentBloomFilterFalsePositives",
				"RecentBloomFilterFalseRatio",
				"SSTablesPerReadHistogram",
				"TotalDiskSpaceUsed",
				"WriteLatency",
				"WriteTotalLatency"
			})
		);
		cassandraMetrics.put(
			"Commit Log",
			getStats(new String[] {
				"CompletedTasks",
				"PendingTasks",
				"TotalCommitLogSize"
			})
		);
		cassandraMetrics.put(
			"Compaction",
			getStats(new String[] {
				"CompletedTasks",
				"PendingTasks",
				"BytesCompacted",
				"TotalCompactionsCompleted"
			})
		);
		cassandraMetrics.put(
			"Connection",
			getStats(new String[] {
				"CommandPendingTasks",
				"CommandCompletedTasks",
				"CommandDroppedTasks",
				"ResponsePendingTasks",
				"ResponseCompletedTasks",
				"Timeout"
			})
		);
		cassandraMetrics.put(
			"Streaming",
			getStats(new String[] {
				"ActiveOutboundStreams",
				"TotalIncomingBytes",
				"TotalOutgoingBytes",
			})
		);
		
		cassandraMetrics.put(
			"Storage",
			getStats(new String[] {
				"Load",
			})
		);
		cassandraMetrics.put(
			"Thread Pool",
			getStats(new String[] {
				"ActiveTasks",
				"CompletedTasks",
				"CurrentlyBlockedTasks",
				"PendingTasks",
				"TotalBlockedTasks"
			})
		);
	}

	public Metric getStats(String[] metricNames) throws Exception {

		Metric metric = new Metric();
		
		for (String metricName : metricNames) {
			metric.name = getTileCase(metricName, false);
			metric.bean = cassandraBean;
			metric.attrs = connection.getMBeanInfo(cassandraBean).getAttributes();
		}

		return metric;
	}

	/**
	 * Returns the metric to the AppDynamics Controller.
	 * 
	 * @param metricName 	Name of the Metric
	 * @param metricValue 	Value of the Metric
	 * @param aggregation 	Average OR Observation OR Sum
	 * @param timeRollup 	Average OR Current OR Sum
	 * @param cluster 		Collective OR Individual
	 */
	public void printMetric(String metricName, Object metricValue, String aggregation,
			String timeRollup, String cluster)
	{
		MetricWriter metricWriter = getMetricWriter(metricName, aggregation,
				timeRollup, cluster);

		metricWriter.printMetric(String.valueOf(metricValue));
	}

	public String getTileCase(String camelCase, boolean caps) {
		String tileCase = "";
		String[] tileWords = camelCase.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (String tileWord : tileWords) {
			tileCase += tileWord + " ";
		}
		if (caps) {
			tileCase = tileCase.substring(0, 1).toUpperCase() + tileCase.substring(1);
		}
		return tileCase.trim();
	}

	/**
	 * Metric Prefix
	 * 
	 * @return Metric Location in the Controller (String)
	 */
	public String getMetricPrefix()
	{
		return "Custom Metrics|Cassandra|Status|";
	}
}