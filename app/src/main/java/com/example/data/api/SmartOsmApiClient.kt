package com.example.data.api

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SmartOsmApiClient(
    private val accessTokenProvider: () -> String? = { null }
) {
    private val tokenMutex = Mutex()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor)
        .build()

    private val dataHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor)
        .addInterceptor(AccessTokenInterceptor(accessTokenProvider))
        .build()

    val auth: SmartOsmAuthService = Retrofit.Builder()
        .baseUrl(SmartOsmApiConfig.AUTH_BASE_URL)
        .client(authHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SmartOsmAuthService::class.java)

    val data: SmartOsmDataService = Retrofit.Builder()
        .baseUrl(SmartOsmApiConfig.DATA_BASE_URL)
        .client(dataHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SmartOsmDataService::class.java)

    suspend fun <T> withTokenLock(block: suspend () -> T): T =
        tokenMutex.withLock { block() }

    private object UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "SmartOSM-Android")
                .build()
            return chain.proceed(request)
        }
    }

    private class AccessTokenInterceptor(
        private val provider: () -> String?
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = provider()?.trim().orEmpty()
            val builder = chain.request().newBuilder()
            if (token.isNotEmpty()) {
                builder.header("Authorization", "Bearer " + token)
            }
            return try {
                chain.proceed(builder.build())
            } catch (error: Throwable) {
                Log.w("SmartOsmApi", "API request failed: " + error.message)
                throw error
            }
        }
    }
}
