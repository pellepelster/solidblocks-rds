package de.solidblocks.rds.controller

import de.solidblocks.rds.base.Database
import de.solidblocks.rds.controller.api.ApiHttpServer
import de.solidblocks.rds.controller.controllers.ControllersManager
import de.solidblocks.rds.controller.instances.RdsInstancesManager
import de.solidblocks.rds.controller.instances.api.RdsInstancesApi
import de.solidblocks.rds.controller.model.controllers.ControllersRepository
import de.solidblocks.rds.controller.model.instances.RdsInstancesRepository
import de.solidblocks.rds.controller.model.providers.ProvidersRepository
import de.solidblocks.rds.controller.providers.ProvidersManager
import de.solidblocks.rds.controller.providers.api.ProvidersApi
import mu.KotlinLogging
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider

class Controller(database: Database) {

    private val logger = KotlinLogging.logger {}

    private val rdsScheduler = RdsScheduler(database)

    private val controllersManager = ControllersManager(ControllersRepository(database.dsl))

    private val providersManager = ProvidersManager(ProvidersRepository(database.dsl), controllersManager, rdsScheduler)

    private val instancesManager =
        RdsInstancesManager(RdsInstancesRepository(database.dsl), providersManager, controllersManager, rdsScheduler)

    private val executor = DefaultLockingTaskExecutor(JdbcLockProvider(database.datasource))


    init {
        val httpServer = ApiHttpServer(port = 8080)

        val providersApi = ProvidersApi(httpServer, providersManager)
        val instancesApi = RdsInstancesApi(httpServer, instancesManager)

        rdsScheduler.start()
    }

}
