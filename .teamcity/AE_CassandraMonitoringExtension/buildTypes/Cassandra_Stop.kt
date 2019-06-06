/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package AE_CassandraMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs


import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import AE_CassandraMonitoringExtension.vcsRoots.*
import AE_CassandraMonitoringExtension.buildTypes.*


object Cassandra_Stop : BuildType({
    uuid = "adc25bd3-02fb-4561-b335-2d344494f9d2"
    name = "Stop and Remove all Docker Containers"

    vcs {
        root(cassandramonitoringextension)
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
        dependency(Cassandra_Setup) {
            snapshot{
                runOnSameAgent = true
            }
        }
    }


})
