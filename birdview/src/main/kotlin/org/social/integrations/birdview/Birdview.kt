package org.social.integrations.birdview

import org.social.integrations.birdview.api.BVTaskService
import org.springframework.context.annotation.AnnotationConfigApplicationContext

fun main(args:Array<String>) {
    println("Hello!");
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {
        val tasks = it.getBean(BVTaskService::class.java).getTasks("done")
        println("Tasks = ${tasks}")
    }
}
