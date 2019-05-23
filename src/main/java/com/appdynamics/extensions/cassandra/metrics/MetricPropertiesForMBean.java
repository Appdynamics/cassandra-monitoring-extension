/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.metrics;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.cassandra.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 3/13/19.
 */
class MetricPropertiesForMBean {

    static Map<String, ?> getMapOfProperties(Map<String, ?> mBean) {

        Map<String, ? super Object> metricPropsMap = Maps.newHashMap();
        if (mBean == null || mBean.isEmpty()) {
            return metricPropsMap;
        }
        Map<String, ?> configMetrics = (Map<String, ?>) mBean.get(METRICS);
        List includeMetrics = (List) configMetrics.get(INCLUDE);

        if (includeMetrics != null) {
            for (Object metad : includeMetrics) {
                Map<String, ?> localMetaData = (Map<String, ?>) metad;
                String metricName = (String) localMetaData.get("name");
                Map<String, ? super Object> metricProperties = new HashMap<String, Object>();

                setProps(mBean, metricProperties, metricName); //global level
                setProps(localMetaData, metricProperties, metricName); //local level
                metricPropsMap.put(metricName, metricProperties);
            }
        }
        return metricPropsMap;
    }

    private static void setProps(Map<String, ?> metadata, Map props, String metricName) {
        if (metadata.get(ALIAS) != null) {
            props.put(ALIAS, metadata.get(ALIAS).toString());
        } else {
            if (props.get(ALIAS) == null) {
                props.put(ALIAS, metricName);
            }
        }
        if (metadata.get(MULTIPLIER) != null) {
            props.put(MULTIPLIER, metadata.get(MULTIPLIER).toString());
        }
        if (metadata.get(CONVERT) != null) {
            props.put(CONVERT, metadata.get(CONVERT));
        }
        if (metadata.get(DELTA) != null) {
            props.put(DELTA, metadata.get(DELTA).toString());
        }
        if (metadata.get(CLUSTERROLLUPTYPE) != null) {
            props.put(CLUSTERROLLUPTYPE, metadata.get(CLUSTERROLLUPTYPE).toString());

        }
        if (metadata.get(TIMEROLLUPTYPE) != null) {
            props.put(TIMEROLLUPTYPE, metadata.get(TIMEROLLUPTYPE).toString());
        }
        if (metadata.get(AGGREGATIONTYPE) != null) {
            props.put(AGGREGATIONTYPE, metadata.get(AGGREGATIONTYPE).toString());
        }
    }

    public static List<String> getMBeanKeys(Map<String, ?> aConfigMBean) {
        return (List<String>) aConfigMBean.get(MBEANKEYS);
    }


}
