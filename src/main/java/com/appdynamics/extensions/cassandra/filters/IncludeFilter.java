/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.filters;

import com.appdynamics.extensions.cassandra.utils.JMXUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IncludeFilter {
    private List<Map<String, ?>> dictionary;

    public IncludeFilter(List<Map<String, ?>> dictionary) {
        this.dictionary = dictionary;
    }

    public void applyFilter(Set<String> filteredSet, List<String> readableAttributes) {
        if (readableAttributes == null || dictionary == null) {
            return;
        }

        for (Map<String, ?> mapVal : dictionary) {
            String metricName = (String) mapVal.get("name");
            if (JMXUtil.isCompositeObject(metricName)) {
                metricName = JMXUtil.getMetricNameFromCompositeObject(metricName);
            }
            if (readableAttributes.contains(metricName)) {
                filteredSet.add(metricName);
            }
        }
    }
}

