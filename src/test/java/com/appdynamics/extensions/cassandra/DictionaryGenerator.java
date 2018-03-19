/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class DictionaryGenerator {

    public static List<Map> createIncludeDictionary () {
        List<Map> dictionary = Lists.newArrayList();
        Map metric1 = Maps.newLinkedHashMap();
        metric1.put("Capacity", "CapacityAlias");
        dictionary.add(metric1);
        Map metric2 = Maps.newLinkedHashMap();
        metric2.put("Size", "SizeAlias");
        dictionary.add(metric2);
        Map metric3 = Maps.newLinkedHashMap();
        metric3.put("Hits", "HitsAlias");
        dictionary.add(metric3);
        Map metric4 = Maps.newLinkedHashMap();
        metric4.put("Requests", "RequestsAlias");
        dictionary.add(metric4);
        return dictionary;
    }

    public static List<Map> createIncludeDictionaryWithCompositeObject() {
        List<Map> dictionary = Lists.newArrayList();
        Map metric1 = Maps.newLinkedHashMap();
        metric1.put("HeapMemoryUsage.max", "maxAlias");
        dictionary.add(metric1);
        Map metric2 = Maps.newLinkedHashMap();
        metric2.put("HeapMemoryUsage.used", "usedAlias");
        dictionary.add(metric2);
        return dictionary;
    }
}
