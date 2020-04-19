package org.social.integrations.birdview

import org.social.integrations.birdview.api.BVTaskService
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTaskGroup
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.*

fun main(args:Array<String>) {
    val filter = args[0]
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {
        val taskGroups = it.getBean(BVTaskService::class.java).getTaskGroups(filter)
        println("Today:${Date()}")
        printTaskGroups(taskGroups)
    }
}

fun printTaskGroups(tasksGroup:List<BVTaskGroup>) {
    tasksGroup.forEach { group->
        if (group.tasks.size > 1) {
            println(">>> ${group.getTitle()}")
            group.tasks.forEach { task ->
                println("    ${describe(task)}")
            }
        } else {
            group.tasks.first().also {task->
                println(describe(task))
            }
        }
    }
}

fun describe(task:BVTask)
    = "${task.updated.substringBefore('T')} - ${task.title} : ${task.httpUrl}"