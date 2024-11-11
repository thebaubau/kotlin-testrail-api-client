package io.github.thebaubau.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import mu.KotlinLogging
import java.lang.IllegalStateException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class ApiClient(private val baseURL: String, private val token: String, private val projectId: Int) {
    private val log = KotlinLogging.logger {}
    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    constructor(baseURL: String, user: String, password: String, projectId: Int) : this(
        baseURL, Base64.getEncoder().encodeToString("$user:$password".toByteArray()), projectId
    )

    fun createPlan(planName: String, milestoneId: String, suiteId: Int): JsonObject {
        val req = testRailRequest()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                {
                    "name": "$planName",
                    "milestone_id": "$milestoneId",
                    "entries": [
                        {
                            "suite_id": $suiteId,
                            "name": "Custom run name"
                        }
                    ]
                }
                """.trimIndent()
                )
            )
            .uri(URI("$baseURL/add_plan/$projectId"))
            .build()

        return handlePostResponse("test plan", client.send(req, HttpResponse.BodyHandlers.ofString()))
    }

    fun createMilestone(name: String, parentId: Int? = null): JsonObject {
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

        return handlePostResponse("milestone", client.send(req, HttpResponse.BodyHandlers.ofString()))
    }


    fun createTestSuite(name: String, parentId: Int? = null): JsonObject {
        val req = testRailRequest()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                {
                    "name": "$name", 
                    "parent_id": $parentId,
                     "entries": [
                        {
                            "name": "Custom run name",
                            "suite_id": 4607
                        }
                    ]
                }
                """.trimIndent()
                )
            )
            .uri(URI("$baseURL/add_milestone/$projectId"))
            .build()

        return handlePostResponse("milestone", client.send(req, HttpResponse.BodyHandlers.ofString()))
    }

    fun getPlans(): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_plans/$projectId"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    fun getProjects(): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_projects"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    fun getSuite(suiteId: Int): JsonObject {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_suite/$suiteId"))
            .build()

        val response =  client.send(req, HttpResponse.BodyHandlers.ofString())
        return gson.fromJson(response.body(), JsonElement::class.java).asJsonObject
    }

    fun getMilestones(projectId: Int): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_milestones/$projectId"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    private fun handlePostResponse(resource: String, response: HttpResponse<String>): JsonObject {
        if (response.statusCode() !in 199..300) {
            throw IllegalStateException("Testrail request failed! status code=${response.statusCode()} body=${response.body()}")
        }
        val json = gson.fromJson(response.body(), JsonElement::class.java).asJsonObject
        log.info { "created a $resource with name='${json["name"].asString}' and id='${json["id"].asString}'" }
        return json
    }

    private fun testRailRequest(): HttpRequest.Builder = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic $token")



}
