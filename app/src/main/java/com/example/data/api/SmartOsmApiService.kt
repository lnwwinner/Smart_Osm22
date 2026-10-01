package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface SmartOsmAuthService {
    @POST(SmartOsmApiConfig.LOGIN_PATH)
    suspend fun login(@Body request: SmartOsmLoginRequest): SmartOsmTokenResponse

    @POST(SmartOsmApiConfig.REFRESH_TOKEN_PATH)
    suspend fun refresh(@QueryMap params: Map<String, String>): SmartOsmTokenResponse
}

/**
 * Endpoint surface mirrored from routes observed in the reference APK.
 * Query parameters remain open so the exact server schema is not guessed.
 */
interface SmartOsmDataService {
    @GET(SmartOsmApiConfig.HOUSEHOLDS_PATH)
    suspend fun households(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>

    @GET(SmartOsmApiConfig.MOSQUITO_LARVAE_PATH)
    suspend fun mosquitoLarvae(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>

    @GET(SmartOsmApiConfig.REPORTS_PATH)
    suspend fun reports(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>

    @GET(SmartOsmApiConfig.NCD_SCREENINGS_PATH)
    suspend fun ncdScreenings(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>

    @GET(SmartOsmApiConfig.LOOKUPS_PATH + "/{resource}")
    suspend fun lookup(
        @Path("resource") resource: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<okhttp3.ResponseBody>

    @GET(SmartOsmApiConfig.NOTIFICATIONS_PATH + "/unread-count")
    suspend fun unreadNotificationCount(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>

    @POST(SmartOsmApiConfig.NOTIFICATIONS_PATH + "/mark-read")
    suspend fun markNotificationRead(@QueryMap params: Map<String, String> = emptyMap()): Response<okhttp3.ResponseBody>
}
