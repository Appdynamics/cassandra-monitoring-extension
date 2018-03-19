/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.cassandra.CassandraUtil;

import java.util.Map;

public class MetricProperties {
    static final double DEFAULT_MULTIPLIER = 1d;
    private String alias;
    private String metricName;
    private String aggregationType;
    private String timeRollupType;
    private String clusterRollupType;
    private double multiplier = DEFAULT_MULTIPLIER;
    private boolean aggregation;
    private boolean delta;
    private Map<Object,Object> conversionValues;

    String getAlias() {
        return alias;
    }

    void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMetricName() {
        return metricName;
    }

    void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    String getAggregationType() {
        return aggregationType;
    }

    void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    String getTimeRollupType() {
        return timeRollupType;
    }

    void setTimeRollupType(String timeRollupType) {
        this.timeRollupType = timeRollupType;
    }

    String getClusterRollupType() {
        return clusterRollupType;
    }

    void setClusterRollupType(String clusterRollupType) {
        this.clusterRollupType = clusterRollupType;
    }

    double getMultiplier() {
        return multiplier;
    }

    void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isAggregation() {
        return aggregation;
    }

    void setAggregation(boolean aggregation) {
        this.aggregation = aggregation;
    }

    Map<Object, Object> getConversionValues() {
        return conversionValues;
    }

    void setConversionValues(Map<Object, Object> conversionValues) {
        this.conversionValues = conversionValues;
    }

    boolean isDelta() {
        return delta;
    }

    void setDelta(boolean delta) {
        this.delta = delta;
    }

    void setAggregationFields(String metricType) {
        String[] metricTypes = CassandraUtil.split(metricType," ");
        this.setAggregationType(metricTypes[0]);
        this.setTimeRollupType(metricTypes[1]);
        this.setClusterRollupType(metricTypes[2]);
    }
}
