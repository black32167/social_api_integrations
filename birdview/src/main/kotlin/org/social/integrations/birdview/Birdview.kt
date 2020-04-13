package org.social.integrations.birdview

import org.social.integrations.birdview.api.BVTaskService
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import social.api.task.model.Task

fun main(args:Array<String>) {
    println("Hello!");
    val ctx = AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {
        val tasks = it.getBean(BVTaskService::class.java).getTasks("done").tasks as List<Task>
        println("Tasks = ${tasks.map { "${it.updated} - ${it.title} : ${it.httpUrl}" }.joinToString("\n")}")
    }
}
