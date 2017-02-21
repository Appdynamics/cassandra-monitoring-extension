package com.appdynamics.extensions.cassandra.filters;

import com.appdynamics.extensions.cassandra.CassandraUtil;

import java.util.*;

public class IncludeFilter {
    private List dictionary;

    public IncludeFilter (List dictionary) {
        this.dictionary = dictionary;
    }

    public void applyFilter (Set<String> filteredSet, List<String> allMetrics) {
        if (allMetrics == null || dictionary == null) {
            return;
        }

        for (Object obj : dictionary) {
            Map metric = (Map) obj;
            Map.Entry firstEntry = (Map.Entry) metric.entrySet().iterator().next();
            String metricName = firstEntry.getKey().toString();
            if (CassandraUtil.isCompositeObject(metricName)) {
                metricName = CassandraUtil.getMetricNameFromCompositeObject(metricName);
            }

            if (allMetrics.contains(metricName)) {
                filteredSet.add(metricName);
            }
        }
    }
}

