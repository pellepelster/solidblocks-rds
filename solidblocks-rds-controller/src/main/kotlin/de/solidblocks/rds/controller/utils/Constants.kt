package de.solidblocks.rds.controller.utils

import de.solidblocks.rds.controller.model.providers.ProviderEntity
import de.solidblocks.rds.controller.model.instances.RdsInstanceEntity

object Constants {

    public enum class LOCATION { fsn1 }

    fun data1VolumeName(rdsInstance: RdsInstanceEntity, location: LOCATION) =
        "${rdsInstance.name}-${location}-data1-${rdsInstance.id}".lowercase()

    fun serverName(rdsInstance: RdsInstanceEntity, location: LOCATION) =
        "${rdsInstance.name}-${location}-${rdsInstance.id}".lowercase()

    fun sshKeyName(provider: ProviderEntity) = "${provider.name}-${provider.id}".lowercase()

    val labelNamespace: String = "solidblocks.de"

    val managedByLabel: String = "${labelNamespace}/managed"

    val versionLabel: String = "${labelNamespace}/version"

    val cloudInitChecksumLabel: String = "${labelNamespace}/cloudInitChecksum"
}
