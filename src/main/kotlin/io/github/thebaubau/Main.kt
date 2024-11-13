package io.github.thebaubau

import com.google.gson.Gson
import io.github.thebaubau.api.ApiClient
import io.github.thebaubau.api.id
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


fun main() {
    val bvUrl = System.getenv("testrail_url")
    val user = System.getenv("testrail_user")
    val password = System.getenv("testrail_pass")

    val configJson = Files.readString(Paths.get("src/main/resources/config.json"))
    val config = Gson().fromJson(configJson, ConfigProperties::class.java)

    val url = "https://$bvUrl/index.php?/api/v2"
    val client = ApiClient(url, user, password, config.projectId)

    client.createMilestone(config.buildVersion).also { parent ->

        config.brands.forEach { brand ->
            client.createMilestone(brand, parent.id()).also { milestone ->
                client.createPlan(config.buildNo, milestone.id(), config.suites)
            }
        }
    }
}

data class NativeSuite(val name: String, val id: Long)

data class ConfigProperties(
    val projectId: Long,
    val suites: List<NativeSuite>,
    val buildVersion: String,
    val brands: List<String>,
    val buildNo: String
)




