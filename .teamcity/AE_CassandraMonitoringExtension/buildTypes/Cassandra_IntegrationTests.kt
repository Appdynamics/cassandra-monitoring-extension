/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package AE_CassandraMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import AE_CassandraMonitoringExtension.vcsRoots.*
import AE_CassandraMonitoringExtension.buildTypes.*;

object Cassandra_IntegrationTests : BuildType({
    uuid = "7BE210AD-581F-42A3-9938-FD5482212236"
    name = "Run Integration Tests - Linux"

    vcs {
        root(cassandramonitoringextension)
    }

    steps {
        maven {

            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    dependencies {
        dependency(Cassandra_Setup) {
            snapshot {
                runOnSameAgent = true
            }
        }
    }

    triggers {
        vcs {
        }
    }
})
