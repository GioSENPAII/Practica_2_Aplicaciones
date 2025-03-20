package com.example.practicacrud.utils

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Guardar el token
    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    // Obtener el token
    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    // Guardar el rol
    fun saveRole(role: String) {
        sharedPreferences.edit().putString("role", role).apply()
    }

    // Obtener el rol
    fun getRole(): String? {
        return sharedPreferences.getString("role", null)
    }

    // Guardar el ID de usuario
    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("user_id", userId).apply()
    }

    // Obtener el ID de usuario
    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    // Guardar el nombre de usuario
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    // Obtener el nombre de usuario
    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    // Eliminar todos los datos (para cerrar sesión)
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    // Eliminar el token (para cerrar sesión)
    fun clearToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    // Eliminar el rol
    fun clearRole() {
        sharedPreferences.edit().remove("role").apply()
    }

    // Verificar si el usuario está autenticado
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    // Verificar si el usuario es administrador
    fun isAdmin(): Boolean {
        return getRole() == "admin"
    }
}