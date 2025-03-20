package com.example.practicacrud.api

import com.example.practicacrud.models.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // Operaciones CRUD para datos
    @GET("data")
    fun getData(): Call<List<DataItem>>

    @POST("data")
    fun createData(@Body data: DataItem): Call<DataItem>

    @PUT("data/{id}")
    fun updateData(@Path("id") id: Int, @Body data: DataItem): Call<DataItem>

    @DELETE("data/{id}")
    fun deleteData(@Path("id") id: Int): Call<Void>

    // Operaciones CRUD para usuarios (admin)
    @GET("users")
    fun getAllUsers(): Call<List<UserResponse>>

    @POST("users")
    fun createUser(@Body registerRequest: RegisterRequest): Call<UserResponse>

    @PUT("users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: UserResponse): Call<UserResponse>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: Int): Call<Void>

    // Para que los administradores gestionen fotos de perfil de usuarios
    @Multipart
    @POST("admin/users/{id}/picture")
    fun uploadUserProfilePicture(@Path("id") userId: Int, @Part image: MultipartBody.Part): Call<UserResponse>

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