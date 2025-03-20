package com.example.practicacrud

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.DataItem
import com.example.practicacrud.utils.AuthManager
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ocultar el ActionBar
        supportActionBar?.hide()

        // Inicializar AuthManager
        authManager = AuthManager(this)

        // Configurar el menú de navegación
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_login -> {
                    // Navegar a la pantalla de inicio de sesión
                    true
                }
                R.id.nav_register -> {
                    // Navegar a la pantalla de registro
                    true
                }
                R.id.nav_crud -> {
                    if (authManager.getRole() == "admin") {
                        val intent = Intent(this, CrudActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_profile -> {
                    // Navegar a la pantalla de perfil
                    true
                }
                else -> false
            }
        }

        // Ejemplo de operaciones CRUD
        val newData = DataItem(0, "Nuevo Item", "Descripción del nuevo item")
        createData(newData)
        getData()
    }

    private fun getData() {
        RetrofitClient.create(authManager).getData().enqueue(object : Callback<List<DataItem>> {
            override fun onResponse(call: Call<List<DataItem>>, response: Response<List<DataItem>>) {
                if (response.isSuccessful) {
                    val dataList = response.body()
                    dataList?.forEach { item ->
                        Log.d("MainActivity", "Item: ${item.name}")
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<DataItem>>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión: ${t.message}")
            }
        })
    }

    private fun createData(data: DataItem) {
        RetrofitClient.create(authManager).createData(data).enqueue(object : Callback<DataItem> {
            override fun onResponse(call: Call<DataItem>, response: Response<DataItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Dato creado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error al crear dato", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DataItem>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión: ${t.message}")
            }
        })
    }

    private fun updateData(id: Int, data: DataItem) {
        RetrofitClient.create(authManager).updateData(id, data).enqueue(object : Callback<DataItem> {
            override fun onResponse(call: Call<DataItem>, response: Response<DataItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Dato actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error al actualizar dato", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DataItem>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión: ${t.message}")
            }
        })
    }

    private fun deleteData(id: Int) {
        RetrofitClient.create(authManager).deleteData(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Dato eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error al eliminar dato", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión: ${t.message}")
            }
        })
    }
}