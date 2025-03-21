package com.example.practicacrud

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.practicacrud.adapters.UserAdapter
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.UserResponse
import com.example.practicacrud.utils.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrudActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreate: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var authManager: AuthManager
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<UserResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        // Ocultar el ActionBar para evitar que el título se sobreponga
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.recyclerView)
        btnCreate = findViewById(R.id.btnCreate)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        authManager = AuthManager(this)

        // Verificar que el usuario es administrador
        if (!authManager.isAdmin()) {
            Toast.makeText(this, "Acceso denegado. Se requiere rol de administrador.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadUsers()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList,
            onEditClick = { user -> editUser(user) },
            onDeleteClick = { user -> deleteUser(user) }
        )
        recyclerView.adapter = adapter

        // Cargar datos iniciales
        loadUsers()

        // Botón para crear un nuevo usuario
        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateEditUserActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando la actividad vuelva a estar activa
        loadUsers()
    }

    private fun loadUsers() {
        swipeRefreshLayout.isRefreshing = true

        RetrofitClient.create(authManager).getAllUsers().enqueue(object : Callback<List<UserResponse>> {
            override fun onResponse(call: Call<List<UserResponse>>, response: Response<List<UserResponse>>) {
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    userList.clear()
                    response.body()?.let { userList.addAll(it) }
                    adapter.notifyDataSetChanged()

                    // Mostrar mensaje si no hay datos
                    if (userList.isEmpty()) {
                        Toast.makeText(this@CrudActivity, "No hay usuarios disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (response.code() == 401) {
                        // Token expirado o inválido
                        Toast.makeText(this@CrudActivity, "Sesión expirada. Por favor, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
                        authManager.clearAll()
                        val intent = Intent(this@CrudActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@CrudActivity, "Error al cargar usuarios: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CrudActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<UserResponse>>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@CrudActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CrudActivity", "Error: ${t.message}")
            }
        })
    }

    private fun editUser(user: UserResponse) {
        val intent = Intent(this, CreateEditUserActivity::class.java).apply {
            putExtra("id", user.id)
            putExtra("username", user.username)
            putExtra("role", user.role)
            putExtra("profilePicture", user.profilePicture)
        }
        startActivity(intent)
    }

    private fun deleteUser(user: UserResponse) {
        // No permitir eliminar al propio administrador
        if (user.id == authManager.getUserId()) {
            Toast.makeText(this, "No puedes eliminarte a ti mismo", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.create(authManager).deleteUser(user.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    userList.remove(user)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@CrudActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CrudActivity, "Error al eliminar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("CrudActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CrudActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CrudActivity", "Error: ${t.message}")
            }
        })
    }
}