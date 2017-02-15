package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.cassandra.JMXConnectionAdapter;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class NodeMetricsProcessorTest {

    JMXConnector jmxConnector = mock(JMXConnector.class);
    JMXConnectionAdapter jmxConnectionAdapter = mock(JMXConnectionAdapter.class);

    @Test
    public void getNodeMetrics () throws MalformedObjectNameException, IntrospectionException, ReflectionException,
            InstanceNotFoundException, IOException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.cassandra.metrics:type=Cache,scope=KeyCache," +
                "name=Capacity", "test"));

        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(new Attribute("Value", new BigDecimal(100)));

        List<String> metricNames = Lists.newArrayList();
        metricNames.add("metric1");
        metricNames.add("metric2");

        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn
                (objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class)))
                .thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[]
                .class))).thenReturn(attributes);

        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder();

        NodeMetricsProcessor nodeMetricsProcessor = new NodeMetricsProcessor(jmxConnectionAdapter, jmxConnector);

        Map<String, MetricProperties> metricPropertiesMap = metricPropertiesBuilder.build(mBeans.get(0));

        List<Metric> metrics = nodeMetricsProcessor.getNodeMetrics(mBeans.get(0), metricPropertiesMap);
        Assert.assertTrue(metrics.get(0).getMetricKey().equals("Cache|KeyCache|Capacity|Cache Capacity (MB)"));
        Assert.assertTrue(metrics.get(0).getInstanceKey().equals("Cache|KeyCache|Capacity|"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals(new BigDecimal(100)));
        Assert.assertTrue(metrics.get(0).getProperties().equals(metricPropertiesMap.get("Value")));
    }
}
