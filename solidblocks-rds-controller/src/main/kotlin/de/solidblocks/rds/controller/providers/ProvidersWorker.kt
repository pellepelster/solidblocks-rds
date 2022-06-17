package de.solidblocks.rds.controller.providers

import de.solidblocks.rds.controller.model.providers.ProviderEntity
import de.solidblocks.rds.controller.utils.Constants.sshKeyName
import mu.KotlinLogging

class ProvidersWorker(val providersManager: ProvidersManager) {

    private val logger = KotlinLogging.logger {}

    fun work(): Boolean {
        return providersManager.listInternal().map {
            work(it)
        }.all { it }
    }

    private fun work(provider: ProviderEntity): Boolean {
        logger.info { "starting work for provider '${provider.name} (${provider.id})'" }

        val hetznerApi = providersManager.createProviderInstance(provider.id)
        if (hetznerApi == null) {
            logger.info { "could not create provider instance for provider '${provider.name} (${provider.id})'" }
            return false
        }

        val response = hetznerApi.ensureSSHKey(sshKeyName(provider), provider.sshPublicKey())
        if (!response) {
            logger.error {
                "creating ssh key failed for provider '${provider.name}'"
            }

            return false
        }

        return true
    }
}