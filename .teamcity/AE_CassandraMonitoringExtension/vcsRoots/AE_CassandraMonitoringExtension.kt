package AE_CassandraMonitoringExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object AE_CassandraMonitoringExtension : GitVcsRoot({
    uuid = "000d1b5f-d547-451f-a5e0-84ee9bb647d5"
    id("AE_CassandraMonitoringExtension")
    name = "AE_CassandraMonitoringExtension"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/cassandra-monitoring-extension.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/cassandra-monitoring-extension.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    agentCleanPolicy = AgentCleanPolicy.ALWAYS
    branchSpec = """
    +:refs/heads/(master)
    +:refs/(pull-requests/*)/from
    """.trimIndent()
})