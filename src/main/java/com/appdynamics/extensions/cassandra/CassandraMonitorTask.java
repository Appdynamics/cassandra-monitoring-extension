package com.appdynamics.extensions.cassandra;


import com.appdynamics.extensions.jmx.JMXConnectionUtil;
import com.appdynamics.extensions.jmx.MBeanKeyPropertyEnum;
import com.appdynamics.extensions.util.metrics.Metric;
import com.appdynamics.extensions.util.metrics.MetricFactory;
import com.appdynamics.extensions.util.metrics.MetricOverride;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.appdynamics.extensions.util.metrics.MetricConstants.METRICS_SEPARATOR;

public class CassandraMonitorTask implements Callable<Void> {

    private String metricPrefix;
    private String displayName;
    private AManagedMonitor monitor;
    private MetricOverride[] metricOverrides;
    private JMXConnectionUtil jmxConnector;
    public static final Logger logger = Logger.getLogger(CassandraMonitorTask.class);


    public CassandraMonitorTask(String metricPrefix,String displayName,MetricOverride[] metricOverrides,JMXConnectionUtil jmxConnector,AManagedMonitor monitor) {
        this.metricPrefix = metricPrefix;
        this.displayName = displayName;
        this.metricOverrides = metricOverrides;
        this.jmxConnector = jmxConnector;
        this.monitor = monitor;
    }


    public Void call() throws Exception {
        Map<String, Object> allMetrics = extractJMXMetrics();
        // to get overridden properties for a metric.
        MetricFactory<Object> metricFactory = new MetricFactory<Object>(metricOverrides);
        List<Metric> decoratedMetrics = metricFactory.process(allMetrics);
        reportMetrics(decoratedMetrics);
        return null;
    }


    /**
     * Connects to a remote/local JMX server, applies exclusion filters and collects the metrics
     *
     * @return Void. In case of exception, the CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL is set with CassandraMonitorConstants.ERROR_VALUE.
     * @throws Exception
     */
    private Map<String, Object> extractJMXMetrics() throws IOException {
        Map<String, Object> allMetrics = new HashMap<String, Object>();
        long startTime = System.currentTimeMillis();
        logger.debug("Starting Cassandra monitor thread at " + startTime + " for server " + displayName);
        try{
            JMXConnector connector = jmxConnector.connect();
            if(connector != null){
                Set<ObjectInstance> allMbeans = jmxConnector.getAllMBeans();
                if(allMbeans != null) {
                    mapMetrics(allMbeans, allMetrics);
                    allMetrics.put(CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL, CassandraMonitorConstants.SUCCESS_VALUE);
                }
            }
        }
        catch(Exception e){
            logger.error("Error JMX-ing into the server :: " + displayName, e);
            long diffTime = System.currentTimeMillis() - startTime;
            logger.debug("Error in Cassandra thread at " + diffTime);
            allMetrics.put(CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL, CassandraMonitorConstants.ERROR_VALUE);
        }
        finally{
            jmxConnector.close();
        }
        return allMetrics;
    }

    private void mapMetrics(Set<ObjectInstance> allMbeans, Map<String, Object> allMetrics) {
        for(ObjectInstance mbean : allMbeans){
            ObjectName objectName = mbean.getObjectName();
            MBeanAttributeInfo[] attributes = jmxConnector.fetchAllAttributesForMbean(objectName);
            if (attributes != null) {
                for (MBeanAttributeInfo attr : attributes) {
                    try {
                        // See we do not violate the security rules, i.e. only if the attribute is readable.
                        if (attr.isReadable()) {
                            Object attribute = jmxConnector.getMBeanAttribute(objectName, attr.getName());
                            //AppDynamics only considers number values
                            if (attribute != null && attribute instanceof Number) {
                                String metricKey = getMetricsKey(objectName, attr);
                                allMetrics.put(metricKey, attribute);
                            }
                        }
                    }
                    catch(Exception e){
                        logger.warn("Error fetching attribute " + attr.getName(), e);
                    }
                }
            }
        }
    }

    private void reportMetrics(List<Metric> decoratedMetrics) {
        StringBuffer pathPrefixBuffer = new StringBuffer();
        pathPrefixBuffer.append(metricPrefix);
        if(!metricPrefix.endsWith("|")){
            pathPrefixBuffer.append(METRICS_SEPARATOR);
        }
        pathPrefixBuffer.append(displayName).append(METRICS_SEPARATOR);
        String pathPrefix = pathPrefixBuffer.toString();
        for(Metric aMetric:decoratedMetrics){
            printMetric(pathPrefix + aMetric.getMetricPath(),aMetric.getMetricValue().toString(),aMetric.getAggregator(),aMetric.getTimeRollup(),aMetric.getClusterRollup());
        }
    }

    private void printMetric(String metricPath,String metricValue,String aggType,String timeRollupType,String clusterRollupType) {
        MetricWriter metricWriter = monitor.getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );
        System.out.println("Sending [" + aggType + METRICS_SEPARATOR + timeRollupType + METRICS_SEPARATOR + clusterRollupType
        		+ "] metric = " + metricPath + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug("Sending [" + aggType + METRICS_SEPARATOR + timeRollupType + METRICS_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }


    private String getMetricsKey(ObjectName objectName,MBeanAttributeInfo attr) {
        // Standard jmx keys. {type, scope, name, keyspace, path etc.}
        String type = objectName.getKeyProperty(MBeanKeyPropertyEnum.TYPE.toString());
        String keyspace = objectName.getKeyProperty(MBeanKeyPropertyEnum.KEYSPACE.toString());
        String path = objectName.getKeyProperty(MBeanKeyPropertyEnum.PATH.toString());
        String scope = objectName.getKeyProperty(MBeanKeyPropertyEnum.SCOPE.toString());
        String name = objectName.getKeyProperty(MBeanKeyPropertyEnum.NAME.toString());
        String columnFamily = objectName.getKeyProperty(MBeanKeyPropertyEnum.COLUMNFAMILY.toString());
        StringBuilder metricsKey = new StringBuilder();
        metricsKey.append(Strings.isNullOrEmpty(type) ? "" : type + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(keyspace) ? "" : keyspace + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(path) ? "" : path + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(scope) ? "" : scope + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(columnFamily) ? "" : columnFamily + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(name) ? "" : name + METRICS_SEPARATOR);
        metricsKey.append(attr.getName());

        return metricsKey.toString();
    }





}
