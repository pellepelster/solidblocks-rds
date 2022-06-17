package de.solidblocks.rds.controller

import com.github.kagkarlsson.scheduler.Scheduler
import de.solidblocks.rds.base.Database
import java.time.Duration

class SolidblocksScheduler(database: Database) {

    private val scheduler = Scheduler.create(database.datasource)
        .deleteUnresolvedAfter(Duration.ofSeconds(60)).threads(5).build()

    fun start() {
        scheduler.start()
    }
}