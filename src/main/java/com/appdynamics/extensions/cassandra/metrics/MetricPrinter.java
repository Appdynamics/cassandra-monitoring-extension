/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra.metrics;


import com.appdynamics.extensions.util.MetricWriteHelper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static com.appdynamics.extensions.cassandra.CassandraUtil.*;

public class MetricPrinter {

    private static final Logger logger = LoggerFactory.getLogger(MetricPrinter.class);
    private int totalMetricsReported;
    private String metricPrefix;
    private String displayName;
    private MetricWriteHelper metricWriter;

    public MetricPrinter (String metricPrefix, String displayName, MetricWriteHelper metricWriter) {
        this.metricPrefix = metricPrefix;
        this.displayName = displayName;
        this.metricWriter = metricWriter;
    }

    public void reportNodeMetrics (final List<Metric> componentMetrics) {
        if (componentMetrics == null || componentMetrics.isEmpty()) {
            return;
        }
        for (Metric metric : componentMetrics) {
            MetricProperties props = metric.getProperties();
            String fullMetricPath = formMetricPath(metric.getMetricKey());
            printMetric(fullMetricPath, metric.getMetricValue(), props.getAggregationType(), props.getTimeRollupType
                    (), props.getClusterRollupType());
        }
    }

    public void printMetric (String metricPath, BigDecimal metricValue, String aggType, String timeRollupType, String
            clusterRollupType) {
        try {
            String metricValStr = toBigIntString(metricValue);
            if (metricValStr != null) {
                metricWriter.printMetric(metricPath, metricValStr, aggType, timeRollupType, clusterRollupType);
                logger.debug("Sending [{}|{}|{}] metric= {},value={}", aggType, timeRollupType, clusterRollupType,
                        metricPath, metricValStr);
                totalMetricsReported++;
            }
        } catch (Exception e) {
            logger.error("Error reporting metric {} with value {}", metricPath, metricValue, e);
        }
    }

    public String formMetricPath (String metricKey) {
        if (!Strings.isNullOrEmpty(displayName)) {
            return metricPrefix + "|" + displayName + "|" + metricKey;
        }
        return metricPrefix + "|" + metricKey;
    }

    public int getTotalMetricsReported () {
        return totalMetricsReported;
    }
}
