package de.solidblocks.rds.controller.instances

import de.solidblocks.rds.base.Utils
import de.solidblocks.rds.controller.api.CreationResult
import de.solidblocks.rds.controller.api.ValidationResult
import de.solidblocks.rds.controller.instances.api.RdsInstanceCreateRequest
import de.solidblocks.rds.controller.instances.api.RdsInstanceResponse
import de.solidblocks.rds.controller.model.Constants
import de.solidblocks.rds.controller.model.instances.RdsInstancesRepository
import de.solidblocks.rds.controller.utils.ErrorCodes
import mu.KotlinLogging
import java.util.*

class RdsInstancesManager(private val repository: RdsInstancesRepository) {

    private val logger = KotlinLogging.logger {}

    fun get(id: UUID) = repository.read(id)?.let {
        RdsInstanceResponse(it.id, it.name)
    }

    fun delete(id: UUID) = repository.delete(id)

    fun list() = repository.list().map {
        RdsInstanceResponse(it.id, it.name)
    }

    fun listInternal() = repository.list()

    fun validate(request: RdsInstanceCreateRequest): ValidationResult {

        if (repository.exists(request.name)) {
            return ValidationResult.error(RdsInstanceCreateRequest::name, ErrorCodes.DUPLICATE)
        }

        return ValidationResult(emptyList())
    }

    fun create(request: RdsInstanceCreateRequest): CreationResult<RdsInstanceResponse> {

        /*
        val serverCa = Utils.createCertificate()


        if (!repository.exists(DEFAULT_CONTROLLER_NAME)) {
            repository.create(DEFAULT_CONTROLLER_NAME, mapOf(Constants.CA_PUBLIC_KEY to serverCa.privateKey, Constants.CA_PRIVATE_KEY to serverCa.privateKey))
        }
        */

        val entity = repository.create(
            request.provider, request.name, mapOf()
        )

        return CreationResult(
            entity?.let {
                RdsInstanceResponse(it.id, it.name)
            }
        )
    }
}
