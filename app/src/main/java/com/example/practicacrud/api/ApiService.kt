package com.example.practicacrud.api

import com.example.practicacrud.models.DataItem
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
}