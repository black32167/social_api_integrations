package org.social.integrations.birdview

import org.social.integrations.birdview.api.BVTaskService
import org.social.integrations.birdview.command.TaskListCommand
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import picocli.CommandLine

fun main(vararg args:String) {
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {ctx->
        val taskService =  ctx.getBean(BVTaskService::class.java)
        CommandLine(TaskListCommand(taskService)).execute(*args)
    }
}
