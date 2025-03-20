package com.example.practicacrud

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.DataItem
import com.example.practicacrud.utils.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateEditActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var authManager: AuthManager
    private var dataId: Int = -1 // ID del registro (si es una edición)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit)

        etName = findViewById(R.id.etName)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        authManager = AuthManager(this)

        // Verificar permisos de administrador
        if (!authManager.isAdmin()) {
            Toast.makeText(this, "Acceso denegado. Se requiere rol de administrador.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Obtener los datos del intent (si es una edición)
        dataId = intent.getIntExtra("id", -1)
        val existingName = intent.getStringExtra("name")
        val existingDescription = intent.getStringExtra("description")

        // Si es una edición, llenar los campos con los datos existentes
        if (dataId != -1 && existingName != null && existingDescription != null) {
            etName.setText(existingName)
            etDescription.setText(existingDescription)
            title = "Editar Registro"
        } else {
            title = "Crear Nuevo Registro"
        }

        // Botón para guardar
        btnSave.setOnClickListener {
            val newName = etName.text.toString()
            val newDescription = etDescription.text.toString()

            if (newName.isNotEmpty() && newDescription.isNotEmpty()) {
                if (dataId == -1) {
                    // Crear un nuevo registro
                    createData(DataItem(0, newName, newDescription))
                } else {
                    // Actualizar un registro existente
                    updateData(dataId, DataItem(dataId, newName, newDescription))
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para cancelar
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createData(data: DataItem) {
        val token = authManager.getToken()
        Log.d("CreateEditActivity", "Token enviado: $token") // Verifica el token

        RetrofitClient.create(authManager).createData(data).enqueue(object : Callback<DataItem> {
            override fun onResponse(call: Call<DataItem>, response: Response<DataItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateEditActivity, "Registro creado", Toast.LENGTH_SHORT).show()
                    Log.d("CreateEditActivity", "Registro creado: ${response.body()}")
                    finish() // Cierra la actividad después de guardar
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@CreateEditActivity, "Sesión expirada. Por favor, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
                        authManager.clearAll()
                        finish()
                    } else {
                        Toast.makeText(this@CreateEditActivity, "Error al crear registro: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CreateEditActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<DataItem>, t: Throwable) {
                Toast.makeText(this@CreateEditActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateEditActivity", "Error: ${t.message}")
            }
        })
    }

    private fun updateData(id: Int, data: DataItem) {
        RetrofitClient.create(authManager).updateData(id, data).enqueue(object : Callback<DataItem> {
            override fun onResponse(call: Call<DataItem>, response: Response<DataItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateEditActivity, "Registro actualizado", Toast.LENGTH_SHORT).show()
                    Log.d("CreateEditActivity", "Registro actualizado: ${response.body()}")
                    finish() // Cierra la actividad después de guardar
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@CreateEditActivity, "Sesión expirada. Por favor, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
                        authManager.clearAll()
                        finish()
                    } else {
                        Toast.makeText(this@CreateEditActivity, "Error al actualizar registro: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CreateEditActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<DataItem>, t: Throwable) {
                Toast.makeText(this@CreateEditActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateEditActivity", "Error: ${t.message}")
            }
        })
    }
}