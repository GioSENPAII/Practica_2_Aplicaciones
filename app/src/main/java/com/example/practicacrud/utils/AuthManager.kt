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

    // Eliminar el token (para cerrar sesión)
    fun clearToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    // Verificar si el usuario está autenticado
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}