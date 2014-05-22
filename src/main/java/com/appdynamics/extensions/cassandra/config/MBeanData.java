package com.appdynamics.extensions.cassandra.config;


import java.util.HashSet;
import java.util.Set;

public class MBeanData {

    private String domainName;
    private Set<String> excludePatterns = new HashSet<String>();

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public Set<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(Set<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
}
