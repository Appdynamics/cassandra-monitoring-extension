/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package ExtensionsJMX_KafkaMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import ExtensionsJMX_KafkaMonitoringExtension.vcsRoots.*
import ExtensionsJMX_KafkaMonitoringExtension.buildTypes.*;

object Cassandra_IntegrationTests : BuildType({
    uuid = "7BE210AD-581F-42A3-9938-FD5482212236"
    name = "Run Integration Tests - Linux"

    vcs {
        root(kafkamonitoringextensionci)
    }

    steps {
        maven {

            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    dependencies {
        dependency(ExtensionsJMX_KafkaMonitoringExtension_SetupKafka) {
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
