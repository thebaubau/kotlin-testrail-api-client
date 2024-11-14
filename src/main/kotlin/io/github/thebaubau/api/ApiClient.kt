package io.github.thebaubau.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.thebaubau.NativeSuite
import mu.KotlinLogging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ApiClient(private val baseURL: String, private val token: String, private val projectId: Long) {
    private val log = KotlinLogging.logger {}
    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    constructor(baseURL: String, user: String, password: String, projectId: Long) : this(
        baseURL, Base64.getEncoder().encodeToString("$user:$password".toByteArray()), projectId
    )

    fun createPlan(planName: String, milestoneId: Long, suites: List<NativeSuite>): JsonObject {
        val entriesJson = suites.map {
            """
            {
                "suite_id": ${it.id},
                "name": "${it.name}"
            }
        """
        }.joinToString(separator = ",", prefix = "[", postfix = "]")

        val req = testRailRequest()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                {
                    "name": "$planName",
                    "milestone_id": "$milestoneId",
                    "entries": $entriesJson
                }
                """.trimIndent()
                )
            )
            .uri(URI("$baseURL/add_plan/$projectId"))
            .build()

        return handlePostResponse(
            "test plan with Suites ${suites.map {it.name}}",
            client.send(req, HttpResponse.BodyHandlers.ofString()))
    }

    fun createMilestone(name: String, parentId: Long? = null, startDate: Long? = null, dueDate: Long? = null): JsonObject {
        val req = testRailRequest()
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                {
                    "name": "$name", 
                    "parent_id": $parentId,
                    "start_on": $startDate,
                    "due_on":  $dueDate
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

    fun getSuite(suiteId: Long): JsonObject {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_suite/$suiteId"))
            .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())
        return gson.fromJson(response.body(), JsonElement::class.java).asJsonObject
    }

    fun getMilestones(projectId: Long): HttpResponse<String> {
        val req = testRailRequest()
            .GET()
            .uri(URI("$baseURL/get_milestones/$projectId"))
            .build()

        return client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    private fun handlePostResponse(resource: String, response: HttpResponse<String>): JsonObject {
        require(response.is2xxSuccessful()) { "Testrail request failed! status code=${response.statusCode()} body=${response.body()}" }

        return gson.fromJson(response.body(), JsonElement::class.java)
            .asJsonObject
            .also {
                log.info { "created a $resource with name='${it.name()}' and id='${it.id()}'" }
            }
    }

    private fun <T> HttpResponse<T>.is2xxSuccessful(): Boolean {
        return this.statusCode() in 200..<300
    }

    private fun testRailRequest(): HttpRequest.Builder = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic $token")
}

fun JsonObject.id(): Long {
    return this["id"].asLong
}

fun JsonObject.name(): String {
    return this["name"].asString
}

