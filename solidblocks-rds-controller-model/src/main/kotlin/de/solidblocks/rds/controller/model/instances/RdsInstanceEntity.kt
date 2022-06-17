package de.solidblocks.rds.controller.model.instances

import de.solidblocks.rds.controller.model.CloudConfigValue
import java.util.UUID

data class RdsInstanceEntity(
    val id: UUID,
    val name: String,
    val provider: UUID,
    val configValues: List<CloudConfigValue>
)
