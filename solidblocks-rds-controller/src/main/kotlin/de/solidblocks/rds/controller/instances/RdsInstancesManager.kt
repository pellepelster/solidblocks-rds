package de.solidblocks.rds.controller.instances

import de.solidblocks.rds.base.Utils
import de.solidblocks.rds.controller.api.CreationResult
import de.solidblocks.rds.controller.api.ValidationResult
import de.solidblocks.rds.controller.controllers.ControllersManager
import de.solidblocks.rds.controller.instances.api.RdsInstanceCreateRequest
import de.solidblocks.rds.controller.instances.api.RdsInstanceResponse
import de.solidblocks.rds.controller.model.Constants.SERVER_PRIVATE_KEY
import de.solidblocks.rds.controller.model.Constants.SERVER_PUBLIC_KEY
import de.solidblocks.rds.controller.model.instances.RdsInstancesRepository
import de.solidblocks.rds.controller.providers.ProvidersManager
import de.solidblocks.rds.controller.utils.ErrorCodes
import mu.KotlinLogging
import java.util.*

class RdsInstancesManager(
    private val repository: RdsInstancesRepository,
    private val providersManager: ProvidersManager,
    private val controllersManager: ControllersManager
) {

    private val logger = KotlinLogging.logger {}

    fun read(id: UUID) = repository.read(id)?.let {
        RdsInstanceResponse(it.id, it.name, it.provider)
    }

    fun delete(id: UUID) = repository.delete(id)

    fun list() = repository.list().map {
        RdsInstanceResponse(it.id, it.name, it.provider)
    }

    fun listInternal() = repository.list()

    fun validate(request: RdsInstanceCreateRequest): ValidationResult {

        if (repository.exists(request.name)) {
            return ValidationResult.error(RdsInstanceCreateRequest::name, ErrorCodes.DUPLICATE)
        }

        return ValidationResult(emptyList())
    }

    fun create(request: RdsInstanceCreateRequest): CreationResult<RdsInstanceResponse> {

        val provider = providersManager.read(request.provider)
            ?: return CreationResult.error("provider '${request.provider}' not found")

        val controller = controllersManager.readInternal(provider.controller)
            ?: return CreationResult.error("controller '${provider.controller}' not found")

        val serverKeyPair = Utils.createCertificate(controller.caServerPrivateKey(), controller.caServerPublicKey())

        val entity = repository.create(
            request.provider, request.name,
            mapOf(
                SERVER_PRIVATE_KEY to serverKeyPair.privateKey,
                SERVER_PUBLIC_KEY to serverKeyPair.publicKey,
            )
        )

        return CreationResult(
            entity.let {
                RdsInstanceResponse(it.id, it.name, it.provider)
            }
        )
    }
}
