package com.appdynamics.extensions.cassandra.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

public class ConfigUtilTest {

    private ConfigUtil<Configuration> configUtil = new ConfigUtil();

    @Before
    public void setup(){

    }


    @Test
    public void loadConfigSuccessfully() throws FileNotFoundException {
        Configuration configuration = configUtil.readConfig("src/test/resources/conf/config.yml", Configuration.class);
        Assert.assertTrue(configuration !=  null);
        Assert.assertTrue(configuration.getServers() != null);
    }
}
