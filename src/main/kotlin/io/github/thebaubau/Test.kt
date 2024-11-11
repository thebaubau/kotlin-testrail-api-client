package io.github.thebaubau

import io.github.thebaubau.api.ApiClient

fun test() {
    val client = ApiClient("https://[YOUR_URL]/index.php?/api/v2", "[YOUR_USER]", "[YOUR_PASSWORD]")
    client.gson

}