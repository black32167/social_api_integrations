package org.social.integrations.birdview

import org.social.integrations.birdview.api.BVTaskService
import org.social.integrations.birdview.model.BVTaskGroup
import org.springframework.context.annotation.AnnotationConfigApplicationContext

fun main(args:Array<String>) {
    println("Hello!");
    val ctx = AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {
        val taskGroups = it.getBean(BVTaskService::class.java).getTaskGroups("done")
        println("Tasks:")
        printTaskGroups(taskGroups)
    }
}

fun printTaskGroups(tasksGroup:List<BVTaskGroup>) {
    tasksGroup.forEach { group->
        println(">>> ${group.getTitle()}")
        group.tasks.forEach { task->
            println("    ${task.updated} - ${task.title} : ${task.httpUrl}" )
        }
    }
}