/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class DictionaryGenerator {

    public static List<Map> createIncludeDictionary() {
        List<Map> dictionary = Lists.newArrayList();
        Map metric1 = Maps.newLinkedHashMap();
        metric1.put("name", "Capacity");
        metric1.put("alias", "CapacityAlias");
        dictionary.add(metric1);
        Map metric2 = Maps.newLinkedHashMap();
        metric2.put("name", "Size");
        metric2.put("alias", "SizeAlias");
        dictionary.add(metric2);
        Map metric3 = Maps.newLinkedHashMap();
        metric3.put("name", "Hits");
        metric3.put("alias", "HitsAlias");
        dictionary.add(metric3);
        Map metric4 = Maps.newLinkedHashMap();
        metric4.put("name", "Requests");
        metric4.put("alias", "RequestsAlias");
        dictionary.add(metric4);
        return dictionary;
    }

    public static List<Map> createIncludeDictionaryWithCompositeObject() {
        List<Map> dictionary = Lists.newArrayList();
        Map metric1 = Maps.newLinkedHashMap();
        metric1.put("name", "HeapMemoryUsage.max");
        metric1.put("alias", "maxAlias");
        dictionary.add(metric1);
        Map metric2 = Maps.newLinkedHashMap();
        metric2.put("name", "HeapMemoryUsage.used");
        metric2.put("alias", "usedAlias");
        dictionary.add(metric2);
        return dictionary;
    }
}
