
package com.appdynamics.monitors.cassandra;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

/**
 * Cassandra Monitoring Extension.
 */
public class CassandraMonitor extends AManagedMonitor
{
    private static final String CAMEL_CASE_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
    private static Logger logger = Logger.getLogger(CassandraMonitor.class);
    private static final String CASSANDRA_METRICS_OBJECT = "org.apache.cassandra.metrics";
    private static final String CUSTOM_METRICS_CASSANDRA_STATUS = "Custom Metrics|Cassandra|Status";

    private MBeanServerConnection connection = null;
    private final HashMap<String, Object> cassandraMetrics = new HashMap<String, Object>();
    private final Collection<String> filters = new HashSet<String>();

    /**
     * Connects to JMX Remote Server to access Cassandra JMX Metrics
     * 
     * @param   host                Host of the remote jmx server.
     * @param   port                Port of the remote jmx server.
     * @param   username            Username to access the remote jmx server.
     * @param   password            Password to access the remote jmx server.
     * @throws  IOException         Failed to connect to server.
     */
    public void connect(final String host, final String port, final String username, final String password)
        throws IOException
    {
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        final Map<String, Object> env = new HashMap<String, Object>();
        JMXConnector connector = null;

        if (!"".equals(username)) {
            env.put(JMXConnector.CREDENTIALS, new String[] { username, password });
            connector = JMXConnectorFactory.connect(url, env);
        }
        else {
            connector = JMXConnectorFactory.connect(url);
        }

        this.connection = connector.getMBeanServerConnection();
    }

    /**
     * Populates the metrics by iterating through properties of given mbean name (domain).
     */
    public void populateMetrics(final String mbeanDomain)
    {
        try {
            // Get all the m-beans registered.
            final Set<ObjectInstance> queryMBeans = this.connection.queryMBeans(null, null);

            // Iterate through each of them available.
            for (final ObjectInstance mbean : queryMBeans) {

                // Get the canonical name
                final String canonicalName = mbean.getObjectName().getCanonicalName();

                // See if its the one we want to gather metrics from.
                // If the 'domain' name is not supplied then, 
                // the m-bean "org.apache.cassandra.metrics" would be used.
                if (canonicalName.startsWith(mbeanDomain)) {
                    final ObjectName objectName = mbean.getObjectName();

                    // Fetch all attributes.
                    final MBeanAttributeInfo[] attributes = this.connection.getMBeanInfo(objectName).getAttributes();
                    for (final MBeanAttributeInfo attr : attributes) {

                        // See we do not violate the security rules, i.e. only if the attribute is readable.
                        if (attr.isReadable()) {
                            // Collect the statistics.
                            final Object attribute = this.connection.getAttribute(objectName, attr.getName());
                            final String[] split = canonicalName.substring(canonicalName.indexOf(':') + 1).split(",");
                            String type = null, keySpace = null, scope = null, name = null;

                            // Form the AppDynamics Controller UI's path to show it.
                            for (final String token : split) {
                                final String[] keyValuePairs = token.split("=");

                                // Standard jmx attributes. {type, scope, name, keyspace, etc.}
                                if ("type".equalsIgnoreCase(keyValuePairs[0])) {
                                    type = keyValuePairs[1];
                                }
                                else if ("scope".equalsIgnoreCase(keyValuePairs[0])) {
                                    scope = keyValuePairs[1];
                                }
                                else if ("name".equalsIgnoreCase(keyValuePairs[0])) {
                                    name = keyValuePairs[1];
                                }
                                else if ("keyspace".equalsIgnoreCase(keyValuePairs[0])) {
                                    keySpace = keyValuePairs[1];
                                }
                            }

                            if (null != attr.getName()) {
                                // Get the metrics name tiled.
                                final String attributeNameTiled = getTileCase(attr.getName(), false);

                                // If not it is to be filtered add it to the metrics.
                                if (!filters.contains(attributeNameTiled)) {
                                    String metricsKey = getMetricPrefix()
                                        + ((isNotEmpty(type)) ? ("|" + getTileCase(type, false)) : "")
                                        + ((isNotEmpty(keySpace)) ? ("|" + getTileCase(keySpace, false)) : "")
                                        + ((isNotEmpty(scope)) ? ("|" + getTileCase(scope, false)) : "")
                                        + ((isNotEmpty(name)) ? ("|" + getTileCase(name, false)) : "")
                                        + ("|" + attributeNameTiled);

                                    // Put the path and the value in case every thing is okay and not null.
                                    cassandraMetrics.put(metricsKey, attribute);
                                    System.out.println(metricsKey + "{" + String.valueOf(attribute) + "}");
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Collecting statistics failed.", e);
        }
    }

    /**
     * @param input
     * @return
     */
    public static boolean isNotEmpty(final String input)
    {
        return null != input && !"".equals(input.trim());
    }

    /**
     * Main execution method that uploads the metrics to the AppDynamics Controller
     * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    @Override
    public TaskOutput execute(final Map<String, String> args, final TaskExecutionContext arg1)
        throws TaskExecutionException
    {
        try {
            final String host = args.get("host");
            final String port = args.get("port");
            final String username = args.get("user");
            final String password = args.get("pass");
            final String filter = args.get("filter");
            String mBeanDomain = args.get("mbean");

            parseFilters(filter);
            mBeanDomain = (null != mBeanDomain) ? mBeanDomain : CASSANDRA_METRICS_OBJECT;

            // Obtain JMX connection.
            connect(host, port, username, password);

            // Populate the metrics.
            populateMetrics(mBeanDomain);

            // Send it to controller.
            printMetric(
                "Uptime", 1, MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);

            for (Iterator<Entry<String, Object>> it = cassandraMetrics.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, Object> metricMap = (Map.Entry<String, Object>) it.next();

                printMetric(
                    metricMap.getKey(), metricMap.getValue(), MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
            }

            return new TaskOutput("Cassandra Metric Upload Complete");
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new TaskOutput("Cassandra Metric Upload Failed!");
        }
    }

    /**
     * @param filter
     */
    private void parseFilters(final String filter)
    {
        if (isNotEmpty(filter)) {
            final String[] split = filter.split(",");
            filters.clear();

            for (final String token : split) {
                filters.add(token.trim());
            }
        }
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
    public void printMetric(
        final String metricName,
        final Object metricValue,
        final String aggregation,
        final String timeRollup,
        final String cluster)
    {
        MetricWriter metricWriter = getMetricWriter(metricName, aggregation, timeRollup, cluster);
        metricWriter.printMetric(String.valueOf(metricValue));
    }

    /**
     * @param camelCase
     * @param caps
     * @return
     */
    public String getTileCase(final String camelCase, final boolean caps)
    {
        if (-1 == camelCase.indexOf('_')) {
            return _getTileCase(camelCase, CAMEL_CASE_REGEX);
        }
        else {
            return _getTileCase(camelCase, "_");
        }
    }

    /**
     * @param camelCase
     * @param regEx
     * @param caps
     * @return
     */
    public String _getTileCase(final String camelCase, final String regEx)
    {
        String tileCase = "";
        String[] tileWords = camelCase.split(regEx);

        for (String tileWord : tileWords) {
            tileCase += Character.toUpperCase(tileWord.charAt(0)) + tileWord.substring(1) + " ";
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
        return CUSTOM_METRICS_CASSANDRA_STATUS;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        CassandraMonitor monitor = new CassandraMonitor();
        monitor.connect("localhost", "7199", "", "");
        monitor.populateMetrics(CASSANDRA_METRICS_OBJECT);
    }
}
