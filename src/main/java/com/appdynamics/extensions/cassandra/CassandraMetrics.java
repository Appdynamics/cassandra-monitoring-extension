package com.appdynamics.extensions.cassandra;


import com.appdynamics.extensions.util.metrics.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraMetrics {

    private String displayName;
    private Map<String,Object> metrics;
    private List<Metric> allMetrics;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Object> getMetrics() {
        if(metrics == null){
            metrics = new HashMap<String, Object>();
        }
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public List<Metric> getAllMetrics() {
        if(allMetrics == null){
            allMetrics = new ArrayList<Metric>();
        }
        return allMetrics;
    }


}
