package com.appdynamics.extensions.cassandra.config;


public class Configuration {

    private Server[] servers;
    private MBeanData[] mbeans;
    private String metricPrefix;
    private int threadTimeout;
    private int numberOfThreads;

    public Server[] getServers() {
        return servers;
    }

    public void setServers(Server[] servers) {
        this.servers = servers;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public MBeanData[] getMbeans() {
        return mbeans;
    }

    public void setMbeans(MBeanData[] mbeans) {
        this.mbeans = mbeans;
    }

    public int getThreadTimeout() {
        return threadTimeout;
    }

    public void setThreadTimeout(int threadTimeout) {
        this.threadTimeout = threadTimeout;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
