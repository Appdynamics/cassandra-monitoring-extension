package AE_CassandraMonitoringExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object cassandramonitoringextension : GitVcsRoot({
    uuid = "de423cab-7e1b-4ba5-8727-b321715da94d"
    name = "cassandramonitoringextension"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/cassandra-monitoring-extension.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/cassandra-monitoring-extension.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    branchSpec = """
        +:refs/heads/(2.1)
        +:refs/(pull-requests/*)/from
    """.trimIndent()
})
