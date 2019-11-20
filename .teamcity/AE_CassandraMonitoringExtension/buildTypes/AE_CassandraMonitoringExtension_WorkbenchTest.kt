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

object AE_CassandraMonitoringExtension_WorkbenchTest : BuildType({
    uuid = "60ec417d-e73c-468c-9867-2ed53797787a"
    name = "Test Workbench mode"

    withDefaults()

    steps {
        exec {
            path = "make"
            arguments = "workbenchTest"
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