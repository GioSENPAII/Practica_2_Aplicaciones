package com.example.practicacrud

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var authManager: AuthManager
    private lateinit var adapter: DataAdapter
    private val dataList = mutableListOf<DataItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        recyclerView = findViewById(R.id.recyclerView)
        btnCreate = findViewById(R.id.btnCreate)
        authManager = AuthManager(this)

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

    private fun loadData() {
        RetrofitClient.create(authManager).getData().enqueue(object : Callback<List<DataItem>> {
            override fun onResponse(call: Call<List<DataItem>>, response: Response<List<DataItem>>) {
                if (response.isSuccessful) {
                    dataList.clear()
                    response.body()?.let { dataList.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CrudActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<DataItem>>, t: Throwable) {
                Toast.makeText(this@CrudActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@CrudActivity, "Error al eliminar registro", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CrudActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}