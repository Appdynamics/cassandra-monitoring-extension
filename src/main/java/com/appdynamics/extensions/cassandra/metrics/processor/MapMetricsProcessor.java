/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.metrics.processor;

import com.appdynamics.extensions.cassandra.metrics.MetricDetails;

import javax.management.Attribute;
import java.util.Map;

import static com.appdynamics.extensions.cassandra.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class MapMetricsProcessor extends BaseMetricsProcessor {

    @Override
    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        Map<String, ?> attributesFound = (Map<String, ?>) attribute.getValue();
        processAttributeValue(metricDetails, attributeName, attributesFound);
    }

    private void processAttributeValue(MetricDetails metricDetails, String attributeName, Map<String, ?> attributesFound) {
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute mapMetric = new Attribute(key, attributeValue);
            if (attributeValue instanceof Map) {
                populateMetricsFromEntity(metricDetails, mapMetric);
            } else {
                super.populateMetricsFromEntity(metricDetails, mapMetric);
            }
        }
    }


}
