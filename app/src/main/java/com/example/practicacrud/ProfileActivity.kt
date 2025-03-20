package com.example.practicacrud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.practicacrud.api.RetrofitClient
import com.example.practicacrud.api.RetrofitClient.BASE_URL
import com.example.practicacrud.models.UserResponse
import com.example.practicacrud.utils.AuthManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.bumptech.glide.load.engine.DiskCacheStrategy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var etUsername: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var btnChooseImage: Button
    private lateinit var authManager: AuthManager
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
        setContentView(R.layout.activity_profile)

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        etUsername = findViewById(R.id.etUsername)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        authManager = AuthManager(this)

        // Cargar datos del perfil
        loadUserProfile()

        // Configurar botón para elegir imagen
        btnChooseImage.setOnClickListener {
            openImagePicker()
        }

        // Configurar botón para actualizar perfil
        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadUserProfile() {
        RetrofitClient.create(authManager).getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        etUsername.setText(user.username)

                        // Cargar la imagen de perfil si existe
                        user.profilePicture?.let { profilePic ->
                            // Usar Glide para cargar la imagen
                            Glide.with(this@ProfileActivity)
                                .load(BASE_URL + profilePic)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(ivProfilePicture)
                        }
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error: ${t.message}")
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun updateProfile() {
        // Obtener el nombre de usuario actualizado
        val newUsername = etUsername.text.toString().trim()

        // Si hay un nuevo nombre de usuario y es diferente del actual
        if (newUsername.isNotEmpty() && newUsername != authManager.getUsername()) {
            // Actualizar nombre de usuario (sin imagen)
            updateUsername(newUsername)
        }

        // Si hay una imagen seleccionada, subirla
        selectedImageUri?.let { uri ->
            uploadProfilePicture(uri)
        }

        // Si no hay cambios
        if (newUsername.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUsername(newUsername: String) {
        // Crear objeto con datos actualizados
        val updatedUser = UserResponse(
            id = authManager.getUserId(),
            username = newUsername,
            role = authManager.getRole() ?: "user",
            profilePicture = null // No modificamos la imagen aquí
        )

        RetrofitClient.create(authManager).updateUserProfile(updatedUser)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        // Actualizar los datos guardados localmente
                        authManager.saveUsername(newUsername)
                        Toast.makeText(this@ProfileActivity, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error al actualizar el nombre de usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "Error: ${t.message}")
                }
            })
    }

    // Modificar la función uploadProfilePicture en ProfileActivity.kt
    private fun uploadProfilePicture(uri: Uri) {
        try {
            // Convertir Uri a File con un nombre único basado en timestamp
            val timestamp = System.currentTimeMillis()
            val file = File(cacheDir, "temp_image_${timestamp}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // Crear MultipartBody.Part para la imagen
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // Agregar log para depuración
            Log.d("ProfileActivity", "Subiendo imagen: ${file.name}, tamaño: ${file.length()}")

            // Enviar la imagen al servidor
            RetrofitClient.create(authManager).uploadProfilePicture(body)
                .enqueue(object : Callback<UserResponse> {
                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileActivity, "Imagen actualizada correctamente", Toast.LENGTH_SHORT).show()
                            // Si se actualizó el perfil, actualizar también los datos guardados localmente
                            response.body()?.let { user ->
                                // Intentar cargar la imagen actualizada inmediatamente
                                user.profilePicture?.let { profilePic ->
                                    // Usar Glide con skipMemoryCache y diskCacheStrategy para evitar problemas de caché
                                    Glide.with(this@ProfileActivity)
                                        .load(BASE_URL + profilePic)
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .into(ivProfilePicture)
                                }
                            }
                        } else {
                            val errorMsg = "Error al actualizar imagen: ${response.code()}, ${response.errorBody()?.string()}"
                            Toast.makeText(this@ProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                            Log.e("ProfileActivity", errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                        val errorMsg = "Error de conexión: ${t.message}"
                        Toast.makeText(this@ProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        Log.e("ProfileActivity", errorMsg)
                    }
                })
        } catch (e: Exception) {
            val errorMsg = "Error al procesar la imagen: ${e.message}"
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            Log.e("ProfileActivity", errorMsg)
        }
    }
}