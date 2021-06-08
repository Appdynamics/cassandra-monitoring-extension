package AE_CassandraMonitoringExtension.buildTypes

import AE_CassandraMonitoringExtension.publishCommitStatus
import AE_CassandraMonitoringExtension.vcsRoots.AE_CassandraMonitoringExtension
import AE_CassandraMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_CassandraMonitoringExtension_IntegrationTests : BuildType({
    uuid = "ee552a23-7649-4d93-915c-754b714d06dd"
    name = "Run Integration Tests"

    withDefaults()

    steps {
        exec {
            path = "make"
            arguments = "dockerRun sleep"
        }
        maven {
            goals = "clean verify -DskipITs=false"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
        exec {
            path = "make"
            arguments = "dockerStop"
        }
        exec {
            executionMode = BuildStep.ExecutionMode.ALWAYS
            path = "make"
            arguments = "dockerClean"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        dependency(AE_CassandraMonitoringExtension_Build) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            artifacts {
                artifactRules = """
                +:target/CassandraMonitor-*.zip => target/
            """.trimIndent()
            }
        }
    }

    publishCommitStatus()
})