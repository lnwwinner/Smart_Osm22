package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmartOsmLoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SmartOsmTokenResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    val tokenType: String? = null,
    val user: SmartOsmApiUser? = null
)

@JsonClass(generateAdapter = true)
data class SmartOsmApiUser(
    val id: String? = null,
    val username: String? = null,
    val name: String? = null,
    val role: String? = null,
    @Json(name = "village_no") val villageNo: String? = null
)
