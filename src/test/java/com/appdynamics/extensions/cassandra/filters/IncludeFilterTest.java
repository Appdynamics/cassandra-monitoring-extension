package com.appdynamics.extensions.cassandra.filters;

import com.appdynamics.extensions.cassandra.DictionaryGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class IncludeFilterTest {

    @Test
    public void whenAttribsMatch_thenIncludeMetrics(){
        List dictionary = DictionaryGenerator.createIncludeDictionary();
        List<String> metrics = Lists.newArrayList("Capacity","Size","Storage","Load");
        IncludeFilter filter = new IncludeFilter(dictionary);
        Set<String> filteredSet = Sets.newHashSet();
        filter.applyFilter(filteredSet,metrics);
        Assert.assertTrue(filteredSet.contains("Capacity"));
        Assert.assertTrue(filteredSet.contains("Size"));
        Assert.assertTrue(!filteredSet.contains("Storage"));
        Assert.assertTrue(!filteredSet.contains("Load"));
    }

    @Test
    public void whenNullDictionary_thenReturnUnchangedSet(){
        List<String> metrics = Lists.newArrayList("Capacity","Size");
        IncludeFilter filter = new IncludeFilter(null);
        Set<String> filteredSet = Sets.newHashSet();
        filter.applyFilter(filteredSet,metrics);
        Assert.assertTrue(filteredSet.size() == 0);
    }

    @Test
    public void whenEmptyDictionary_thenReturnUnchangedSet(){
        List dictionary = Lists.newArrayList();
        List<String> metrics = Lists.newArrayList("Capacity","Size");
        IncludeFilter filter = new IncludeFilter(dictionary);
        Set<String> filteredSet = Sets.newHashSet("Hits");
        filter.applyFilter(filteredSet,metrics);
        Assert.assertTrue(filteredSet.size() == 1);
    }
}
