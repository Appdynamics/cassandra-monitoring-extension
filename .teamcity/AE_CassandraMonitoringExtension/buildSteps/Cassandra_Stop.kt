/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package ExtensionsJMX_KafkaMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs


import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import ExtensionsJMX_KafkaMonitoringExtension.vcsRoots.*
import ExtensionsJMX_KafkaMonitoringExtension.buildTypes.*


object Cassandra_Stop : BuildType({
    uuid = "adc25bd3-02fb-4561-b335-2d344494f9d2"
    name = "Stop and Remove all Docker Containers"

    vcs {
        root(kafkamonitoringextensionci)
    }

    steps {
        exec {
            path = "make"
            arguments = "dockerStop"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        dependency(ExtensionsJMX_KafkaMonitoringExtension_SetupKafka) {
            snapshot{
                runOnSameAgent = true
            }
        }
    }


})
