package com.appdynamics.monitors.cassandra;

import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


public class CassandraCommunicator implements Callable<Map<String, Object>> {
    private static final String CASSANDRA_METRICS_OBJECT = "org.apache.cassandra.metrics";
    private static final String CAMEL_CASE_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
    private static final String CUSTOM_METRICS_CASSANDRA_STATUS = "Custom Metrics|Cassandra|Status";
    public static final String DBNAME_KEY = "__DBNAME__";
    private Logger logger;

    private String dbname;
    private String host;
    private String port;
    private String username;
    private String password;
    private String filter;
    private String mBeanDomain;

    private MBeanServerConnection connection;
    private Set<String> filters;
    private Map<String, Object> cassandraMetrics;

    public CassandraCommunicator(String dbname, String host, String port, String username,
                                 String password, String filter, String mBeanDomain, Logger logger) {
        this.dbname = dbname;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.filter = filter;
        this.mBeanDomain = (isNotEmpty(mBeanDomain)) ? mBeanDomain : CASSANDRA_METRICS_OBJECT;

        this.logger = logger;
        cassandraMetrics = new HashMap<String, Object>();
        cassandraMetrics.put(DBNAME_KEY, dbname);
        filters = new HashSet<String>();
    }

    @Override
    public Map<String, Object> call() throws Exception {
        if (isNotEmpty(host) && isNotEmpty(port)){
            parseFilter();

            connect();
            populateMetrics();

            return cassandraMetrics;
        } else {
            return null;
        }
    }

    private void connect() throws IOException {
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        final Map<String, Object> env = new HashMap<String, Object>();
        JMXConnector connector;

        //TODO: is the conditional needed
        if (!"".equals(username)) {
            env.put(JMXConnector.CREDENTIALS, new String[]{username, password});
            connector = JMXConnectorFactory.connect(url, env);
        } else {
            connector = JMXConnectorFactory.connect(url);
        }

        connection = connector.getMBeanServerConnection();
    }

    private void populateMetrics() {
        try {
            // Get all the m-beans registered.
            final Set<ObjectInstance> queryMBeans = connection.queryMBeans(null, null);

            // Iterate through each of them available.
            for (final ObjectInstance mbean : queryMBeans) {

                // Get the canonical name
                final String canonicalName = mbean.getObjectName().getCanonicalName();

                // See if its the one we want to gather metrics from.
                // If the 'domain' name is not supplied then,
                // the m-bean "org.apache.cassandra.metrics" would be used.
                if (!canonicalName.startsWith(mBeanDomain)) {
                    continue;
                }
                final ObjectName objectName = mbean.getObjectName();

                // Fetch all attributes.
                final MBeanAttributeInfo[] attributes = connection.getMBeanInfo(objectName).getAttributes();
                for (final MBeanAttributeInfo attr : attributes) {

                    // See we do not violate the security rules, i.e. only if the attribute is readable.
                    if (attr.isReadable()) {
                        // Collect the statistics.
                        final Object attribute = connection.getAttribute(objectName, attr.getName());

                        // Get the metrics name tiled.
                        final String attributeNameTiled = getTileCase(attr.getName());
                        if (!(attribute instanceof Number) || isFiltered(attributeNameTiled)) {
                            continue;
                        }

                        final String[] split = canonicalName.substring(canonicalName.indexOf(':') + 1).split(",");
                        String type = null, keySpace = null, scope = null, name = null;

                        // Form the AppDynamics metric path
                        for (final String token : split) {
                            final String[] keyValuePairs = token.split("=");

                            // Standard jmx attributes. {type, scope, name, keyspace, etc.}
                            if ("type".equalsIgnoreCase(keyValuePairs[0])) {
                                type = keyValuePairs[1];
                            } else if ("scope".equalsIgnoreCase(keyValuePairs[0])) {
                                scope = keyValuePairs[1];
                            } else if ("name".equalsIgnoreCase(keyValuePairs[0])) {
                                name = keyValuePairs[1];
                            } else if ("keyspace".equalsIgnoreCase(keyValuePairs[0])) {
                                keySpace = keyValuePairs[1];
                            }
                        }

                        String metricsKey = getMetricPrefix() + "|" + dbname
                                + ((isNotEmpty(type)) ? ("|" + getTileCase(type)) : "")
                                + ((isNotEmpty(keySpace)) ? ("|" + getTileCase(keySpace)) : "")
                                + ((isNotEmpty(scope)) ? ("|" + getTileCase(scope)) : "")
                                + ((isNotEmpty(name)) ? ("|" + getTileCase(name)) : "")
                                + ("|" + attributeNameTiled);

                        cassandraMetrics.put(metricsKey, attribute);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Collecting statistics failed.", e);
        }
    }

    private void parseFilter() {
        if (isNotEmpty(filter)) {
            String[] split = filter.toLowerCase().split(",");
            filters.clear();

            for (String token : split) {
                filters.add(token.trim());
            }
        }
    }

    private String getTileCase(String camelCase) {
        if (camelCase.contains("_")) {
            return getTileCase(camelCase, CAMEL_CASE_REGEX);
        } else {
            return getTileCase(camelCase, "_+");
        }
    }

    private String getTileCase(String camelCase, String regex) {
        String tileCase = "";
        String[] tileWords = camelCase.split(regex);

        for (String tileWord : tileWords) {
            if (!tileWord.isEmpty()) {
                tileCase += Character.toUpperCase(tileWord.charAt(0)) + tileWord.substring(1) + " ";
            }
        }

        return tileCase.trim();
    }

    private boolean isFiltered(String name) {
        return filters.contains(name.toLowerCase());
    }

    private static boolean isNotEmpty(final String input) {
        return input != null && !"".equals(input.trim());
    }

    public static String getMetricPrefix() {
        return CUSTOM_METRICS_CASSANDRA_STATUS;
    }
}
