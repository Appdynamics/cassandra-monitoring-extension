package com.appdynamics.extensions.cassandra.filters;

import java.util.*;

public class ExcludeFilter {
    private List dictionary;

    public ExcludeFilter (List dictionary) {
        this.dictionary = dictionary;
    }

    public void applyFilter (Set<String> filteredSet, List<String> allMetrics) {
        if (allMetrics == null || dictionary == null) {
            return;
        }

        for (String metric : allMetrics) {
            if (!dictionary.contains(metric)) {
                filteredSet.add(metric);
            }
        }
    }
}
