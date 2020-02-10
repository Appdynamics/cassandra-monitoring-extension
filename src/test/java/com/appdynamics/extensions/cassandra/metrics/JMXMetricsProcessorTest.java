/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.cassandra.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.remote.JMXConnector;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class JMXMetricsProcessorTest {

    private JMXConnector jmxConnector = mock(JMXConnector.class);
    private JMXConnectionAdapter jmxConnectionAdapter = mock(JMXConnectionAdapter.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private String metricPrefix;

    @Before
    public void before() {
        Map<String, ?> conf = YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml"));
        ABaseMonitor baseMonitor = mock(ABaseMonitor.class);
        monitorContextConfiguration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(baseMonitor.getContextConfiguration()).thenReturn(monitorContextConfiguration);
        when(monitorContextConfiguration.getContext()).thenReturn(context);
        when(monitorContextConfiguration.getMetricPrefix()).thenReturn("Custom Metrics|JMX Monitor");
        metricPrefix = "Custom Metrics|JMX Monitor";
        MetricPathUtils.registerMetricCharSequenceReplacer(baseMonitor);
        MetricCharSequenceReplacer replacer = MetricCharSequenceReplacer.createInstance(conf);
        when(context.getMetricCharSequenceReplacer()).thenReturn(replacer);
        MetricWriter metricWriter = mock(MetricWriter.class);
        when(baseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
    }

    @Test
    public void getNodeMetrics_NonCompositeObject() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_without_composite_object.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.activemq.metricsold:type=ClientRequest,scope=Read,name=Latency", "test"));

        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        attributes.add(new Attribute("Min", new BigDecimal(100)));

        List<String> metricNames = Lists.newArrayList();
        metricNames.add("Max");
        metricNames.add("Min");
        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);
        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");
        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Min"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("Min"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("100"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("AVERAGE"));
    }

    @Test
    public void getNodeMetrics_CompositeObject() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource
                ("/conf/config_with_composite_object.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("java.lang:type=Memory", "test"));
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("HeapMemoryUsage", createCompositeDataSupportObject()));
        List<String> metricNames = Lists.newArrayList();
        metricNames.add("HeapMemoryUsage.max");
        metricNames.add("HeapMemoryUsage.used");
        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);
        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|Memory|HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("100"));

        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|Memory|HeapMemoryUsage.used"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("HeapMemoryUsage.used"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("50"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("AVERAGE"));

    }

    @Test
    public void getNodeMetrics_compositeAndNonCompositeObjects() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_with_composite_and_noncomposite_objects.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("java.lang:type=Memory", "test"));
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("ObjectPendingFinalizationCount", 0));
        attributes.add(new Attribute("HeapMemoryUsage", createCompositeDataSupportObject()));
        List<String> metricNames = Lists.newArrayList();
        metricNames.add("ObjectPendingFinalizationCount");
        metricNames.add("HeapMemoryUsage.used");

        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);

        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|Memory|ObjectPendingFinalizationCount"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("ObjectPendingFinalizationCount"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("0"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|Memory|HeapMemoryUsage.used"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("HeapMemoryUsage.used"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("50"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("AVERAGE"));
    }


    @Test
    public void testGlobalProperties() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_for_global_metadata.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.cassandra.metricsold:type=ClientRequest,scope=Read,name=Latency", "test"));
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        attributes.add(new Attribute("Min", new BigDecimal(100)));
        List<String> metricNames = Lists.newArrayList();
        metricNames.add("Max");
        metricNames.add("Min");

        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);

        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("SUM"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("SUM"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Min"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("Min"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("100"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("SUM"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("SUM"));

    }

    @Test
    public void testlocalProperties() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_for_local_metadata.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.cassandra.metricsold:type=ClientRequest,scope=Read,name=Latency", "test"));

        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        attributes.add(new Attribute("Min", new BigDecimal(100)));

        List<String> metricNames = Lists.newArrayList();
        metricNames.add("Max");
        metricNames.add("Min");

        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);

        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("SUM"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("SUM"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Min"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("Min"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("100"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("SUM"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("SUM"));

    }

    @Test
    public void checkMetricPropertiesFromConfig() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_with_props.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.cassandra.metricsold:type=ClientRequest,scope=Read,name=Latency", "test"));
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        List<String> metricNames = Lists.newArrayList();
        metricNames.add("Max");

        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);

        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");
        Map<Object, Object> metricProps = metrics.get(0).getMetricProperties().getConversionValues();

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("OBSERVATION"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metricProps.containsKey("ENDANGERED"));
        Assert.assertTrue(metricProps.get("ENDANGERED").equals("1"));
        Assert.assertTrue(metricProps.containsKey("NODE-SAFE"));
        Assert.assertTrue(metricProps.get("NODE-SAFE").equals("2"));
        Assert.assertTrue(metricProps.containsKey("MACHINE-SAFE"));
        Assert.assertTrue(metricProps.get("MACHINE-SAFE").equals("3"));

    }


    private CompositeDataSupport createCompositeDataSupportObject() throws JMException {
        String typeName = "type";
        String description = "description";
        String[] itemNames = {"max", "used"};
        String[] itemDescriptions = {"maxDesc", "usedDesc"};
        OpenType<?>[] itemTypes = new OpenType[]{new OpenType("java.lang.String", "type", "description") {
            @Override
            public boolean isValue(Object obj) {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return "100";
            }
        }, new OpenType("java.lang.String", "type", "description") {
            @Override
            public boolean isValue(Object obj) {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return "50";
            }
        }};

        CompositeType compositeType = new CompositeType(typeName, description, itemNames, itemDescriptions, itemTypes);

        String[] itemNamesForCompositeDataSupport = {"max", "used"};
        Object[] itemValuesForCompositeDataSupport = {new BigDecimal(100), new BigDecimal(50)};
        return new CompositeDataSupport(compositeType, itemNamesForCompositeDataSupport,
                itemValuesForCompositeDataSupport);
    }

    @Test
    public void getSingleAndMultiLevelMapMetricsThroughJMX() throws JMException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_with_map.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.activemq.metricsold:type=ClientRequest,scope=Read,name=Latency", "test"));
        Map map3 = new HashMap();
        map3.put("key31", 31);
        map3.put("key32", 32);
        Map map2 = new HashMap();
        map2.put("key1", 1);
        map2.put("key2", 2);
        map2.put("map3", map3);
        Map attr1 = new HashMap();
        attr1.put("key1", 1);
        attr1.put("key2", 2);
        attr1.put("map2", map2);
        attr1.put("key4", 4);
        Attribute mapAttribute = new Attribute("MapOfString", attr1);
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(mapAttribute);
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        attributes.add(new Attribute("HeapMemoryUsage", createCompositeDataSupportObject()));
        List<String> metricNames = Lists.newArrayList();
        metricNames.add("MapOfString.key1");
        metricNames.add("MapOfString.key2");
        metricNames.add("MapOfString.map2.key2");
        metricNames.add("MapOfString.map2.map3.key32");
        metricNames.add("Max");
        metricNames.add("HeapMemoryUsage.max");
        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);
        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration, jmxConnectionAdapter, jmxConnector);
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPrefix, "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|MapOfString.key1"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("MapOfString.key1"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("1"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|MapOfString.key2"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("MapOfString.key2"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("2"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getAggregationType().equals("OBSERVATION"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(1).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(2).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|MapOfString.map2.key2"));
        Assert.assertTrue(metrics.get(2).getMetricName().equals("MapOfString.map2.key2"));
        Assert.assertTrue(metrics.get(2).getMetricValue().equals("2"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(3).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|MapOfString.map2.map3.key32"));
        Assert.assertTrue(metrics.get(3).getMetricName().equals("MapOfString.map2.map3.key32"));
        Assert.assertTrue(metrics.get(3).getMetricValue().equals("32"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(4).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(4).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(4).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getAggregationType().equals("OBSERVATION"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(5).getMetricPath().equals("Custom Metrics|JMX Monitor|ClientRequest|Read|Latency|HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(5).getMetricName().equals("HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(5).getMetricValue().equals("100"));
        Assert.assertTrue(metrics.get(5).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(5).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(5).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(5).getMetricProperties().getDelta() == false);
        Assert.assertTrue(metrics.get(5).getMetricProperties().getMultiplier().compareTo(new BigDecimal(10)) == 0);
    }

}

