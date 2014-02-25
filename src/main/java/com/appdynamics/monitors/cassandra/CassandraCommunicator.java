/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private static final String CUSTOM_METRICS_CASSANDRA_STATUS = "Custom Metrics|Cassandra|";
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

    public Map<String, Object> call() throws Exception {
        if (isNotEmpty(host) && isNotEmpty(port)){
            parseFilter();

            connectAndPopulateMetrics();

            return cassandraMetrics;
        } else {
            return null;
        }
    }

    private void connectAndPopulateMetrics() throws IOException {
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
        
        try {
            populateMetrics();
        } finally {
            if(connector != null) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Closing the connection");
                }
                connector.close();
            }
        }
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

                        String metricsKey = getMetricPrefix() + dbname
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
            logger.error("Collecting statistics failed for '" + host + ":" + port + " mbean=" + mBeanDomain + "'.", e);
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
            return getTileCase(camelCase, "_+");
        } else {
            return getTileCase(camelCase, CAMEL_CASE_REGEX);
        }
    }

    private String getTileCase(String camelCase, String regex) {
        String tileCase = "";
        String[] tileWords = camelCase.split(regex);

        for (String tileWord : tileWords) {
            if (tileWord.length() > 0) {
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
