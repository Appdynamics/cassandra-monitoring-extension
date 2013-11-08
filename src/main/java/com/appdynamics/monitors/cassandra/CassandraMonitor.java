package com.appdynamics.monitors.cassandra;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cassandra Monitoring Extension.
 */
public class CassandraMonitor extends AManagedMonitor {
    private static Logger logger = Logger.getLogger(CassandraMonitor.class);

    /**
     * Connects to JMX Remote Server to access Cassandra JMX Metrics
     *
     * @param   host                Host of the remote jmx server.
     * @param   port                Port of the remote jmx server.
     * @param   username            Username to access the remote jmx server.
     * @param   password            Password to access the remote jmx server.
     * @throws IOException         Failed to connect to server.
     */

    /**
     * Populates the metrics by iterating through properties of given mbean name (domain).
     */

    /**
     * @param input
     * @return
     */
    private static boolean isNotEmpty(final String input) {
        return input != null && !"".equals(input.trim());
    }

    /**
     * Main execution method that uploads the metrics to the AppDynamics Controller
     *
     * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(final Map<String, String> args, final TaskExecutionContext arg1)
            throws TaskExecutionException {
        try {
            List<Credential> credentials = new ArrayList<Credential>();
            Credential credential = new Credential();

            credential.dbname = args.get("dbname");
            credential.host = args.get("host");
            credential.port = args.get("port");
            credential.username = args.get("user");
            credential.password = args.get("pass");
            credential.filter = args.get("filter");
            credential.mBeanDomain = args.get("mbean");

            if (!isNotEmpty(credential.dbname)) {
                credential.dbname = "DB_1";
            }

            credentials.add(credential);

            String xmlPath = args.get("properties-path");
            if (xmlPath != null && !xmlPath.isEmpty()) {
                try {
                    SAXReader reader = new SAXReader();
                    Document doc = reader.read(xmlPath);
                    Element root = doc.getRootElement();

                    for (Element credElem : (List<Element>) root.elements("credentials")) {
                        Credential cred = new Credential();
                        cred.dbname = credElem.elementText("dbname");
                        cred.host = credElem.elementText("host");
                        cred.port = credElem.elementText("port");
                        cred.username = credElem.elementText("username");
                        cred.password = credElem.elementText("password");
                        cred.mBeanDomain = credElem.elementText("mbean");
                        cred.filter = credElem.elementText("filter");

                        if (isNotEmpty(cred.host) && isNotEmpty(cred.port)) {
                            if (!isNotEmpty(cred.dbname)) {
                                cred.dbname = "DB_" + (credentials.size() + 1);
                            }
                            credentials.add(cred);
                        }
                    }
                } catch (DocumentException e) {
                    logger.error("Cannot read '" + xmlPath + "'. Monitor is running without additional credentials");
                }
            }

            ExecutorService executor = Executors.newFixedThreadPool(credentials.size());

            try {
                CompletionService<Map<String, Object>> threadPool =
                        new ExecutorCompletionService<Map<String, Object>>(executor);

                for (Credential cred : credentials) {
                    threadPool.submit(new CassandraCommunicator(cred.dbname, cred.host, cred.port,
                            cred.username, cred.password, cred.filter, cred.mBeanDomain, logger));
                }

                for (int i = 0; i < credentials.size(); i++) {
                    Map<String, Object> metrics = threadPool.take().get();
                    if (metrics != null) {
                        String dbname = (String) metrics.remove(CassandraCommunicator.DBNAME_KEY);

                        printMetric(CassandraCommunicator.getMetricPrefix() + "|" + dbname + "|Uptime", 1,
                                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);

                        for (final Entry<String, Object> metricMap : metrics.entrySet()) {
                            printMetric(metricMap.getKey(), metricMap.getValue(),
                                    MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
                        }
                    }
                }
            } finally {
                executor.shutdown();
            }


            return new TaskOutput("Cassandra Metric Upload Complete");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new TaskOutput("Cassandra Metric Upload Failed!");
        }
    }

    /**
     * Returns the metric to the AppDynamics Controller.
     *
     * @param metricName  Name of the Metric
     * @param metricValue Value of the Metric
     * @param aggregation Average OR Observation OR Sum
     * @param timeRollup  Average OR Current OR Sum
     * @param cluster     Collective OR Individual
     */
    private void printMetric(
            final String metricName,
            final Object metricValue,
            final String aggregation,
            final String timeRollup,
            final String cluster) {
        MetricWriter metricWriter = getMetricWriter(metricName, aggregation, timeRollup, cluster);
        if (metricValue instanceof Double) {
            metricWriter.printMetric(String.valueOf(Math.round((Double) metricValue)));
        } else if (metricValue instanceof Float) {
            metricWriter.printMetric(String.valueOf(Math.round((Float) metricValue)));
        } else {
            metricWriter.printMetric(String.valueOf(metricValue));
        }
    }

    private class Credential {
        public String dbname;
        public String host;
        public String port;
        public String username;
        public String password;
        public String filter;
        public String mBeanDomain;
    }
}
