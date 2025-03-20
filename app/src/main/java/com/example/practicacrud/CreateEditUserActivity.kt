package com.example.practicacrud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.models.RegisterRequest
import com.example.practicacrud.models.UserResponse
import com.example.practicacrud.utils.AuthManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class CreateEditUserActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnChooseImage: Button
    private lateinit var authManager: AuthManager
    private var userId: Int = -1 // ID del usuario (si es una edición)
    private val roles = arrayOf("user", "admin")
    private var selectedImageUri: Uri? = null

    // Launcher para seleccionar imagen de la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivProfilePicture.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit_user)

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        authManager = AuthManager(this)

        // Configurar el spinner de roles
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        // Verificar permisos de administrador
        if (!authManager.isAdmin()) {
            Toast.makeText(this, "Acceso denegado. Se requiere rol de administrador.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar botón para elegir imagen
        btnChooseImage.setOnClickListener {
            openImagePicker()
        }

        // Obtener los datos del intent (si es una edición)
        userId = intent.getIntExtra("id", -1)
        val existingUsername = intent.getStringExtra("username")
        val existingRole = intent.getStringExtra("role")
        val existingProfilePicture = intent.getStringExtra("profilePicture")

        // Si es una edición, llenar los campos con los datos existentes
        if (userId != -1 && existingUsername != null && existingRole != null) {
            etUsername.setText(existingUsername)
            val rolePosition = roles.indexOf(existingRole)
            if (rolePosition != -1) {
                spinnerRole.setSelection(rolePosition)
            }

            // Cargar imagen de perfil si existe
            existingProfilePicture?.let { profilePic ->
                Glide.with(this)
                    .load(RetrofitClient.BASE_URL + profilePic)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivProfilePicture)
            }

            title = "Editar Usuario"
            // En modo edición, la contraseña es opcional
            etPassword.hint = "Nueva contraseña (opcional)"
        } else {
            title = "Crear Nuevo Usuario"
        }

        // Botón para guardar
        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString()
            val newPassword = etPassword.text.toString()
            val newRole = roles[spinnerRole.selectedItemPosition]

            if (newUsername.isNotEmpty()) {
                if (userId == -1) {
                    // Es una creación, se requiere contraseña
                    if (newPassword.isNotEmpty()) {
                        createUser(RegisterRequest(newUsername, newPassword, newRole))
                    } else {
                        Toast.makeText(this, "La contraseña es obligatoria para nuevos usuarios", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Es una edición, la contraseña es opcional
                    updateUser(userId, UserResponse(userId, newUsername, newRole, null), newPassword)
                }
            } else {
                Toast.makeText(this, "El nombre de usuario es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para cancelar
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun createUser(registerRequest: RegisterRequest) {
        RetrofitClient.create(authManager).createUser(registerRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateEditUserActivity, "Usuario creado", Toast.LENGTH_SHORT).show()

                    // Si hay una imagen seleccionada, subirla para el nuevo usuario
                    if (selectedImageUri != null) {
                        response.body()?.id?.let { newUserId ->
                            uploadUserProfilePicture(newUserId, selectedImageUri!!)
                        }
                    }

                    finish() // Cierra la actividad después de guardar
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@CreateEditUserActivity, "Sesión expirada. Por favor, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
                        authManager.clearAll()
                        finish()
                    } else {
                        Toast.makeText(this@CreateEditUserActivity, "Error al crear usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CreateEditUserActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@CreateEditUserActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateEditUserActivity", "Error: ${t.message}")
            }
        })
    }

    private fun updateUser(id: Int, user: UserResponse, newPassword: String?) {
        // Si hay una nueva contraseña, necesitamos actualizar la contraseña en el servidor
        // Esto requeriría un endpoint adicional, pero por ahora solo actualizamos el username y role

        RetrofitClient.create(authManager).updateUser(id, user).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateEditUserActivity, "Usuario actualizado", Toast.LENGTH_SHORT).show()

                    // Si hay una imagen seleccionada, subirla
                    if (selectedImageUri != null) {
                        uploadUserProfilePicture(id, selectedImageUri!!)
                    }

                    finish() // Cierra la actividad después de guardar
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@CreateEditUserActivity, "Sesión expirada. Por favor, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
                        authManager.clearAll()
                        finish()
                    } else {
                        Toast.makeText(this@CreateEditUserActivity, "Error al actualizar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("CreateEditUserActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@CreateEditUserActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateEditUserActivity", "Error: ${t.message}")
            }
        })
    }

    private fun uploadUserProfilePicture(userId: Int, uri: Uri) {
        try {
            // Convertir Uri a File
            val file = File(cacheDir, "temp_image.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // Crear MultipartBody.Part para la imagen
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // Enviar la imagen al servidor
            RetrofitClient.create(authManager).uploadUserProfilePicture(userId, body)
                .enqueue(object : Callback<UserResponse> {
                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                        if (response.isSuccessful) {
                            Log.d("CreateEditUserActivity", "Imagen actualizada correctamente")
                        } else {
                            Log.e("CreateEditUserActivity", "Error al actualizar imagen: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                        Log.e("CreateEditUserActivity", "Error de conexión: ${t.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e("CreateEditUserActivity", "Error al procesar la imagen: ${e.message}")
        }
    }
}