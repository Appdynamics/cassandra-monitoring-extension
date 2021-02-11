/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.cassandra;

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author: {Bhuvnesh Kumar}
 */
public class MetricCheckIT {
    private MetricAPIService metricAPIService;
    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {
        metricAPIService = IntegrationTestUtils.initializeMetricAPIService();
        customDashboardAPIService = IntegrationTestUtils.initializeCustomDashboardAPIService();
    }

    @Test
    public void whenInstanceIsUpThenHeartBeatIs1ForServer() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CCassandra%7CLocal%20Cassandra%20Server%201%7CHeart%20Beat&time-range-type=BEFORE_NOW&duration-in-mins=60&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            int heartBeat = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals("heartbeat is 0", heartBeat, 1);
        }
    }

    @Test
    public void checkTotalNumberOfMetricsReportedIsGreaterThan1() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CCassandra%7CMetrics%20Uploaded&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            int totalNumberOfMetricsReported = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertTrue(totalNumberOfMetricsReported > 1);
        }
    }

    @Test
    public void whenAliasIsAppliedThenCheckMetricName() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CCassandra%7CLocal%20Cassandra%20Server%201%7CCache%7CChunkCache%7CSize%7CCache%20Size&time-range-type=BEFORE_NOW&duration-in-mins=60&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
            String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
            int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals("Metric alias is invalid", "\"Custom Metrics|Cassandra|Local Cassandra Server 1|Cache|ChunkCache|Size|Cache Size\"", metricName);
            Assert.assertNotNull("Metric Value is  null in last 15min, maybe a stale metric ", metricValue);
        }
    }

    @Test
    public void checkDashboardsUploaded() {
        boolean dashboardPresent = false;
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
             dashboardPresent = IntegrationTestUtils.isDashboardPresent("Cassandra SIM Dashboard", allDashboardsNode);
            Assert.assertTrue(dashboardPresent);
        } else {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void checkMetricCharReplaced() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CCassandra%7CLocal%20Cassandra%20Server%201%7CCommit%20Log%7CCompletedTasks%7CNumber%20of%20Completed%20Tasks&time-range-type=BEFORE_NOW&duration-in-mins=60&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
            String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
            int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals("Metric char replacement is not done", "\"Custom Metrics|Cassandra|Local Cassandra Server 1|Commit Log|CompletedTasks|Number of Completed Tasks\"", metricName);
            Assert.assertNotNull("Metric Value is  null in last 15min, maybe a stale metric ", metricValue);
        }
    }
}
