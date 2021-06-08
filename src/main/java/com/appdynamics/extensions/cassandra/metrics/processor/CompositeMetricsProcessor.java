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
import javax.management.openmbean.CompositeData;
import java.util.Set;

import static com.appdynamics.extensions.cassandra.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class CompositeMetricsProcessor extends BaseMetricsProcessor {

    @Override
    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        CompositeData metricValue = (CompositeData) attribute.getValue();
        Set<String> attributesFound = metricValue.getCompositeType().keySet();
        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
                Object attributeValue = metricValue.get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                // Value of a composite type has to be a base metric, can not be a list, map
                super.populateMetricsFromEntity(metricDetails, attribute1);
            }
        }
    }
}