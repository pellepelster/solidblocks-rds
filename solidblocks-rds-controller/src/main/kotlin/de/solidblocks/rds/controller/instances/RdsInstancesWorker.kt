package de.solidblocks.rds.controller.instances

import de.solidblocks.rds.controller.controllers.ControllersManager
import de.solidblocks.rds.controller.model.providers.ProviderEntity
import de.solidblocks.rds.controller.model.instances.RdsInstanceEntity
import de.solidblocks.rds.controller.providers.HetznerApi
import de.solidblocks.rds.controller.providers.ProvidersManager
import de.solidblocks.rds.controller.utils.Constants.LOCATION.*
import de.solidblocks.rds.controller.utils.Constants.data1VolumeName
import de.solidblocks.rds.controller.utils.Constants.serverName
import mu.KotlinLogging

class RdsInstancesWorker(val rdsInstancesManager: RdsInstancesManager, val providersManager: ProvidersManager, val controllersManager: ControllersManager) {

    private val logger = KotlinLogging.logger {}

    /*
    private var provisionerApplyTask = Tasks.recurring("provisioner-apply-task", FixedDelay.ofSeconds(30))
        .execute { _: TaskInstance<Void>, _: ExecutionContext ->

            executor.executeWithLock<Any>(
                {
                    providersManager.apply()
                },
                LockConfiguration(
                    Instant.now(), "global-apply-task", Duration.ofSeconds(60), Duration.ofSeconds(5)
                )
            )
        }
    */


    fun apply(hetznerApi: HetznerApi, provider: ProviderEntity): Boolean {

        /*
        val instances = repository.list(provider.id)

        if (!hetznerApi.cleanupServersNotInList(instances)) {
            logger.error {
                "cleaning up deleted servers failed"
            }

            return false
        }

        return instances.map {
            logger.info {
                "applying config for instance '${it.name}'"
            }

            hetznerApi.ensureVolume(Constants.data1VolumeName(it))
            hetznerApi.ensureServer(
                Constants.serverName(it),
                Constants.data1VolumeName(it), "",
                Constants.sshKeyName(provider)
            )

            true
        }.all { it }
         */
        return true
    }

    fun work(): Boolean {
        return rdsInstancesManager.listInternal().map {
            work(it)
        }.any { it }
    }

    private fun work(rdsInstance: RdsInstanceEntity): Boolean {
        logger.info { "starting work for rds instance '${rdsInstance.name} (${rdsInstance.id})'" }

        val hetznerApi = providersManager.createProviderInstance(rdsInstance.provider) ?: run {
            logger.info { "could not create provider instance for rds instance '${rdsInstance.id}'" }
            return false
        }

        val location = fsn1

        val volumeName = data1VolumeName(rdsInstance, location)
        val serverName = serverName(rdsInstance, location)
        val sshKeyName = providersManager.sshKeyName(rdsInstance.provider) ?: run {
            logger.info { "could not find ssh key name for provider '${rdsInstance.provider}'" }
            return false
        }

        val volumeResult = hetznerApi.ensureVolume(data1VolumeName(rdsInstance, location))
        if (!volumeResult) {
            logger.info {
                "could not create volume '${volumeName}' for instance '${rdsInstance.name}'"
            }
            return false
        }

        val serverInfo = hetznerApi.ensureServer(serverName, volumeName, "", sshKeyName) ?: run {
            logger.info {
                "could not server '${serverName}' for rds instance '${rdsInstance.name}'"
            }

            return false
        }

        val controller = controllersManager.defaultController()

        return true
    }

}