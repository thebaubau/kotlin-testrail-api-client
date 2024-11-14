package io.github.thebaubau

import com.google.gson.Gson
import io.github.thebaubau.api.ApiClient
import io.github.thebaubau.api.id
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun main() {
    val bvUrl = System.getenv("testrail_url")
    val user = System.getenv("testrail_user")
    val password = System.getenv("testrail_pass")

    val configJson = Files.readString(Paths.get("src/main/resources/config.json"))
    val config = Gson().fromJson(configJson, ConfigProperties::class.java)

    val url = "https://$bvUrl/index.php?/api/v2"
    val client = ApiClient(url, user, password, config.projectId)

    client.createMilestone(
        name = config.buildVersion,
        startDate = timestamp(config.startDate),
        dueDate = timestamp(config.dueDate)
    )
        .also { parent ->
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
    val buildNo: String,
    val startDate: String,
    val dueDate: String
)

fun timestamp(date: String?): Long {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val localDate = LocalDate.parse(date, formatter)

    return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}




