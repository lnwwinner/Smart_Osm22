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
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class SmartOsmApiClient(
    private val accessTokenProvider: () -> String? = { null }
) {
    private val tokenMutex = Mutex()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor)
        .addInterceptor(NetworkDiagnosticsInterceptor)
        .build()

    private val dataHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor)
        .addInterceptor(AccessTokenInterceptor(accessTokenProvider))
        .addInterceptor(NetworkDiagnosticsInterceptor)
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

    private object NetworkDiagnosticsInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val host = request.url.host
            return try {
                val networkResponse = chain.proceed(request)
                if (!networkResponse.isSuccessful) {
                    Log.w("SmartOsmApi", "HTTP " + networkResponse.code + " " + request.method + " " + host + request.url.encodedPath)
                }
                networkResponse
            } catch (error: SSLHandshakeException) {
                Log.e("SmartOsmApi", "TLS_HANDSHAKE_FAILED host=" + host, error)
                throw error
            } catch (error: UnknownHostException) {
                Log.e("SmartOsmApi", "DNS_FAILED host=" + host, error)
                throw error
            } catch (error: SocketTimeoutException) {
                Log.e("SmartOsmApi", "TIMEOUT host=" + host, error)
                throw error
            } catch (error: ConnectException) {
                Log.e("SmartOsmApi", "CONNECTION_FAILED host=" + host, error)
                throw error
            } catch (error: IOException) {
                Log.e("SmartOsmApi", "NETWORK_IO_FAILED host=" + host + " type=" + error.javaClass.simpleName, error)
                throw error
            }
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
            return chain.proceed(builder.build())
        }
    }
}