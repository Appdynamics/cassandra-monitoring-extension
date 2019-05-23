/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.metrics.processor;

import com.appdynamics.extensions.cassandra.metrics.MetricDetails;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import javax.management.Attribute;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.cassandra.utils.Constants.EMPTY_STRING;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class BaseMetricsProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(BaseMetricsProcessor.class);
    protected List<Metric> metrics;

    public BaseMetricsProcessor() {
        this.metrics = new ArrayList<>();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    private static void getInstanceKey(ObjectInstance instance, List<String> mBeanKeys, LinkedList<String> metricTokens, String displayName) {
        if (!Strings.isNullOrEmpty(displayName)) {
            metricTokens.add(displayName);
        }

        for (String key : mBeanKeys) {
            String value = getKeyProperty(instance, key);
            metricTokens.add(Strings.isNullOrEmpty(value) ? EMPTY_STRING : value);
        }
    }

    private static void addAttributeNameToMetricPathTokens(String attributeName, LinkedList<String> metricTokens) {
            metricTokens.add(attributeName);
    }

    private static ObjectName getObjectName(ObjectInstance instance) {
        return instance.getObjectName();
    }

    private static String getKeyProperty(ObjectInstance instance, String property) {
        if (instance == null) {
            return EMPTY_STRING;
        }
        return getObjectName(instance).getKeyProperty(property);
    }

    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        Map<String, ?> props = (Map<String, ?>) metricDetails.getMetricPropsPerMetricName().get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        } else {
            LinkedList<String> metricTokens = new LinkedList<>();
            getInstanceKey(metricDetails.getInstance(), metricDetails.getmBeanKeys(), metricTokens, metricDetails.getDisplayName());
            addAttributeNameToMetricPathTokens(attributeName, metricTokens);
            String attrVal = getAttrValue(attribute);
            if (!attrVal.equals("")) {
                String[] tokens = new String[metricTokens.size()];
                tokens = metricTokens.toArray(tokens);
                metrics.add(new Metric(attributeName, attrVal, props, metricDetails.getMetricPrefix(), tokens));
            }
        }
    }

    private String getAttrValue(Attribute attribute) {
        String attrVal = attribute.getValue().toString();
        char[] chars = attrVal.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        if (chars[0] != '-') {
            for (int i = 0; i < attrVal.length(); i++) {
                if (Character.toString(attrVal.charAt(i)).matches("[0-9]")) {
                    stringBuilder.append(attrVal.charAt(i));
                } else {
                    return stringBuilder.toString();
                }
            }
        } else{
            return attrVal;
        }
        return stringBuilder.toString();
    }

}
