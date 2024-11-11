package io.github.thebaubau

import io.github.thebaubau.api.ApiClient

fun main() {
    val url = "https://[YOUR_URL]/index.php?/api/v2"

    val user = "[YOUR_USER]"
    val password = "[YOUR_PASSWORD]"

    val projectId = 147
    val client = ApiClient(url, user, password)

    client.getProjects().also { println(it) }
    client.getMilestones(projectId).also { println(it) }
    client.getPlans(projectId).also { println(it) }

}


