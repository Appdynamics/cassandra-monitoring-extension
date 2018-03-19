/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.cassandra;

public enum CassandraMBeansKeyPropertiesEnum {

    NODEID("nodeId"), SERVICE("service"), RESPONSIBILITY("responsibility"), DOMAIN("Domain"), SUBTYPE("subType"),
    CACHE("cache"), TYPE("type"), SCOPE("scope"), NAME("name"), KEYSPACE("keyspace"), PATH("path"), TIER("tier");

    private final String name;

    CassandraMBeansKeyPropertiesEnum (String name) {
        this.name = name;
    }

    public String toString () {
        return name;
    }
}
