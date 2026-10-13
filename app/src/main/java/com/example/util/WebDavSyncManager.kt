package com.example.util

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object WebDavSyncManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun getFullUrl(baseUrl: String, path: String): String {
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val cleanPath = if (path.startsWith("/")) path.substring(1) else path
        return "$cleanBase$cleanPath"
    }

    data class ConnectionDiagnostics(
        val isAuthorized: Boolean,
        val remoteFileExists: Boolean,
        val fileDetails: String?,
        val message: String
    )

    fun testConnectionDiagnostics(url: String, user: String, pass: String, path: String): Result<ConnectionDiagnostics> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("WebDAV sunucu adresi boş olamaz."))
        }
        val requestUrl = if (url.endsWith("/")) url else "$url/"
        val credential = Credentials.basic(user, pass)
        
        // 1. Check server reachability and credentials using PROPFIND/GET on root
        val rootRequest = Request.Builder()
            .url(requestUrl)
            .method("PROPFIND", "".toRequestBody("text/xml".toMediaType()))
            .header("Authorization", credential)
            .header("Depth", "0")
            .build()

        var isAuthorized = false
        try {
            client.newCall(rootRequest).execute().use { response ->
                if (response.code == 401) {
                    return Result.success(ConnectionDiagnostics(
                        isAuthorized = false,
                        remoteFileExists = false,
                        fileDetails = null,
                        message = "Kimlik doğrulama başarısız. Kullanıcı adı veya şifre yanlış."
                    ))
                }
                if (response.isSuccessful || response.code == 207 || response.code == 405) {
                    isAuthorized = true
                }
            }
        } catch (e: IOException) {
            // Fallback to GET for simple/restricted WebDAV servers
            try {
                val fallbackRequest = Request.Builder()
                    .url(requestUrl)
                    .get()
                    .header("Authorization", credential)
                    .build()
                client.newCall(fallbackRequest).execute().use { response ->
                    if (response.code == 401) {
                        return Result.success(ConnectionDiagnostics(
                            isAuthorized = false,
                            remoteFileExists = false,
                            fileDetails = null,
                            message = "Kimlik doğrulama başarısız."
                        ))
                    }
                    if (response.isSuccessful || response.code == 403) {
                        isAuthorized = true
                    }
                }
            } catch (fallbackEx: IOException) {
                return Result.failure(fallbackEx)
            }
        }

        if (!isAuthorized) {
            return Result.success(ConnectionDiagnostics(
                isAuthorized = false,
                remoteFileExists = false,
                fileDetails = null,
                message = "Sunucuya bağlanılamadı veya yetki alınamadı."
            ))
        }

        // 2. Check if the target file exists and retrieve its metadata
        val fileUrl = getFullUrl(url, path)
        val fileCheckRequest = Request.Builder()
            .url(fileUrl)
            .head()
            .header("Authorization", credential)
            .build()

        var remoteFileExists = false
        var fileDetails: String? = null

        try {
            client.newCall(fileCheckRequest).execute().use { response ->
                if (response.isSuccessful) {
                    remoteFileExists = true
                    val length = response.header("Content-Length")
                    val lastModified = response.header("Last-Modified")
                    fileDetails = if (length != null && lastModified != null) {
                        "Dosya boyutu: ${length.toLongOrNull()?.let { "${it / 1024} KB" } ?: length}, Son Güncelleme: $lastModified"
                    } else if (lastModified != null) {
                        "Son Güncelleme: $lastModified"
                    } else {
                        "Mevcut"
                    }
                }
            }
        } catch (e: IOException) {
            // Ignore failure on head, will assume file does not exist yet
        }

        val message = if (remoteFileExists) {
            "Bağlantı Başarılı! Sunucuda yedek dosyası bulundu ($fileDetails)."
        } else {
            "Bağlantı Başarılı! Sunucuda henüz yedek dosyası yok, yeni bir tane oluşturulmaya hazır."
        }

        return Result.success(ConnectionDiagnostics(
            isAuthorized = true,
            remoteFileExists = remoteFileExists,
            fileDetails = fileDetails,
            message = message
        ))
    }

    // Recursively create directory path using MKCOL
    private fun createParentDirectories(baseUrl: String, user: String, pass: String, filePath: String) {
        val credential = Credentials.basic(user, pass)
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        val directoryPart = if (filePath.contains("/")) {
            filePath.substringBeforeLast("/")
        } else {
            ""
        }
        
        if (directoryPart.isBlank()) return
        
        val folders = directoryPart.split("/").filter { it.isNotBlank() }
        var currentPath = cleanBase
        
        for (folder in folders) {
            currentPath = "$currentPath$folder/"
            val request = Request.Builder()
                .url(currentPath)
                .method("MKCOL", "".toRequestBody(null))
                .header("Authorization", credential)
                .build()
                
            try {
                client.newCall(request).execute().use { response ->
                    // 201 Created or 405 Already Exists are successes.
                    if (response.code != 201 && response.code != 405 && response.code != 200) {
                        // Continue anyway
                    }
                }
            } catch (e: Exception) {
                // Continue
            }
        }
    }

    fun backup(url: String, user: String, pass: String, path: String, jsonContent: String): Result<Unit> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("WebDAV sunucu adresi boş olamaz."))
        }
        
        // Auto-create directories before upload
        createParentDirectories(url, user, pass, path)

        val requestUrl = getFullUrl(url, path)
        val credential = Credentials.basic(user, pass)
        val requestBody = jsonContent.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(requestUrl)
            .put(requestBody)
            .header("Authorization", credential)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code == 402) {
                    Result.failure(Exception("Hata 402: Sunucu WebDAV için ücretli bir plan istiyor."))
                } else {
                    Result.failure(Exception("Yedekleme başarısız oldu (Kod: ${response.code})"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    fun restore(url: String, user: String, pass: String, path: String): Result<String> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("WebDAV sunucu adresi boş olamaz."))
        }
        val requestUrl = getFullUrl(url, path)
        val credential = Credentials.basic(user, pass)
        val request = Request.Builder()
            .url(requestUrl)
            .get()
            .header("Authorization", credential)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception("Yedek dosyası boş."))
                    }
                } else if (response.code == 404) {
                    Result.failure(Exception("Yedek dosyası sunucuda bulunamadı (404)."))
                } else if (response.code == 402) {
                    Result.failure(Exception("Hata 402: Sunucu WebDAV için ücretli bir plan istiyor."))
                } else {
                    Result.failure(Exception("Dosya yüklenemedi (Kod: ${response.code})"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Keep compatibility with old method for a safe compile transition, but delegates to the new diagnostics model
    fun testConnection(url: String, user: String, pass: String): Result<Unit> {
        return testConnectionDiagnostics(url, user, pass, "subscriptions_backup.json").map { Unit }
    }
}
