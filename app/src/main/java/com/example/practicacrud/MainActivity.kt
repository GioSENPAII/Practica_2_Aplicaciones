package com.example.practicacrud

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.practicacrud.utils.AuthManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var authManager: AuthManager

    // Constante para ID de menú de logout
    private val MENU_LOGOUT_ID = 9999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar AuthManager
        authManager = AuthManager(this)

        // Configurar el DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        // Habilitar el botón para mostrar el menú
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configurar el menú de navegación
        navigationView = findViewById(R.id.navigationView)
        setupNavigationMenu()
    }

    private fun setupNavigationMenu() {
        // Actualizar menú según el estado de autenticación
        val menu = navigationView.menu
        val isLoggedIn = authManager.isLoggedIn()
        val isAdmin = authManager.getRole() == "admin"

        // Ocultar/mostrar items según el estado de autenticación y rol
        menu.findItem(R.id.nav_login).isVisible = !isLoggedIn
        menu.findItem(R.id.nav_register).isVisible = !isLoggedIn
        menu.findItem(R.id.nav_crud).isVisible = isLoggedIn && isAdmin
        menu.findItem(R.id.nav_profile).isVisible = isLoggedIn

        // Añadir opción de logout si está autenticado
        if (isLoggedIn) {
            // Si ya existe, eliminar primero para evitar duplicados
            val logoutItem = menu.findItem(MENU_LOGOUT_ID)
            if (logoutItem != null) {
                menu.removeItem(MENU_LOGOUT_ID)
            }
            // Añadir nuevo item de logout
            menu.add(0, MENU_LOGOUT_ID, Menu.NONE, "Cerrar Sesión 🔒")
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                }
                R.id.nav_register -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                    true
                }
                R.id.nav_crud -> {
                    if (isAdmin) {
                        startActivity(Intent(this, CrudActivity::class.java))
                    } else {
                        Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                MENU_LOGOUT_ID -> {
                    authManager.clearToken()
                    authManager.clearRole()
                    startActivity(Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.open()
        return true
    }
}