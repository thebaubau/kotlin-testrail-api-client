package io.github.thebaubau

import io.github.thebaubau.api.ApiClient

fun main() {
    val bvUrl = System.getenv("testrail_url")
    val user = System.getenv("testrail_user")
    val password = System.getenv("testrail_pass")

    val projectId = 147
    val url = "https://$bvUrl/index.php?/api/v2"
    val client = ApiClient(url, user, password, projectId)

    val buildNo = "17313308"

    client.createMilestone("6.44.0").also { parent ->
        val parentId = parent["id"].asInt

        client.createMilestone( "BV-UK", parentId).also { milestone ->
            val milestoneId = milestone["id"].asString

            val suiteId = 4607
            client.getSuite(4607)

            client.createPlan(buildNo, milestoneId, suiteId).also { plan ->
                val planId = milestone["id"].asString

            }
        }
    }
}


