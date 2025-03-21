package com.example.practicacrud.api

import android.util.Log
import com.example.practicacrud.utils.AuthManager
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// En app/src/main/java/com/example/practicacrud/api/RetrofitClient.kt
object RetrofitClient {
    const val BASE_URL = "http://192.168.100.7:3000/" // Usa la IP correcta

    fun create(authManager: AuthManager): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = authManager.getToken()
                val request: Request = if (!token.isNullOrEmpty()) {
                    Log.d("RetrofitClient", "Enviando solicitud con token: $token")
                    chain.request().newBuilder()
                        .addHeader("Authorization", token)
                        .build()
                } else {
                    Log.d("RetrofitClient", "Enviando solicitud sin token")
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}