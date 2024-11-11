package api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class ApiClient(val baseURL: URL, val token: String) {
    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    constructor(baseURL: String, user: String, password: String) : this(
        URL(baseURL), Base64.getEncoder().encodeToString("$user:$password".toByteArray())
    )

    fun getProjects(): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_projects"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    fun getMilestones(projectId: Int): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_milestones/$projectId"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    fun createMilestone(projectId: Int, name: String, parentId: Int?): JsonObject {
        val req = testRailRequest()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                {
                    "name": "$name", 
                    "parent_id": $parentId
                }
                """.trimIndent()
                )
            )
            .uri(URI("$baseURL/add_milestone/$projectId"))
            .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())
        return gson.fromJson(response.body(), JsonElement::class.java).asJsonObject
    }

    private fun testRailRequest(): HttpRequest.Builder = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic $token")
}