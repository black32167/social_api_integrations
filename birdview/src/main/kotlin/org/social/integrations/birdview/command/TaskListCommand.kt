package org.social.integrations.birdview.command

import org.social.integrations.birdview.GroupDescriber
import org.social.integrations.birdview.api.BVTaskService
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTaskGroup
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.utils.BVColorUtils
import org.social.integrations.birdview.utils.BVColorUtils.bold
import picocli.CommandLine
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true,
        description = ["Lists tasks."])
class TaskListCommand(val taskService: BVTaskService, val groupDescriber: GroupDescriber) : Callable<Int> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }

    @CommandLine.Option(names = ["-s", "--status"], description = ["progress|todo|done"])
    var status = "progress"

    @CommandLine.Option(names = ["-n", "--noColors"], description = ["Disable ANSI colors"])
    var noColors = false

    @CommandLine.Option(names = ["--noGrouping"], description = ["Disable tasks grouping"])
    var noGrouping = false

    @CommandLine.Option(names = ["--groupingThreshold"], description = ["Grouping threshold"])
    var groupingThreshold = 0.05

    @CommandLine.Option(names = ["--noItems"], description = ["Hide items in groups"])
    var noItems = false

    @CommandLine.Option(names = ["-u", "--user"], description = ["Specific user"])
    var user:String? = null

    @CommandLine.Option(names = ["--daysBack"], description = ["Days back"])
    var daysBack:Long = 2

    override fun call(): Int {
        BVColorUtils.useColors = !noColors

        val sinceDateTime = ZonedDateTime.now().minusDays(daysBack)

        val taskGroups = taskService.getTaskGroups(
                TasksRequest(
                status = status,
                grouping = !noGrouping,
                groupingThreshold = groupingThreshold,
                since = sinceDateTime,
                user = user
                ))

        println("Listing work in '${bold(BVColorUtils.red(status))}' state.")
        val now = LocalDate.now()
        println("Activity for the last ${bold(daysBack.toString())} days (" +
                "from ${bold(sinceDateTime.minusDays(daysBack).format(DateTimeFormatter.ISO_LOCAL_DATE))} " +
                "to ${bold(now.format(DateTimeFormatter.ISO_LOCAL_DATE))})")
        println("")
        printTaskGroups(taskGroups)

        return 0
    }

    fun printTaskGroups(tasksGroup:List<BVTaskGroup>) {
        tasksGroup.forEach { group->
            if(noItems) {
                println("[${group.title}]")
            } else if (group.tasks.size > 1) {
                println("[${group.title}]")
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