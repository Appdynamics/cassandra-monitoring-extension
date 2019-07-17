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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.appdynamics.extensions.cassandra.IntegrationTestUtils.initializeMetricAPIService;
import static com.appdynamics.extensions.util.JsonUtils.getTextValue;


/**
 * @author: {Bhuvnesh Kumar}
 */
public class MetricCheckIT {
    private MetricAPIService metricAPIService;
    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {
        metricAPIService = initializeMetricAPIService();
        customDashboardAPIService = IntegrationTestUtils.initializeCustomDashboardAPIService();
    }

    @After
    public void tearDown() {
        //todo: shutdown client
    }

//    @Test
//    public void whenInstanceIsUpThenHeartBeatIs1ForServerWithSSLDisabled() {
//        JsonNode jsonNode = null;
//        if (metricAPIService != null) {
//            jsonNode = metricAPIService.getMetricData("",
//                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CswingTier%7CCustom%20Metrics%7CCassandra%7CCassandra%20Server%201%7CHeart%20Beat&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
//        }
//        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
//        if (jsonNode != null) {
//            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
//            int heartBeat = (valueNode == null) ? 0 : valueNode.get(0).asInt();
//            Assert.assertEquals("heartbeat is 0", heartBeat, 1);
//        }
//    }
//    @Test
//    public void whenInstanceIsUpThenHeartBeatIs1ForServerWithSSLEnabled() {
//        JsonNode jsonNode = null;
//        if (metricAPIService != null) {
//            jsonNode = metricAPIService.getMetricData("",
//                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CswingTier%7CCustom%20Metrics%7CCassandra%7CCassandra%20Server%201%7CHeart%20Beat&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
//        }
//        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
//        if (jsonNode != null) {
//            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
//            int heartBeat = (valueNode == null) ? 0 : valueNode.get(0).asInt();
//            Assert.assertEquals("heartbeat is 0", 1, heartBeat);
//        }
//    }

//    @Test
//    public void whenMultiplierIsAppliedThenCheckMetricValue() {
//        JsonNode jsonNode = null;
//        if (metricAPIService != null) {
//            jsonNode = metricAPIService.getMetricData("",
//                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CKafka%7CLocal%20Kafka%20Server2%7Ckafka.server%7CKafkaRequestHandlerPool%7CRequestHandlerAvgIdle%25%7COneMinuteRate&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
//        }
//        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
//        if (jsonNode != null) {
//            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
//            int requestHandlerAvgPercent = (valueNode == null) ? 0 : valueNode.get(0).asInt();
//            Assert.assertTrue((requestHandlerAvgPercent > 90) && (requestHandlerAvgPercent <= 100));
//        }
//    }
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

//    @Test
//    public void whenAliasIsAppliedThenCheckMetricName() {
//        JsonNode jsonNode = null;
//        if (metricAPIService != null) {
//            jsonNode = metricAPIService.getMetricData("",
//                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CKafka%7CLocal%20Kafka%20Server2%7Ckafka.server%7CReplicaManager%7CUnderReplicatedPartitions%7CUnderReplicatedPartitions&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
//        }
//        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
//        if (jsonNode != null) {
//            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
//            String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
//            int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
//            Assert.assertEquals("Metric alias is invalid", "\"Custom Metrics|Kafka|Local Kafka Server2|kafka.server|ReplicaManager|UnderReplicatedPartitions|UnderReplicatedPartitions\"", metricName);
//            Assert.assertNotNull("Metric Value is  null in last 15min, maybe a stale metric ", metricValue);
//        }
//    }

    @Test
    public void checkDashboardsUploaded() {//TODO: have a good dashboard
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
            boolean dashboardPresent = isDashboardPresent("Cassandra BTD Dashboard", allDashboardsNode);
            Assert.assertTrue(dashboardPresent);
        }
    }

    private boolean isDashboardPresent(String dashboardName, JsonNode existingDashboards) {
        if (existingDashboards != null) {
            for (JsonNode existingDashboard : existingDashboards) {
                if (dashboardName.equals(getTextValue(existingDashboard.get("name")))) {
                    return true;
                }
            }
        }
        return false;
    }

//    @Test
//    public void checkMetricCharReplaced() {
//        JsonNode jsonNode = null;
//        if (metricAPIService != null) {
//            jsonNode = metricAPIService.getMetricData("",
//                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CKafka%7CLocal%20Kafka%20Server2%7Ckafka.server%7CKafkaRequestHandlerPool%7CRequestHandlerAvgIdle%25%7COneMinuteRate&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
//        }
//        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
//        if (jsonNode != null) {
//            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
//            String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
//            int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
//            Assert.assertEquals("Metric char replacement is not done", "\"Custom Metrics|Kafka|Local Kafka Server2|kafka.server|KafkaRequestHandlerPool|RequestHandlerAvgIdle%|OneMinuteRate\"", metricName);
//            Assert.assertNotNull("Metric Value is  null in last 15min, maybe a stale metric ", metricValue);
//        }
//    }


    @Test
    public void checkWorkBenchUrlIsUp() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://0.0.0.0:9089");
        try {
            CloseableHttpResponse response = httpClient.execute(get);
            Assert.assertEquals(200, response.getStatusLine());
        } catch (IOException ioe) {

        }
    }

}
