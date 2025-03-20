package com.example.practicacrud.api

import com.example.practicacrud.utils.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.7:3000/" // Usa 10.0.2.2 para el emulador 192.168.100.7

    fun create(authManager: AuthManager): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = authManager.getToken()
                val request: Request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", token)
                        .build()
                } else {
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