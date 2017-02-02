package com.appdynamics.extensions.cassandra.metrics;

import com.google.common.base.Strings;

import java.math.BigDecimal;

public class Metric {
    private String metricName;
    private String metricKey;
    private String instanceKey;
    private BigDecimal metricValue;
    private MetricProperties properties;

    String getMetricNameOrAlias () {
        if (properties == null || Strings.isNullOrEmpty(properties.getAlias())) {
            return metricName;
        }
        return properties.getAlias();
    }

    void setMetricName (String metricName) {
        this.metricName = metricName;
    }

    String getMetricKey () {
        return metricKey;
    }

    void setMetricKey (String metricKey) {
        this.metricKey = metricKey;
    }

    BigDecimal getMetricValue () {
        return metricValue;
    }

    void setMetricValue (BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    MetricProperties getProperties () {
        return properties;
    }

    void setProperties (MetricProperties properties) {
        this.properties = properties;
    }

    public String getInstanceKey () {
        return instanceKey;
    }

    void setInstanceKey (String instanceKey) {
        this.instanceKey = instanceKey;
    }
}
