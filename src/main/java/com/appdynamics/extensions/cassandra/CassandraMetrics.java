package com.appdynamics.extensions.cassandra;


import java.util.HashMap;
import java.util.Map;

public class CassandraMetrics {

    private String displayName;
    private Map<String,Object> metrics;

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
}
