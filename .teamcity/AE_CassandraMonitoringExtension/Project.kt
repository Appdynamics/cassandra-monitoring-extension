package AE_CassandraMonitoringExtension

import AE_CassandraMonitoringExtension.vcsRoots.*
import AE_CassandraMonitoringExtension.buildTypes.*
import AE_CassandraMonitoringExtension.vcsRoots.cassandramonitoringextension
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "9e8a6564-3357-4478-899c-0e0aa4e04f24"
    id("AE_CassandraMonitoringExtension")
    parentId("AE")
    name = "Cassandra Monitoring Extension"

    vcsRoot(cassandramonitoringextension)
    buildType(Cassandra_Setup)
    buildType(Cassandra_Stop)
    buildType(Cassandra_IntegrationTests)
    buildType(Cassandra_Build)

    buildTypesOrder = arrayListOf(Cassandra_Build,
            Cassandra_Setup,
            Cassandra_IntegrationTests,
            Cassandra_Stop)

    features {
        versionedSettings {
            id = "PROJECT_EXT_2"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${cassandramonitoringextension.id}"
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }
})
