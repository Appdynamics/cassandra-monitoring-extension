package com.appdynamics.extensions.cassandra.metrics;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

public class MetricValueTransformerTest {
    MetricValueTransformer mvc = new MetricValueTransformer();

    @Test
    public void whenMatchingConversionValuesAndMultiplier_thenConvert () {
        MetricProperties props = new MetricProperties();
        Map<Object, Object> conversionValues = Maps.newHashMap();
        conversionValues.put("ENDANGERED", 2);
        props.setConversionValues(conversionValues);
        props.setMultiplier(2);
        BigDecimal retValue = mvc.transform("StatusHA", "ENDANGERED", props);
        Assert.assertTrue(retValue.intValue() == 4);
    }

    @Test
    public void whenNullConversionValues_thenConvert () {
        MetricProperties props = new MetricProperties();
        props.setConversionValues(null);
        props.setMultiplier(2);
        BigDecimal retValue = mvc.transform("CacheHits", 2.4, props);
        Assert.assertTrue(retValue.intValue() == 4);
    }

    @Test
    public void whenEmptyConversionNoMultipliers_thenConvert () {
        MetricProperties props = new MetricProperties();
        props.setConversionValues(Maps.newHashMap());
        BigDecimal retValue = mvc.transform("CacheHits", 2.4, props);
        Assert.assertTrue(retValue.intValue() == 2);
    }

    @Test(expected = Exception.class)
    public void whenNoConversion_thenReturn () {
        MetricProperties props = new MetricProperties();
        Map<Object, Object> conversionValues = Maps.newHashMap();
        conversionValues.put("MACHINE-SAFE", 2);
        props.setConversionValues(conversionValues);
        BigDecimal retValue = mvc.transform("StatusHA", "ENDANGERED", props);
    }
}
