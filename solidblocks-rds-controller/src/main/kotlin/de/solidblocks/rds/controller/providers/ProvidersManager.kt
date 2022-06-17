package de.solidblocks.rds.controller.providers

import de.solidblocks.rds.base.Utils
import de.solidblocks.rds.controller.controllers.ControllersManager
import de.solidblocks.rds.controller.api.CreationResult
import de.solidblocks.rds.controller.api.ValidationResult
import de.solidblocks.rds.controller.model.Constants.API_KEY
import de.solidblocks.rds.controller.model.Constants.SSH_PRIVATE_KEY
import de.solidblocks.rds.controller.model.Constants.SSH_PUBLIC_KEY
import de.solidblocks.rds.controller.model.providers.ProvidersRepository
import de.solidblocks.rds.controller.providers.api.ProviderCreateRequest
import de.solidblocks.rds.controller.providers.api.ProviderResponse
import de.solidblocks.rds.controller.utils.Constants
import de.solidblocks.rds.controller.utils.ErrorCodes
import me.tomsdevsn.hetznercloud.HetznerCloudAPI
import mu.KotlinLogging
import java.util.UUID

class ProvidersManager(
    private val repository: ProvidersRepository,
    private val controllersManager: ControllersManager
) {

    private val logger = KotlinLogging.logger {}


    fun get(id: UUID) = repository.read(id)?.let {
        ProviderResponse(it.id, it.name)
    }

    fun delete(id: UUID) = repository.delete(id)

    fun list() = repository.list().map {
        ProviderResponse(it.id, it.name)
    }

    fun listInternal() = repository.list()

    fun validate(request: ProviderCreateRequest): ValidationResult {

        if (repository.exists(request.name)) {
            return ValidationResult.error(ProviderCreateRequest::name, ErrorCodes.DUPLICATE)
        }

        try {
            val cloudApi = HetznerCloudAPI(request.apiKey)
            cloudApi.datacenters.datacenters
        } catch (e: Exception) {
            return ValidationResult.error(ProviderCreateRequest::apiKey, ErrorCodes.INVALID)
        }

        return ValidationResult(emptyList())
    }

    fun create(request: ProviderCreateRequest): CreationResult<ProviderResponse> {
        val sshKey = Utils.generateSshKey(request.name)

        val entity = repository.create(
            request.name,
            controllersManager.defaultController(),
            mapOf(
                API_KEY to request.apiKey,
                SSH_PUBLIC_KEY to sshKey.publicKey,
                SSH_PRIVATE_KEY to sshKey.privateKey
            )
        )

        return CreationResult(
            entity?.let {
                ProviderResponse(it.id, it.name)
            }
        )
    }

    fun createProviderInstance(id: UUID) = repository.read(id)?.let { HetznerApi(it.apiKey()) }

    fun sshKeyName(id: UUID) = repository.read(id)?.let { Constants.sshKeyName(it) }
}
