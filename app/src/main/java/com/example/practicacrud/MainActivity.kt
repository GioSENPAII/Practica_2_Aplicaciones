package com.example.practicacrud

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.LoginResponse
import com.example.practicacrud.models.User
import com.example.practicacrud.utils.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar AuthManager
        authManager = AuthManager(this)

        // Ejemplo de registro
        val newUser = User(0, "user1", "password123", "user")
        registerUser(newUser)

        // Ejemplo de login
        val loginUser = User(0, "user1", "password123", "user")
        loginUser(loginUser)
    }

    private fun registerUser(user: User) {
        // Obtener una instancia de ApiService usando RetrofitClient.create(authManager)
        val apiService = RetrofitClient.create(authManager)
        apiService.registerUser(user).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Usuario registrado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginUser(user: User) {
        // Obtener una instancia de ApiService usando RetrofitClient.create(authManager)
        val apiService = RetrofitClient.create(authManager)
        apiService.loginUser(user).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        // Guardar el token en SharedPreferences
                        authManager.saveToken(token)
                        Toast.makeText(this@MainActivity, "Token guardado: $token", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error en el login", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}