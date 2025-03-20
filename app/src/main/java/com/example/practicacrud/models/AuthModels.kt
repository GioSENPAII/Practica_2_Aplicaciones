package com.example.practicacrud.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: String = "user" // Por defecto, el rol será "user"
)

data class UserResponse(
    val id: Int,
    val username: String,
    val role: String,
    val profilePicture: String?
)