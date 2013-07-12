package com.appdynamics.monitors.cassandra.metrics;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

public class Metric
{
	public String name;
	public ObjectName bean;
	public MBeanAttributeInfo[] attrs;
}
