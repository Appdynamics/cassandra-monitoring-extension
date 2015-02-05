package com.appdynamics.extensions.cassandra.config;


public class MetricOverride {

    private String metricKey;
    private boolean disabled;
    private int multiplier = 1;
    private String aggregator;
    private String timeRollup;
    private String clusterRollup;
    private String postfix;

    public String getMetricKey() {
        return metricKey;
    }

    public void setMetricKey(String metricKey) {
        this.metricKey = metricKey;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public String getTimeRollup() {
        return timeRollup;
    }

    public void setTimeRollup(String timeRollup) {
        this.timeRollup = timeRollup;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getClusterRollup() {
        return clusterRollup;
    }

    public void setClusterRollup(String clusterRollup) {
        this.clusterRollup = clusterRollup;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
