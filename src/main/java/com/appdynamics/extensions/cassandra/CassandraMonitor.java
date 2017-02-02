package com.appdynamics.extensions.cassandra;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.TaskInputArgs.PASSWORD_ENCRYPTED;
import static com.appdynamics.extensions.cassandra.CassandraUtil.convertToString;

/**
 * This extension will extract metrics from Cassandra through the JMX protocol.
 */
public class CassandraMonitor extends AManagedMonitor {

    private static final Logger logger = LoggerFactory.getLogger(CassandraMonitor.class);
    private MonitorConfiguration configuration;

    public CassandraMonitor () {
        logger.info(getLogVersion());
    }

    public TaskOutput execute (Map<String, String> map, TaskExecutionContext taskExecutionContext) throws
            TaskExecutionException {
        logger.info(getLogVersion());
        logger.debug("The raw arguments are {}", map);
        try {
            initialize(map);
            configuration.executeTask();
        } catch (Exception ex) {
            if (configuration != null && configuration.getMetricWriter() != null) {
                configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            }
        }
        return null;
    }

    protected void initialize (Map<String, String> argsMap) {
        if (configuration == null) {
            MetricWriteHelper metricWriter = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration("Custom Metrics|Cassandra|", new TaskRunner(),
                    metricWriter);
            final String configFilePath = argsMap.get("config-file");
            conf.setConfigYml(configFilePath);
            conf.checkIfInitialized(MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem
                    .CONFIG_YML, MonitorConfiguration.ConfItem.HTTP_CLIENT, MonitorConfiguration.ConfItem
                    .EXECUTOR_SERVICE);
            this.configuration = conf;
        }
    }

    private class TaskRunner implements Runnable {
        public void run () {
            Map<String, ?> config = configuration.getConfigYml();
            if (config != null) {
                List<Map> servers = (List) config.get("servers");
                if (servers != null && !servers.isEmpty()) {
                    for (Map server : servers) {
                        try {
                            CassandraMonitorTask task = createTask(server);
                            configuration.getExecutorService().execute(task);
                        } catch (IOException e) {
                            logger.error("Cannot construct JMX uri for {}", convertToString(server.get("displayName")
                                    , ""));
                        }
                    }
                } else {
                    logger.error("There are no servers configured");
                }
            } else {
                logger.error("The config.yml is not loaded due to previous errors.The task will not run");
            }
        }
    }

    public static String getImplementationVersion () {
        return CassandraMonitor.class.getPackage().getImplementationTitle();
    }

    private String getLogVersion () {
        return "Using Cassandra Monitor Version [" + getImplementationVersion() + "]";
    }

    private CassandraMonitorTask createTask (Map server) throws IOException {
        String serviceUrl = convertToString(server.get("serviceURL"), "");
        String host = convertToString(server.get("host"), "");
        String portStr = convertToString(server.get("port"), "");
        int port = (portStr == null) ? -1 : Integer.parseInt(portStr);
        String username = convertToString(server.get("username"), "");
        String password = getPassword(server);
        JMXConnectionAdapter adapter = JMXConnectionAdapter.create(serviceUrl, host, port, username, password);
        return new CassandraMonitorTask.Builder().metricPrefix(configuration.getMetricPrefix()).metricWriter
                (configuration.getMetricWriter()).jmxConnectionAdapter(adapter).server(server).mbeans((List<Map>)
                configuration.getConfigYml().get("mbeans")).build();
    }

    private String getPassword (Map server) {
        String password = convertToString(server.get("password"), "");
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        }
        String encryptionKey = convertToString(configuration.getConfigYml().get("encryptionKey"), "");
        String encryptedPassword = convertToString(server.get("encryptedPassword"), "");
        if (!Strings.isNullOrEmpty(encryptionKey) && !Strings.isNullOrEmpty(encryptedPassword)) {
            java.util.Map<String, String> cryptoMap = Maps.newHashMap();
            cryptoMap.put(PASSWORD_ENCRYPTED, encryptedPassword);
            cryptoMap.put(TaskInputArgs.ENCRYPTION_KEY, encryptionKey);
            return CryptoUtil.getPassword(cryptoMap);
        }
        return null;
    }

    public static void main (String[] args) throws TaskExecutionException {
        CassandraMonitor cassandraMonitor = new CassandraMonitor();
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("config-file", "/Users/adityajagtiani/repos/appdynamics/extensions/cassandra-monitoring-extension" +
                "" + "" + "/src/main/resources/conf/config.yml");
        cassandraMonitor.execute(argsMap, null);
    }
}
