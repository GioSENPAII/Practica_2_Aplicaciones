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
import com.example.practicacrud.adapters.DataAdapter
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.DataItem
import com.example.practicacrud.utils.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrudActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreate: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var authManager: AuthManager
    private lateinit var adapter: DataAdapter
    private val dataList = mutableListOf<DataItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

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
            loadData()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DataAdapter(dataList,
            onEditClick = { data -> editData(data) },
            onDeleteClick = { data -> deleteData(data) }
        )
        recyclerView.adapter = adapter

        // Cargar datos iniciales
        loadData()

        // Botón para crear un nuevo registro
        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando la actividad vuelva a estar activa
        loadData()
    }

    private fun loadData() {
        swipeRefreshLayout.isRefreshing = true

        RetrofitClient.create(authManager).getData().enqueue(object : Callback<List<DataItem>> {
            override fun onResponse(call: Call<List<DataItem>>, response: Response<List<DataItem>>) {
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    dataList.clear()
                    response.body()?.let { dataList.addAll(it) }
                    adapter.notifyDataSetChanged()

                    // Mostrar mensaje si no hay datos
                    if (dataList.isEmpty()) {
                        Toast.makeText(this@CrudActivity, "No hay registros disponibles", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@CrudActivity, "Error al cargar datos: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CrudActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<DataItem>>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@CrudActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CrudActivity", "Error: ${t.message}")
            }
        })
    }

    private fun editData(data: DataItem) {
        val intent = Intent(this, CreateEditActivity::class.java).apply {
            putExtra("id", data.id)
            putExtra("name", data.name)
            putExtra("description", data.description)
        }
        startActivity(intent)
    }

    private fun deleteData(data: DataItem) {
        RetrofitClient.create(authManager).deleteData(data.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    dataList.remove(data)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@CrudActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CrudActivity, "Error al eliminar registro: ${response.code()}", Toast.LENGTH_SHORT).show()
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