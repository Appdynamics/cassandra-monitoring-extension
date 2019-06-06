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



object Cassandra_Setup : BuildType({
    uuid = "129f1153-3e0f-4794-a46b-8a1600da7692"
    name = "Setup Cassandra Controller and Machine Agent in Docker Containers"

    vcs {
        root(cassandramonitoringextension)
    }

    steps {
        exec {
            path = "make"
            arguments = "dockerRun"
        }

        //Waits for 5 minutes to send metrics to the controller
        exec {
            path = "make"
            arguments = "sleep"
        }

    }

    dependencies {
        dependency(Cassandra_Build) {
            snapshot {

            }

artifacts {
                artifactRules = """
                target/CassandraMonitor-*.zip => target
            """.trimIndent()
            }
        }
    }

    triggers {
        vcs {
        }
    }
})
