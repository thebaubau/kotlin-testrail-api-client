package io.github.thebaubau

import api.ApiClient

fun main() {
    val url = "https://betvictor.testrail.net/index.php?/api/v2"


    val user = "daniel.fintinariu@betvictor.com"
    val password = "1faktEiPlSLpr0gXhGJ/-tIzB8gm6G7UZFeJ1V1G5"

    val projectId = 147
    val client = ApiClient(url, user, password)

    client.getProjects().also { println(it) }
    client.getMilestones(projectId).also { println(it) }

    client.createMilestone(projectId, "Test Milestone", null).also { it ->
        val id = it["id"].asInt
        client.createMilestone(projectId, "Test MilestoneCHILD", id).also {
            val id2 = it["id"].asInt
            client.createMilestone(projectId, "Test Milestone GRAND CHILD", id2)




        }
    }
}


