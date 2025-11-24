package org.app.corge.data.repository

import com.russhwolf.settings.Settings
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.app.corge.network.HttpClientProvider
import org.app.corge.network.InstallResponse

interface RemoteConfigRepository {
    suspend fun fetchUrl(): String?
    suspend fun saveUrl(url: String)
    suspend fun getSavedUrl(): String?
}
class RemoteConfigRepositoryImpl(
    private val settings: Settings
) : RemoteConfigRepository {

    private val bundle = "6754844692"
    private val apiUrl = "https://cloudspell.top/v1/public/install"

    override suspend fun fetchUrl(): String? {
        println("🔵 [RemoteConfig] Sending POST → $apiUrl")
        println("🔵 [RemoteConfig] Body: bundle=$bundle")

        try {
            val response = HttpClientProvider.client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("bundle" to bundle))
            }

            val status = response.status.value
            val raw = response.bodyAsText()

            println("🟣 [RemoteConfig] HTTP Status = $status")
            println("🟣 [RemoteConfig] Raw HTTP body = $raw")

            when (status) {
                200 -> {
                    return try {
                        val parsed = Json.decodeFromString<InstallResponse>(raw)
                        println("🟢 [RemoteConfig] Parsed URL = ${parsed.url}")
                        parsed.url
                    } catch (decodeErr: Exception) {
                        println("🔴 [RemoteConfig] JSON decoding error: ${decodeErr.message}")
                        null
                    }
                }

                403, 404 -> {
                    println("🔴 [RemoteConfig] Server error code = $status")
                    return null
                }

                else -> {
                    println("🔴 [RemoteConfig] Unexpected status = $status")
                    return null
                }
            }

        } catch (networkErr: Exception) {
            println("🔴 [RemoteConfig] Network error: ${networkErr.message}")
            return null
        }
    }

    override suspend fun saveUrl(url: String) {
        println("🟡 [RemoteConfig] Saving URL to settings → $url")
        settings.putString("remote_url", url)
    }

    override suspend fun getSavedUrl(): String? {
        val saved = settings.getString("remote_url", "").ifBlank { null }
        println("🟣 [RemoteConfig] Loaded saved URL → $saved")
        return saved
    }
}