package com.example.practicacrud.models

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserResponse? = null
)