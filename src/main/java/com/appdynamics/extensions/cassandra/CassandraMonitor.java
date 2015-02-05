package com.appdynamics.extensions.cassandra;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.cassandra.config.ConfigUtil;
import com.appdynamics.extensions.cassandra.config.Configuration;
import com.appdynamics.extensions.cassandra.config.MetricOverride;
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

    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if (taskArgs != null) {
            logger.info("Starting the Cassandra Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug("Task Arguments Passed ::" + taskArgs);
            }
            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
            try {
                //read the config.
                Configuration config = configUtil.readConfig(configFilename, Configuration.class);
                threadPool = Executors.newFixedThreadPool(config.getNumberOfThreads() == 0 ? DEFAULT_NUMBER_OF_THREADS : config.getNumberOfThreads());
                List<Future<CassandraMetrics>> parallelTasks = createConcurrentTasks(config);
                //collect the metrics
                List<CassandraMetrics> cMetrics = collectMetrics(parallelTasks,config.getThreadTimeout() == 0 ? DEFAULT_THREAD_TIMEOUT : config.getThreadTimeout());

                //metric overrides
                Map<String,MetricOverride> overrideMap = createOverrideLookup(config.getMetricOverides());
                //build metrics
                List<AMetric> allMetrics = new ArrayList<AMetric>();
                for(CassandraMetrics cMetric : cMetrics){
                    allMetrics.addAll(initializeMetrics(getMetricPrefix(config, cMetric),cMetric.getMetrics(), overrideMap));
                }
                printMetrics(allMetrics);
                logger.info("Cassandra monitoring task completed successfully.");
                return new TaskOutput("Cassandra monitoring task completed successfully.");
            } catch (FileNotFoundException e) {
                logger.error("Config file not found :: " + configFilename, e);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Metrics collection failed", e);
            } finally {
                if(!threadPool.isShutdown()){
                    threadPool.shutdown();
                }
            }

        }
        throw new TaskExecutionException("Cassandra monitoring task completed with failures.");
    }

    private void printMetrics(List<AMetric> allMetrics) {
        for(AMetric aMetric:allMetrics){
            if(!aMetric.isDisabled()) {
                logger.debug("Original Metric Key =" + aMetric.getMetricKey() + ", Original Metric Value =" + aMetric.getMetricValue());
                printMetric(aMetric.getMetricPath(), aMetric.getMetricValueAsBigString(), aMetric.getAggregator(), aMetric.getTimeRollup(), aMetric.getClusterRollup());
            }
            else{
                logger.debug("Original Metric Key =" + aMetric.getMetricKey() + ", Original Metric Value =" + aMetric.getMetricValue() + " has reporting disabled from config file");
            }
        }
    }

    private String getMetricPrefix(Configuration config, CassandraMetrics cMetric) {
        return config.getMetricPrefix() + cMetric.getDisplayName() + METRIC_SEPARATOR;
    }

    private List<AMetric> initializeMetrics(String metricPrefix,Map<String, Object> metrics, Map<String, MetricOverride> overrideMap) {
        List<AMetric> aMetricList = new ArrayList<AMetric>();
        for(Map.Entry<String,Object> entry : metrics.entrySet()){
            String metricKey = entry.getKey();
            Object metricValue = entry.getValue();
            MetricOverride override = overrideMap.get(metricKey);
            AMetric.AMetricBuilder aMetricBuilder = createMetricBuilder(metricPrefix, metricKey, metricValue, override);
            aMetricList.add(aMetricBuilder.build());
        }
        return aMetricList;
    }

    private AMetric.AMetricBuilder createMetricBuilder(String metricPrefix,String metricKey, Object metricValue, MetricOverride override) {
        AMetric.AMetricBuilder aMetricBuilder = new AMetric.AMetricBuilder(metricKey,metricValue);
        if(!Strings.isNullOrEmpty(metricPrefix)){
            aMetricBuilder.metricPrefix(metricPrefix);
        }
        if(override != null){
            aMetricBuilder.disabled(override.isDisabled());
            aMetricBuilder.multiplier(override.getMultiplier());
            if(!Strings.isNullOrEmpty(override.getAggregator())){
                aMetricBuilder.aggregator(override.getAggregator());
            }
            if(!Strings.isNullOrEmpty(override.getClusterRollup())){
                aMetricBuilder.clusterRollup(override.getClusterRollup());
            }
            if(!Strings.isNullOrEmpty(override.getTimeRollup())){
                aMetricBuilder.timeRollup(override.getTimeRollup());
            }
            if(!Strings.isNullOrEmpty(override.getPostfix())){
                aMetricBuilder.metricPostfix(override.getPostfix());
            }

        }
        return aMetricBuilder;
    }


    /**
     * Creates concurrent tasks
     *
     * @param config
     * @return Handles to concurrent tasks.(
     */
    private List<Future<CassandraMetrics>> createConcurrentTasks(Configuration config) {
        List<Future<CassandraMetrics>> parallelTasks = new ArrayList<Future<CassandraMetrics>>();
        if (config != null && config.getServers() != null) {
            for (Server server : config.getServers()) {
                CassandraMonitorTask cassandraTask = new CassandraMonitorTask(server);
                parallelTasks.add(getThreadPool().submit(cassandraTask));
            }
        }
        return parallelTasks;
    }


    /**
     * Collects the result from the thread.
     *
     * @param parallelTasks
     * @return
     */
    private List<CassandraMetrics> collectMetrics(List<Future<CassandraMetrics>> parallelTasks, int timeout) {
        List<CassandraMetrics> allMetrics = new ArrayList<CassandraMetrics>();
        for (Future<CassandraMetrics> aParallelTask : parallelTasks) {
            CassandraMetrics cMetric = null;
            try {
                cMetric = aParallelTask.get(timeout, TimeUnit.SECONDS);
                allMetrics.add(cMetric);
            } catch (InterruptedException e) {
                logger.error("Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error("Task execution failed." + e);
            } catch (TimeoutException e) {
                logger.error("Task timed out." + e);
            }
        }
        return allMetrics;
    }




    /**
     * A helper method to report the metrics.
     * @param metricPath
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    private void printMetric(String metricPath,String metricValue,String aggType,String timeRollupType,String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );
      //  System.out.println("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
      //              + "] metric = " + metricPath + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
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


    public static String getImplementationVersion() {
        return CassandraMonitor.class.getPackage().getImplementationTitle();
    }


    private Map<String, MetricOverride> createOverrideLookup(MetricOverride[] metricOverides) {
        Map<String,MetricOverride> overrideLookup = new RegexHashMap<MetricOverride>();
        if(metricOverides != null){
            for(MetricOverride override : metricOverides){
                overrideLookup.put(override.getMetricKey(),override);
            }
        }
        return overrideLookup;
    }



}
