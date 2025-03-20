package com.example.practicacrud.api

import com.example.practicacrud.models.LoginResponse
import com.example.practicacrud.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun registerUser(@Body user: User): Call<LoginResponse>

    @POST("login")
    fun loginUser(@Body user: User): Call<LoginResponse>
}