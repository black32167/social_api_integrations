package org.social.integrations.birdview.api

import org.social.integrations.birdview.source.jira.JiraTaskService
import org.social.integrations.birdview.source.trello.TrelloTaskService
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import javax.inject.Named

@Named
class BVTaskService(
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService
) : TaskApiService {

    override fun getTask(p0: String?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTask(p0: Task?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTasks(filtersSpec:String?): Tasks {
        val tasks = mutableListOf<Task>()
        val start = System.currentTimeMillis()
        tasks.addAll(jira.getTasks("Done").tasks as List<Task>)
        tasks.addAll(trello.getTasks("Planned").tasks as List<Task>)
        val end = System.currentTimeMillis();
        println("Request took ${end-start} ms.")
        return Tasks().apply { this.tasks = tasks  }
    }
}