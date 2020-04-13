package org.social.integrations.birdview.api

import org.social.integrations.birdview.source.jira.JiraTaskService
import org.social.integrations.birdview.source.trello.TrelloTaskService
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class BVTaskService(
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService
) : TaskApiService {
    private val executor = Executors.newFixedThreadPool(3)

    override fun getTask(p0: String?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTask(p0: Task?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTasks(filtersSpec:String?): Tasks {
        val tasks = mutableListOf<Task>()
        val start = System.currentTimeMillis()

        val futures = listOf<Future<Tasks>>(
                executor.submit (object: Callable<Tasks> {
                    override fun call() = jira.getTasks("Done")
                }),
                executor.submit (object: Callable<Tasks> {
                    override fun call() = trello.getTasks("Planned")
                })
        )
        for (future in futures) {
            tasks.addAll(future.get().tasks as List<Task>)
        }

        val end = System.currentTimeMillis();
        println("Request took ${end-start} ms.")
        return Tasks().apply { this.tasks = tasks  }
    }
}