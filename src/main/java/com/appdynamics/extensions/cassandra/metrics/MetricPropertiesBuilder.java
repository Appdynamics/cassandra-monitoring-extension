package com.appdynamics.extensions.cassandra.metrics;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.cassandra.Constants.INCLUDE;
import static com.appdynamics.extensions.cassandra.Constants.METRICS;

public class MetricPropertiesBuilder {

    public Map<String, MetricProperties> build (Map aConfigMBean) {
        Map<String, MetricProperties> metricPropsMap = Maps.newHashMap();
        if (aConfigMBean == null || aConfigMBean.isEmpty()) {
            return metricPropsMap;
        }
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeMetrics = (List) configMetrics.get(INCLUDE);
        if (includeMetrics != null) {
            for (Object metad : includeMetrics) {
                Map localMetaData = (Map) metad;
                Map.Entry entry = (Map.Entry) localMetaData.entrySet().iterator().next();
                String metricName = entry.getKey().toString();
                String alias = entry.getValue().toString();
                MetricProperties props = new DefaultMetricProperties();
                props.setAlias(alias);
                props.setMetricName(metricName);
                setProps(aConfigMBean, props); //global level
                setProps(localMetaData, props); //local level
                metricPropsMap.put(metricName, props);
            }
        }
        return metricPropsMap;
    }

    private void setProps (Map metadata, MetricProperties props) {
        if (metadata.get("metricType") != null) {
            props.setAggregationFields(metadata.get("metricType").toString());
        }
        if (metadata.get("multiplier") != null) {
            props.setMultiplier(Double.parseDouble(metadata.get("multiplier").toString()));
        }
        if (metadata.get("convert") != null) {
            props.setConversionValues((Map) metadata.get("convert"));
        }
        if (metadata.get("aggregation") != null) {
            props.setAggregation(Boolean.parseBoolean(metadata.get("aggregation").toString()));
        }
        if (metadata.get("delta") != null) {
            props.setDelta(Boolean.parseBoolean(metadata.get("delta").toString()));
        }
    }
}
