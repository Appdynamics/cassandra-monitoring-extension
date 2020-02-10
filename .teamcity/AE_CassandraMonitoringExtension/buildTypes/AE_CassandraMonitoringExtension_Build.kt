package AE_CassandraMonitoringExtension.buildTypes

import AE_CassandraMonitoringExtension.publishCommitStatus
import AE_CassandraMonitoringExtension.vcsRoots.AE_CassandraMonitoringExtension
import AE_CassandraMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_CassandraMonitoringExtension_Build : BuildType({
    uuid = "fa86e3c0-466a-44a0-959a-768719e9b9e3"
    name = "Cassandra Monitoring Extension Build"

    withDefaults()

    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
            userSettingsSelection = "teamcity-settings"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = """
    +:target/CassandraMonitor-*.zip => target/
""".trimIndent()

    publishCommitStatus()
})