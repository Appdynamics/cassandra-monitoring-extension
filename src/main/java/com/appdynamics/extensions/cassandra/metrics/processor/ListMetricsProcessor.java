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
import java.util.List;

import static com.appdynamics.extensions.cassandra.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class ListMetricsProcessor extends BaseMetricsProcessor {

    private Attribute getListMetric(Object metricKey, MetricDetails metricDetails, String attributeName) {
        String[] arr = metricKey.toString().split(metricDetails.getSeparator());
        String key = attributeName + PERIOD + arr[0].trim();

        String value = arr[1].trim();
        return new Attribute(key, value);
    }

    @Override
    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        List attributeValuesFromList = (List) attribute.getValue();
        for (Object metricNameKey : attributeValuesFromList) {
            Attribute listMetric = getListMetric(metricNameKey, metricDetails, attributeName);
            // Value of the list metricsold have to be of type base metric, can not be a list, map, composite
            super.populateMetricsFromEntity(metricDetails, listMetric);
        }
    }
}
