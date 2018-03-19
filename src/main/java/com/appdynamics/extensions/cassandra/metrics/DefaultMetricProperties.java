/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra.metrics;

import com.singularity.ee.agent.systemagent.api.MetricWriter;


class DefaultMetricProperties extends MetricProperties {
    private static final String DEFAULT_METRIC_TYPE = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE + " " +
            MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE + " " + MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
    private static final boolean DEFAULT_AGGREGATION = false;
    private static final boolean DEFAULT_DELTA = false;

    DefaultMetricProperties () {
        setAggregationFields(DEFAULT_METRIC_TYPE);
        setMultiplier(DEFAULT_MULTIPLIER);
        setAggregation(DEFAULT_AGGREGATION);
        setDelta(DEFAULT_DELTA);
    }
}
