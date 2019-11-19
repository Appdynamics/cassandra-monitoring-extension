package AE_CassandraMonitoringExtension

import AE_CassandraMonitoringExtension.buildTypes.*
import AE_CassandraMonitoringExtension.vcsRoots.AE_CassandraMonitoringExtension
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Format.KOTLIN
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Mode.ENABLED
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings


object Project : Project({
    uuid = "4c653cc2-f8df-4945-9c03-0103ae70b834"
    id("AE_CassandraMonitoringExtension")
    parentId("AE")
    name = "AE_CassandraMonitoringExtension"

    vcsRoot(AE_CassandraMonitoringExtension)
    buildType(AE_CassandraMonitoringExtension_Build)
    buildType(AE_CassandraMonitoringExtension_IntegrationTests)
    buildType(AE_CassandraMonitoringExtension_WorkbenchTest)

    features {
        versionedSettings {

            mode = ENABLED
            buildSettingsMode = PREFER_SETTINGS_FROM_VCS
            rootExtId = "${AE_CassandraMonitoringExtension.id}"
            showChanges = true
            settingsFormat = KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(
            AE_CassandraMonitoringExtension_Build,
            AE_CassandraMonitoringExtension_IntegrationTests,
            AE_CassandraMonitoringExtension_WorkbenchTest
    )
})
