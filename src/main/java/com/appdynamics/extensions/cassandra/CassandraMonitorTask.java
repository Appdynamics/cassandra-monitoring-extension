package com.appdynamics.extensions.cassandra;


import com.appdynamics.extensions.cassandra.config.MBeanData;
import com.appdynamics.extensions.cassandra.config.Server;
import com.appdynamics.extensions.jmx.JMXConnectionConfig;
import com.appdynamics.extensions.jmx.JMXConnectionUtil;
import com.appdynamics.extensions.jmx.MBeanKeyPropertyEnum;
import com.appdynamics.extensions.util.MetricUtils;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class CassandraMonitorTask implements Callable<CassandraMetrics> {

    public static final String METRICS_SEPARATOR = "|";
    private Server server;
    private MBeanData[] mbeansData;
    private Map<String,MBeanData> mbeanLookup;
    private JMXConnectionUtil jmxConnector;
    public static final Logger logger = Logger.getLogger(CassandraMonitorTask.class);

    public CassandraMonitorTask(Server server, MBeanData[] mbeansData) {
        this.server = server;
        this.mbeansData = mbeansData;
        createMBeansLookup(mbeansData);
    }



    private void createMBeansLookup(MBeanData[] mbeansData) {
        mbeanLookup = new HashMap<String, MBeanData>();
        if(mbeansData != null){
            for(MBeanData mBeanData : mbeansData){
                mbeanLookup.put(mBeanData.getDomainName(),mBeanData);
            }
        }
    }


    /**
     * Connects to a remote/local JMX server, applies exclusion filters and collects the metrics
     *
     * @return CassandraMetrics. In case of exception, the CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL is set with CassandraMonitorConstants.ERROR_VALUE.
     * @throws Exception
     */
    public CassandraMetrics call() throws Exception {
        CassandraMetrics cassandraMetrics = new CassandraMetrics();
        cassandraMetrics.setDisplayName(server.getDisplayName());
        try{
            jmxConnector = new JMXConnectionUtil(new JMXConnectionConfig(server.getHost(),server.getPort(),server.getUsername(),server.getPassword()));
            JMXConnector connector = jmxConnector.connect();
            if(connector != null){
                Set<ObjectInstance> allMbeans = jmxConnector.getAllMBeans();
                if(allMbeans != null) {
                    Map<String, String> filteredMetrics = applyExcludePatternsAndExtractMetrics(allMbeans);
                    filteredMetrics.put(CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL, CassandraMonitorConstants.SUCCESS_VALUE);
                    cassandraMetrics.setMetrics(filteredMetrics);
                }
            }
        }
        catch(Exception e){
            logger.error("Error JMX-ing into the server :: " +cassandraMetrics.getDisplayName() + e);
            cassandraMetrics.getMetrics().put(CassandraMonitorConstants.METRICS_COLLECTION_SUCCESSFUL,CassandraMonitorConstants.ERROR_VALUE);
        }
        finally{
            jmxConnector.close();
        }
        return cassandraMetrics;
    }

    private Map<String, String> applyExcludePatternsAndExtractMetrics(Set<ObjectInstance> allMbeans) {
        Map<String,String> filteredMetrics = new HashMap<String, String>();
        for(ObjectInstance mbean : allMbeans){
            ObjectName objectName = mbean.getObjectName();
            //consider only the the metric domains (org.apache.cassandra.metrics) mentioned in the config
            if(isDomainConfigured(objectName)){
                MBeanData mBeanData = mbeanLookup.get(objectName.getDomain());
                Set<String> excludePatterns = mBeanData.getExcludePatterns();
                MBeanAttributeInfo[] attributes = jmxConnector.fetchAllAttributesForMbean(objectName);
                if(attributes != null) {
                    for (MBeanAttributeInfo attr : attributes) {
                        // See we do not violate the security rules, i.e. only if the attribute is readable.
                        if (attr.isReadable()) {
                            Object attribute = jmxConnector.getMBeanAttribute(objectName, attr.getName());
                            //AppDynamics only considers number values
                            if (attribute != null && attribute instanceof Number) {
                                String metricKey = getMetricsKey(objectName,attr);
                                if (!isKeyExcluded(metricKey, excludePatterns)) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Metric key:value before ceiling = "+ metricKey + ":" + String.valueOf(attribute));
                                    }
                                    String attribStr = MetricUtils.toWholeNumberString(attribute);
                                    filteredMetrics.put(metricKey, attribStr);
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(metricKey + " is excluded");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return filteredMetrics;
    }



    /**
     * Checks if the given metric key matches any exclude patterns
     *
     * @param metricKey
     * @param excludePatterns
     * @return true if match, false otherwise
     */
    private boolean isKeyExcluded(String metricKey, Set<String> excludePatterns) {
        for(String excludePattern : excludePatterns){
            if(metricKey.matches(escapeText(excludePattern))){
                return true;
            }
        }
        return false;
    }

    private String escapeText(String excludePattern) {
        return excludePattern.replaceAll("\\|","\\\\|");
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


    private boolean isDomainConfigured(ObjectName objectName) {
        return (mbeanLookup.get(objectName.getDomain()) != null);
    }


}
