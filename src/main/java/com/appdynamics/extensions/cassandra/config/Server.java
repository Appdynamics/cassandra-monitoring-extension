package com.appdynamics.extensions.cassandra.config;


import com.appdynamics.extensions.util.metrics.MetricOverride;

public class Server {

    private String host;
    private int port;
    private String serviceUrl;
    private String username;
    private String password;
    private String displayName;
    private MetricOverride[] metricOverrides;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MetricOverride[] getMetricOverrides() {
        return metricOverrides;
    }

    public void setMetricOverrides(MetricOverride[] metricOverrides) {
        this.metricOverrides = metricOverrides;
    }
}
