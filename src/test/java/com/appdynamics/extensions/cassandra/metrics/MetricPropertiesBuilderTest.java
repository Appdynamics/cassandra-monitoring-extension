package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.cassandra.DictionaryGenerator;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class MetricPropertiesBuilderTest {

    @Test
    public void whenIncludeMetricsIsNull_thenReturnEmptyMap(){
        MetricPropertiesBuilder builder = new MetricPropertiesBuilder();
        Map<String,MetricProperties> propsMap = builder.build(null);
        Assert.assertTrue(propsMap.size() == 0);
    }

    @Test
    public void whenIncludeMetricsIsEmpty_thenReturnEmptyMap(){
        MetricPropertiesBuilder builder = new MetricPropertiesBuilder();
        Map<String,MetricProperties> propsMap = builder.build(Maps.newHashMap());
        Assert.assertTrue(propsMap.size() == 0);
    }

    @Test
    public void buildMetricPropertyMap_whenMBeanHasIncludeMetrics() {
        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder();
        List<Map> dictionary = DictionaryGenerator.createIncludeDictionary();
        Map metricsMap = getMetricsMap(dictionary);
        Map<String, MetricProperties> metricPropertiesMap = metricPropertiesBuilder.build(metricsMap);
        Assert.assertTrue(metricPropertiesMap.size() == dictionary.size());
        for (Map.Entry<String, MetricProperties> entry : metricPropertiesMap.entrySet()) {
            MetricProperties metricPropValue = entry.getValue();
            Assert.assertTrue(metricPropValue.getAggregationType().equals(MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE));
            Assert.assertTrue(metricPropValue.getTimeRollupType().equals(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE));
            Assert.assertTrue(metricPropValue.getClusterRollupType().equals(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL));
        }
    }

    private Map getMetricsMap(List<Map> dictionary) {
        Map includeMap = Maps.newHashMap();
        includeMap.put("include", dictionary);
        Map metricsMap = Maps.newHashMap();
        metricsMap.put("metrics", includeMap);
        return metricsMap;
    }




}
