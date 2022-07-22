package de.solidblocks.rds.controller

import com.github.kagkarlsson.scheduler.Scheduler
import com.github.kagkarlsson.scheduler.task.AbstractTask
import com.github.kagkarlsson.scheduler.task.Task
import com.github.kagkarlsson.scheduler.task.TaskInstance
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import de.solidblocks.rds.base.Database
import java.time.Duration
import java.time.Instant


class RdsScheduler(private val database: Database) {

    private val tasks = ArrayList<Task<out Any>>()
    private val recurringTasks = ArrayList<RecurringTask<out Any>>()

    private var scheduler: Scheduler? = null

    fun start() {
        scheduler = Scheduler
            .create(database.datasource, tasks)
            .startTasks(recurringTasks)
            .deleteUnresolvedAfter(Duration.ofSeconds(60))
            .threads(5).build()
        scheduler!!.start()
    }

    fun scheduleTask(task: TaskInstance<out Any>) {

        if (scheduler == null) {
            throw RuntimeException("scheduler is no started")
        }

        scheduler!!.schedule(task, Instant.now().plusSeconds(5))
    }

    fun addTask(task: AbstractTask<out Any>) {

        if (scheduler != null && scheduler!!.schedulerState.isStarted) {
            throw RuntimeException("could not add new task definition, scheduler is already running")
        }

        tasks.add(task)
    }

    fun addRecurringTasks(task: RecurringTask<out Any>) {

        if (scheduler != null && scheduler!!.schedulerState.isStarted) {
            throw RuntimeException("could not add new task definition, scheduler is already running")
        }

        recurringTasks.add(task)
    }

}