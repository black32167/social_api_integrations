package org.social.integrations.birdview.api

import org.social.integrations.birdview.source.github.GithubTaskService
import org.social.integrations.birdview.source.jira.JiraTaskService
import org.social.integrations.birdview.source.trello.TrelloTaskService
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
class BVTaskService(
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService,
        private val github: GithubTaskService
) : TaskApiService {
    private val executor = Executors.newFixedThreadPool(3)

    override fun getTask(p0: String?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTask(p0: Task?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTasks(status: String): Tasks {
        val tasks = mutableListOf<Task>()
        val start = System.currentTimeMillis()
        val sources = listOf<TaskApiService>(jira, trello, github)

        val futures = sources.map { source ->
            executor.submit(object : Callable<Tasks> {
                override fun call() = source.getTasks(status)
            })
        }

        for (future in futures) {
            tasks.addAll(future.get().tasks as List<Task>)
        }

        val end = System.currentTimeMillis();
        println("Request took ${end-start} ms.")

        tasks.sortByDescending { it.updated }

        return Tasks().apply { this.tasks = tasks  }
    }
}