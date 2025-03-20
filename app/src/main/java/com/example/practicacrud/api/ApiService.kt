package com.example.practicacrud.api

import com.example.practicacrud.models.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // Operaciones CRUD
    @GET("data")
    fun getData(): Call<List<DataItem>>

    @POST("data")
    fun createData(@Body data: DataItem): Call<DataItem>

    @PUT("data/{id}")
    fun updateData(@Path("id") id: Int, @Body data: DataItem): Call<DataItem>

    @DELETE("data/{id}")
    fun deleteData(@Path("id") id: Int): Call<Void>

    // Autenticación
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<UserResponse>

    // Perfil de usuario
    @GET("users/profile")
    fun getUserProfile(): Call<UserResponse>

    @PUT("users/profile")
    fun updateUserProfile(@Body user: UserResponse): Call<UserResponse>

    // Para subir imagen de perfil
    @Multipart
    @POST("users/profile/picture")
    fun uploadProfilePicture(@Part image: MultipartBody.Part): Call<UserResponse>
}