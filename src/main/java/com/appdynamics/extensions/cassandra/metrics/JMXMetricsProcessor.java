/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.cassandra.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.cassandra.filters.IncludeFilter;
import com.appdynamics.extensions.cassandra.metrics.processor.BaseMetricsProcessor;
import com.appdynamics.extensions.cassandra.metrics.processor.CompositeMetricsProcessor;
import com.appdynamics.extensions.cassandra.metrics.processor.ListMetricsProcessor;
import com.appdynamics.extensions.cassandra.metrics.processor.MapMetricsProcessor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.cassandra.metrics.MetricPropertiesForMBean.getMBeanKeys;
import static com.appdynamics.extensions.cassandra.metrics.MetricPropertiesForMBean.getMapOfProperties;
import static com.appdynamics.extensions.cassandra.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 12/19/18.
 */
public class JMXMetricsProcessor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JMXMetricsProcessor.class);
    private JMXConnectionAdapter jmxConnectionAdapter;
    private JMXConnector jmxConnector;
    private MonitorContextConfiguration monitorContextConfiguration;

    public JMXMetricsProcessor(MonitorContextConfiguration monitorContextConfiguration, JMXConnectionAdapter jmxConnectionAdapter, JMXConnector jmxConnector) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.jmxConnectionAdapter = jmxConnectionAdapter;
        this.jmxConnector = jmxConnector;
    }

    public List<Metric> getJMXMetrics(Map<String, ?> mBean, String metricPrefix, String displayName) throws
            JMException, IOException {
        List<Metric> jmxMetrics = Lists.newArrayList();
        String configObjectName = (String) mBean.get(OBJECT_NAME);
        AssertUtils.assertNotNull(configObjectName, "Metric Object Name can not be Empty");
        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        logger.debug("Processing for Object : {} ", configObjectName);
        for (ObjectInstance instance : objectInstances) {
            List<String> readableAttributes = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            Set<String> metricNamesToBeExtracted = applyFilters(mBean, readableAttributes);
            List<Attribute> attributes = jmxConnectionAdapter.getAttributes(jmxConnector, instance.getObjectName(),
                    metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            if (!attributes.isEmpty()) {

                MetricDetails metricDetails = getMetricDetails(mBean, metricPrefix, displayName, instance);
                jmxMetrics.addAll(collectMetrics(metricDetails, attributes));
            } else {
                logger.debug("No attributes found for Object : {} ", configObjectName);
            }
        }
        return jmxMetrics;
    }

    private MetricDetails getMetricDetails(Map<String, ?> mBean, String metricPrefix, String displayName, ObjectInstance instance) {

        return new MetricDetails.Builder()
                .metricPrefix(metricPrefix)
                .instance(instance)
                .metricPropsPerMetricName(getMapOfProperties(mBean))
                .mBeanKeys(getMBeanKeys(mBean))
                .displayName(displayName)
                .separator(getSeparator())
                .build();
    }

    private Set<String> applyFilters(Map<String, ?> aConfigMBean, List<String> readableAttributes) {
        Set<String> filteredSet = Sets.newHashSet();
        Map<String, ?> configMetrics = (Map<String, ?>) aConfigMBean.get(METRICS);
        List<Map<String, ?>> includeDictionary = (List<Map<String, ?>>) configMetrics.get(INCLUDE);
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, readableAttributes);
        return filteredSet;
    }

    private List<Metric> collectMetrics(MetricDetails metricDetails, List<Attribute> attributes) {
        List<Metric> jmxMetrics = new ArrayList<Metric>();
        logger.debug("Working to get values from attributes for {}", metricDetails.getInstance().toString());
        for (Attribute attribute : attributes) {
            try {
                jmxMetrics.addAll(checkTypeAndReturnMetrics(metricDetails, attribute));
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", metricDetails.getInstance().getObjectName(), attribute.getName(), e);
            }
        }
        return jmxMetrics;
    }

    private String getSeparator() {
        String separator = (String) monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS);
        if (Strings.isNullOrEmpty(separator)) {
            separator = COLON;
        }
        return separator;
    }

    private List<Metric> checkTypeAndReturnMetrics(MetricDetails metricDetails, Attribute attribute) {

        BaseMetricsProcessor jmxMetricProcessor = getReference(attribute);
        jmxMetricProcessor.populateMetricsFromEntity(metricDetails, attribute);
        return jmxMetricProcessor.getMetrics();
    }

    private static BaseMetricsProcessor getReference(Attribute attribute) {
        Object object = attribute.getValue();

        if (object instanceof CompositeData) {
            return new CompositeMetricsProcessor();
        } else if (object instanceof List) {
            return new ListMetricsProcessor();
        } else if (object instanceof Map) {
            return new MapMetricsProcessor();
        } else {
            return new BaseMetricsProcessor();
        }
    }
}
