package de.solidblocks.rds.controller.model.instances

import de.solidblocks.rds.controller.model.BaseRepository
import de.solidblocks.rds.controller.model.CloudConfigValue
import de.solidblocks.rds.controller.model.LOCATION
import de.solidblocks.rds.controller.model.providers.ProviderStatus
import de.solidblocks.rds.controller.model.tables.references.CONFIGURATION_VALUES
import de.solidblocks.rds.controller.model.tables.references.RDS_INSTANCES
import org.jooq.Condition
import org.jooq.DSLContext
import java.util.*

class RdsInstancesRepository(dsl: DSLContext) : BaseRepository(dsl) {

    private val rdsInstances = RDS_INSTANCES.`as`("rds_instances")

    init {
        resetStatus()
    }

    fun create(
        providerId: UUID,
        name: String,
        configValues: Map<String, String> = emptyMap()
    ): RdsInstanceEntity {
        val id = UUID.randomUUID()

        dsl.insertInto(RDS_INSTANCES).columns(
            RDS_INSTANCES.ID,
            RDS_INSTANCES.NAME,
            RDS_INSTANCES.PROVIDER,
            RDS_INSTANCES.STATUS,
            RDS_INSTANCES.DELETED
        ).values(id, name, providerId, ProviderStatus.UNKNOWN.toString(), false).execute()

        configValues.forEach {
            setConfiguration(RdsInstanceId(id), it.key, it.value)
        }

        return read(id) ?: run { throw RuntimeException("could not read created rds instance") }
    }

    fun list(providerId: UUID) = list(rdsInstances.PROVIDER.eq(providerId))

    fun list(filter: Condition? = null): List<RdsInstanceEntity> {

        var filterConditions = rdsInstances.DELETED.isFalse
        if (filter != null) {
            filterConditions = filterConditions.and(filter)
        }

        val latest = latestConfigurationValuesQuery(CONFIGURATION_VALUES.RDS_INSTANCE)

        return dsl.selectFrom(
            rdsInstances.leftJoin(latest).on(rdsInstances.ID.eq(latest.field(CONFIGURATION_VALUES.RDS_INSTANCE)))
        ).where(filterConditions).fetchGroups({ it.into(rdsInstances) }, { it.into(latest) }).map {
            RdsInstanceEntity(
                id = it.key.id!!,
                name = it.key.name!!,
                provider = it.key.provider!!,
                status = RdsInstanceStatus.valueOf(it.key.status!!),
                configValues = it.value.map {
                    if (it.getValue(CONFIGURATION_VALUES.KEY_) == null) {
                        null
                    } else {
                        CloudConfigValue(
                            it.getValue(CONFIGURATION_VALUES.KEY_)!!,
                            it.getValue(CONFIGURATION_VALUES.VALUE_),
                            it.getValue(CONFIGURATION_VALUES.VERSION)!!
                        )
                    }
                }.filterNotNull(),
            )
        }
    }

    fun delete(id: UUID): Boolean {
        dsl.delete(CONFIGURATION_VALUES).where(CONFIGURATION_VALUES.RDS_INSTANCE.eq(id)).execute()
        return dsl.delete(RDS_INSTANCES).where(RDS_INSTANCES.ID.eq(id)).execute() == 1
    }

    fun exists(name: String): Boolean {
        return dsl.selectFrom(RDS_INSTANCES).where(RDS_INSTANCES.NAME.eq(name)).count() == 1
    }

    fun update(id: UUID, values: Map<String, String>): Boolean {
        return values.map {
            update(id, it.key, it.value)
        }.all { it }
    }

    fun read(id: UUID): RdsInstanceEntity? {
        return list(rdsInstances.ID.eq(id)).firstOrNull()
    }

    fun read(name: String): RdsInstanceEntity? {
        return list(rdsInstances.NAME.eq(name)).firstOrNull()
    }

    fun update(
        name: String,
        key: String,
        value: String?
    ): Boolean {
        val instance = read(name) ?: return false
        return setConfiguration(RdsInstanceId(instance.id), key, value)
    }

    private fun update(
        id: UUID,
        key: String,
        value: String
    ) = setConfiguration(RdsInstanceId(id), key, value)

    fun updateStatus(id: UUID, status: ProviderStatus) =
        dsl.update(rdsInstances).set(rdsInstances.STATUS, status.toString()).where(rdsInstances.ID.eq(id))
            .execute() == 1

    fun resetStatus() {
        dsl.update(rdsInstances).set(rdsInstances.STATUS, ProviderStatus.UNKNOWN.toString()).execute()
    }
}
