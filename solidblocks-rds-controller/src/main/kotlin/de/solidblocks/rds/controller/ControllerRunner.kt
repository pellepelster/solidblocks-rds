package de.solidblocks.rds.controller

import de.solidblocks.rds.base.Database
import de.solidblocks.rds.controller.model.controllers.ControllerEntity
import mu.KotlinLogging

class ControllerRunner(database: Database) {

    private val logger = KotlinLogging.logger {}

    /*
    private val rdsInstancesManager = RdsInstancesManager(RdsInstancesRepository(database.dsl))
    private val providersManager = ProvidersManager(ProvidersRepository(database.dsl))
    private val executor = DefaultLockingTaskExecutor(JdbcLockProvider(database.datasource))
    */

    init {
        //val httpServer = ApiHttpServer(port = 8080)
    }

    fun getController(): ControllerEntity {
        TODO("implement")
    }
}
