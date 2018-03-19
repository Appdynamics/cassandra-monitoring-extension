/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra.metrics;

import org.junit.Assert;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * Created by adityajagtiani on 1/30/17.
 */
public class MetricKeyFormatterTest {

    MetricKeyFormatter metricKeyFormatter = new MetricKeyFormatter();

    @Test
    public void whenObjectInstanceIsNull_thenReturnEmpty(){
        Assert.assertTrue(metricKeyFormatter.getInstanceKey(null).isEmpty());
    }

    @Test
    public void whenValidObjectInstance_thenReturnValidPrefix() throws MalformedObjectNameException {
        ObjectInstance instance = new ObjectInstance(new ObjectName("org.apache.cassandra.metrics:type=Cache,scope=KeyCache,name=Capacity"),this.getClass().getName());
        String prefix = metricKeyFormatter.getInstanceKey(instance);
        Assert.assertTrue(prefix.equals("Cache|KeyCache|Capacity|"));
    }

   @Test
    public void whenAllArgsValid_thenReturnNodeKey() throws MalformedObjectNameException {
        ObjectInstance instance = new ObjectInstance(new ObjectName("org.apache.cassandra.metrics:type=Cache,scope=KeyCache,name=Capacity"),this.getClass().getName());
        String prefix = metricKeyFormatter.getInstanceKey(instance);
        String nodeKey = metricKeyFormatter.getNodeKey(instance, "Cache Capacity (MB)", prefix);
        Assert.assertTrue(nodeKey.equals("Cache|KeyCache|Capacity|Cache Capacity (MB)"));
    }

   @Test
    public void whenSomeArgsValid_thenShouldNotThrowExceptions() throws MalformedObjectNameException {
        ObjectInstance instance = new ObjectInstance(new ObjectName("org.apache.cassandra.metrics:type=Cache,scope=KeyCache,name=Capacity"),this.getClass().getName());
        String prefix = metricKeyFormatter.getInstanceKey(instance);
        String nodeKey = metricKeyFormatter.getNodeKey(instance,"Value",prefix);
        Assert.assertTrue(!nodeKey.isEmpty());
    }

}
