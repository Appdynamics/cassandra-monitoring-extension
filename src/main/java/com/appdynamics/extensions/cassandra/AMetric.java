package com.appdynamics.extensions.cassandra;


import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AMetric {

    public static final String EMPTY_STRING = "";
    private String metricKey;
    private Object metricValue;
    private String metricPrefix;
    private String metricPostfix;
    private String aggregator;
    private String timeRollup;
    private String clusterRollup;
    private boolean disabled;
    private int multiplier;

    private AMetric(AMetricBuilder builder){
        this.metricKey = builder.metricKey;
        this.metricValue = builder.metricValue;
        this.metricPrefix = builder.metricPrefix;
        this.metricPostfix = builder.metricPostfix;
        this.aggregator = builder.aggregator;
        this.timeRollup = builder.timeRollup;
        this.clusterRollup = builder.clusterRollup;
        this.disabled = builder.disabled;
        this.multiplier = builder.multiplier;
    }

    public String getMetricKey() {
        return metricKey;
    }

    public String getMetricPath(){
        return getMetricPrefix() + getMetricKey() + getMetricPostfix();
    }

    public Object getMetricValue() {
        return metricValue;
    }

    public String getMetricValueAsBigString(){
        Object metricValue = getMetricValue();
        try {
            BigDecimal bigD = new BigDecimal(metricValue.toString()).multiply(new BigDecimal(getMultiplier()));
            return bigD.setScale(0, RoundingMode.HALF_UP).toString();
        }
        catch(NumberFormatException nfe){
        }
        return BigDecimal.ZERO.toString();
    }

    public String getMetricPrefix() {
        if(metricPrefix == null){
            return EMPTY_STRING;
        }
        return metricPrefix;
    }

    public String getMetricPostfix() {
        if(metricPostfix == null){
            return EMPTY_STRING;
        }
        return metricPostfix;
    }

    public String getAggregator() {
        if(Strings.isNullOrEmpty(aggregator) || !isAggregatorValid(aggregator)){
            return MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        }
        return aggregator;
    }

    private boolean isAggregatorValid(String aggregator) {
         if(aggregator.equalsIgnoreCase(MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE) ||
                 aggregator.equalsIgnoreCase(MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION) ||
                 aggregator.equalsIgnoreCase(MetricWriter.METRIC_AGGREGATION_TYPE_SUM)){
             return true;
         }
        return false;
    }

    public String getTimeRollup() {
        if(Strings.isNullOrEmpty(timeRollup) || !isTimeRollupValid(timeRollup)){
            return MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
        }
        return timeRollup;
    }

    private boolean isTimeRollupValid(String timeRollup) {
        if(timeRollup.equalsIgnoreCase(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE) ||
                timeRollup.equalsIgnoreCase(MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT) ||
                timeRollup.equalsIgnoreCase(MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM)){
            return true;
        }
        return false;
    }

    public String getClusterRollup() {
        if(Strings.isNullOrEmpty(clusterRollup) || !isClusterRollupValid(timeRollup)){
            return MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        }
        return clusterRollup;
    }

    private boolean isClusterRollupValid(String clusterRollup) {
        if(clusterRollup.equalsIgnoreCase(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE) ||
                clusterRollup.equalsIgnoreCase(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE)){
            return true;
        }
        return false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public static class AMetricBuilder{
        private String metricKey;
        private Object metricValue;
        private String metricPrefix;
        private String metricPostfix;
        private String aggregator;
        private String timeRollup;
        private String clusterRollup;
        private boolean disabled;
        private int multiplier=1;

        public AMetricBuilder(String metricKey,Object metricValue){
            this.metricKey = metricKey;
            this.metricValue = metricValue;
        }

        public AMetricBuilder metricPrefix(String metricPrefix){
            this.metricPrefix=metricPrefix;
            return this;
        }

        public AMetricBuilder metricPostfix(String metricPostfix){
            this.metricPostfix=metricPostfix;
            return this;
        }

        public AMetricBuilder aggregator(String aggregator){
            this.aggregator = aggregator;
            return this;
        }

        public AMetricBuilder timeRollup(String timeRollup){
            this.timeRollup = timeRollup;
            return this;
        }

        public AMetricBuilder clusterRollup(String clusterRollup){
            this.clusterRollup = clusterRollup;
            return this;
        }

        public AMetricBuilder disabled(boolean disabled){
            this.disabled = disabled;
            return this;
        }

        public AMetricBuilder multiplier(int multiplier){
            this.multiplier = multiplier;
            return this;
        }

        public AMetric build(){
            return new AMetric(this);
        }

    }
}
