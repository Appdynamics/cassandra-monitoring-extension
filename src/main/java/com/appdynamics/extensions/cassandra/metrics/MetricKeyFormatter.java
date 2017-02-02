package com.appdynamics.extensions.cassandra.metrics;


import com.appdynamics.extensions.cassandra.CassandraMBeansKeyPropertiesEnum;
import com.google.common.base.Strings;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

class MetricKeyFormatter {

    private ObjectName getObjectName (ObjectInstance instance) {
        return instance.getObjectName();
    }

    String getInstanceKey (ObjectInstance instance) {
        if (instance == null) {
            return "";
        }
        // Standard jmx keys. {type, scope, name, keyspace, path etc.}
        String type = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.TYPE.toString());
        String domain = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.DOMAIN.toString());
        String subType = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.SUBTYPE.toString());
        String name = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.NAME.toString());
        String service = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.SERVICE.toString());
        String scope = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.SCOPE.toString());
        String cache = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.CACHE.toString());
        String path = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.PATH.toString());

        StringBuilder metricsKey = new StringBuilder();
        metricsKey.append(Strings.isNullOrEmpty(type) ? "" : type + "|");
        metricsKey.append(Strings.isNullOrEmpty(domain) ? "" : domain + "|");
        metricsKey.append(Strings.isNullOrEmpty(subType) ? "" : subType + "|");
        metricsKey.append(Strings.isNullOrEmpty(service) ? "" : service + "|");
        metricsKey.append(Strings.isNullOrEmpty(path) ? "" : path + "|");
        metricsKey.append(Strings.isNullOrEmpty(scope) ? "" : scope + "|");
        metricsKey.append(Strings.isNullOrEmpty(name) ? "" : name + "|");
        metricsKey.append(Strings.isNullOrEmpty(cache) ? "" : cache + "|");

        return metricsKey.toString();
    }

    private String getKeyProperty (ObjectInstance instance, String property) {
        if (instance == null) {
            return "";
        }
        return getObjectName(instance).getKeyProperty(property);
    }

    String getNodeKey (ObjectInstance instance, String metricName, String instanceKey) {
        StringBuilder metricKey = new StringBuilder(instanceKey);
        String tier = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.TIER.toString());
        String responsibility = getKeyProperty(instance, CassandraMBeansKeyPropertiesEnum.RESPONSIBILITY.toString());
        metricKey.append(Strings.isNullOrEmpty(tier) ? "" : tier + "|");
        metricKey.append(Strings.isNullOrEmpty(responsibility) ? "" : responsibility + "|");
        metricKey.append(metricName);
        return metricKey.toString();
    }
}
