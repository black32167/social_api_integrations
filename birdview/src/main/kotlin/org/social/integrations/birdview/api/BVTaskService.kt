package org.social.integrations.birdview.api

import org.social.integrations.birdview.source.jira.JiraTaskService
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import javax.inject.Named

@Named
class BVTaskService(private var jira:JiraTaskService) : TaskApiService {

    override fun getTask(p0: String?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTask(p0: Task?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTasks(filtersSpec:String?): Tasks {
        return jira.getTasks("Done");
    }
}