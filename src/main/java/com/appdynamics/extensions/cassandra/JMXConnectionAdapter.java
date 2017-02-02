package com.appdynamics.extensions.cassandra;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JMXConnectionAdapter {

    private final JMXServiceURL serviceUrl;
    private final String username;
    private final String password;

    private JMXConnectionAdapter (String host, int port, String username, String password) throws
            MalformedURLException {
        this.serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        this.username = username;
        this.password = password;
    }

    private JMXConnectionAdapter (String serviceUrl, String username, String password) throws MalformedURLException {
        this.serviceUrl = new JMXServiceURL(serviceUrl);
        this.username = username;
        this.password = password;
    }


    static JMXConnectionAdapter create (String serviceUrl, String host, int port, String username, String password)
            throws MalformedURLException {
        if (Strings.isNullOrEmpty(serviceUrl)) {
            return new JMXConnectionAdapter(host, port, username, password);
        } else {
            return new JMXConnectionAdapter(serviceUrl, username, password);
        }
    }

    JMXConnector open () throws IOException {
        JMXConnector jmxConnector;
        final Map<String, Object> env = new HashMap<String, Object>();
        if (!Strings.isNullOrEmpty(username)) {
            env.put(JMXConnector.CREDENTIALS, new String[]{username, password});
            jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
        } else {
            jmxConnector = JMXConnectorFactory.connect(serviceUrl);
        }
        if (jmxConnector == null) {
            throw new IOException("Unable to connect to Mbean server");
        }
        return jmxConnector;
    }

    void close (JMXConnector jmxConnector) throws IOException {
        if (jmxConnector != null) {
            jmxConnector.close();
        }
    }

    public Set<ObjectInstance> queryMBeans (JMXConnector jmxConnection, ObjectName objectName) throws IOException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        return connection.queryMBeans(objectName, null);
    }

    public List<String> getReadableAttributeNames (JMXConnector jmxConnection, ObjectInstance instance) throws
            IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        List<String> attrNames = Lists.newArrayList();
        MBeanAttributeInfo[] attributes = connection.getMBeanInfo(instance.getObjectName()).getAttributes();
        for (MBeanAttributeInfo attr : attributes) {
            if (attr.isReadable()) {
                attrNames.add(attr.getName());
            }
        }
        return attrNames;
    }

    public List<Attribute> getAttributes (JMXConnector jmxConnection, ObjectName objectName, String[] strings) throws
            IOException, ReflectionException, InstanceNotFoundException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        AttributeList list = connection.getAttributes(objectName, strings);
        if (list != null) {
            return list.asList();
        }
        return Lists.newArrayList();
    }
}
