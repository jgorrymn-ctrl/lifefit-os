package com.lifefit.os


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HevyRepository {

    private val client = OkHttpClient()

    private val baseUrl = "https://api.hevyapp.com"

    suspend fun getWorkouts(
        apiKey: String,
        page: Int = 1
    ): JSONObject = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            .url("$baseUrl/v1/workouts?page=$page&pageSize=10")
            .header("api-key", apiKey)
            .get()
            .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw Exception(
                    "Hevy request failed: HTTP ${response.code}"
                )
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Hevy returned an empty response")

            JSONObject(responseBody)
        }
    }

    suspend fun getRoutines(
        apiKey: String,
        page: Int = 1
    ): JSONObject = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            .url("$baseUrl/v1/routines?page=$page&pageSize=10")
            .header("api-key", apiKey)
            .get()
            .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw Exception(
                    "Hevy request failed: HTTP ${response.code}"
                )
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Hevy returned an empty response")

            JSONObject(responseBody)
        }
    }
}