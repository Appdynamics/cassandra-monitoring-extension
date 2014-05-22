package com.appdynamics.extensions.cassandra;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.cassandra.config.ConfigUtil;
import com.appdynamics.extensions.cassandra.config.Configuration;
import com.appdynamics.extensions.cassandra.config.Server;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This extension will extract out metrics from Cassandra through the JMX protocol.
 */
public class CassandraMonitor extends AManagedMonitor {

    public static final Logger logger = Logger.getLogger(CassandraMonitor.class);
    public static final String CONFIG_ARG = "config-file";
    public static final String METRIC_SEPARATOR = "|";
    public static final String LOG_PREFIX = "log-prefix";
    private static final int DEFAULT_NUMBER_OF_THREADS = 10;
    public static final int DEFAULT_THREAD_TIMEOUT = 10;

    private ExecutorService threadPool;
    private static String logPrefix;

    //To load the config files
    private final static ConfigUtil<Configuration> configUtil = new ConfigUtil<Configuration>();


    public CassandraMonitor() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(msg);
        System.out.println(msg);

    }

    @Override
    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if (taskArgs != null) {
            setLogPrefix(taskArgs.get(LOG_PREFIX));
            logger.info(getLogPrefix() + "Starting the Cassandra Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug(getLogPrefix() + "Task Arguments Passed ::" + taskArgs);
            }
            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
            try {
                //read the config.
                Configuration config = configUtil.readConfig(configFilename, Configuration.class);
                threadPool = Executors.newFixedThreadPool(config.getNumberOfThreads() == 0 ? DEFAULT_NUMBER_OF_THREADS : config.getNumberOfThreads());
                List<Future<CassandraMetrics>> parallelTasks = createConcurrentTasks(config);
                //collect the metrics
                List<CassandraMetrics> cMetrics = collectMetrics(parallelTasks,config.getThreadTimeout() == 0 ? DEFAULT_THREAD_TIMEOUT : config.getThreadTimeout());
                //print the metrics
                printStats(config, cMetrics);
                logger.info(getLogPrefix() + "Cassandra monitoring task completed successfully.");
                return new TaskOutput(getLogPrefix() + "Cassandra monitoring task completed successfully.");
            } catch (FileNotFoundException e) {
                logger.error(getLogPrefix() + "Config file not found :: " + configFilename, e);
            } catch (Exception e) {
                logger.error(getLogPrefix() + "Metrics collection failed", e);
            }
        }
        throw new TaskExecutionException(getLogPrefix() + "Cassandra monitoring task completed with failures.");
    }


    /**
     * Creates concurrent tasks
     * @param config
     * @return Handles to concurrent tasks.
     */
    private List<Future<CassandraMetrics>> createConcurrentTasks(Configuration config) {
        List<Future<CassandraMetrics>> parallelTasks = new ArrayList<Future<CassandraMetrics>>();
        if (config != null && config.getServers() != null) {
            for (Server server : config.getServers()) {
                CassandraMonitorTask cassandraTask = new CassandraMonitorTask(server,config.getMbeans());
                parallelTasks.add(getThreadPool().submit(cassandraTask));
            }
        }
        return parallelTasks;
    }


    /**
     * Collects the result from the thread.
     * @param parallelTasks
     * @return
     */
    private List<CassandraMetrics> collectMetrics(List<Future<CassandraMetrics>> parallelTasks,int timeout) {
        List<CassandraMetrics> allMetrics = new ArrayList<CassandraMetrics>();
        for (Future<CassandraMetrics> aParallelTask : parallelTasks) {
            CassandraMetrics cMetric = null;
            try {
                cMetric = aParallelTask.get(timeout, TimeUnit.SECONDS);
                allMetrics.add(cMetric);
            } catch (InterruptedException e) {
                logger.error(getLogPrefix() + "Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error(getLogPrefix() + "Task execution failed." + e);
            } catch (TimeoutException e) {
                logger.error(getLogPrefix() + "Task timed out." + e);
            }
        }
        return allMetrics;
    }


    private void printStats(Configuration config, List<CassandraMetrics> cMetrics) {
        for (CassandraMetrics cMetric : cMetrics) {
            StringBuffer metricPath = new StringBuffer();
            metricPath.append(config.getMetricPrefix()).append(cMetric.getDisplayName()).append(METRIC_SEPARATOR);
            Map<String,String> metricsForAServer = cMetric.getMetrics();
            for(Map.Entry<String,String> entry : metricsForAServer.entrySet()){
                printCollectiveObservedCurrent(metricPath.toString() + entry.getKey(),entry.getValue());
            }
        }
    }


    private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }

    /**
     * A helper method to report the metrics.
     * @param metricPath
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    private void printMetric(String metricPath,String metricValue,String aggType,String timeRollupType,String clusterRollupType){
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );
           System.out.println(getLogPrefix()+"Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Returns a config file name,
     * @param filename
     * @return String
     */
    private String getConfigFilename(String filename) {
        if(filename == null){
            return "";
        }
        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if(!Strings.isNullOrEmpty(filename)){
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = (logPrefix != null) ? logPrefix : "";
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public static String getImplementationVersion() {
        return CassandraMonitor.class.getPackage().getImplementationTitle();
    }
}
