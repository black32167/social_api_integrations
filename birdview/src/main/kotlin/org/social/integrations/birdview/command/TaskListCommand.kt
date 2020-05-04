package org.social.integrations.birdview.command

import org.social.integrations.birdview.api.BVTaskService
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTaskGroup
import org.social.integrations.birdview.utils.BVColorUtils
import picocli.CommandLine
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true,
        description = arrayOf("Lists tasks."))
class TaskListCommand(val taskService: BVTaskService) : Callable<Int> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }

    @CommandLine.Option(names = arrayOf("-s", "--status"), description = arrayOf("progress|todo|done"))
    var status = "progress"

    @CommandLine.Option(names = arrayOf("-n", "--noColors"), description = arrayOf("Disable ANSI colors"))
    var noColors = false

    @CommandLine.Option(names = arrayOf("--noGrouping"), description = arrayOf("Disable tasks grouping"))
    var noGrouping = false

    @CommandLine.Option(names = arrayOf("--noItems"), description = arrayOf("Hide items in groups"))
    var noItems = false

    override fun call(): Int {
        BVColorUtils.useColors = !noColors

        val taskGroups = taskService.getTaskGroups(status, !noGrouping)

        println("Listing work in '${BVColorUtils.bold(BVColorUtils.red(status))}' state.")
        println("Today is ${BVColorUtils.bold(dateFormat.format(Date()))}")
        println("")
        printTaskGroups(taskGroups)

        return 0
    }

    fun printTaskGroups(tasksGroup:List<BVTaskGroup>) {
        tasksGroup.forEach { group->
            if(noItems) {
                println(">>> ${group.getTitle()}")
            } else if (group.tasks.size > 1) {
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

    fun describe(task: BVTask)
            = "${dateFormat.format(task.updated)} - ${BVColorUtils.red(task.title)} : ${task.httpUrl}"}