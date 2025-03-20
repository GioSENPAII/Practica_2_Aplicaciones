package com.example.practicacrud

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
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
        val isAdmin = authManager.isAdmin()

        // Para usuarios no autenticados
        menu.findItem(R.id.nav_login).isVisible = !isLoggedIn
        menu.findItem(R.id.nav_register).isVisible = !isLoggedIn

        // Para usuarios autenticados
        menu.findItem(R.id.nav_crud).isVisible = isLoggedIn && isAdmin // Solo admin ve CRUD
        menu.findItem(R.id.nav_profile).isVisible = isLoggedIn && !isAdmin // Solo usuarios normales ven Perfil

        // Añadir opción de logout para todos los usuarios autenticados
        if (isLoggedIn) {
            // Si ya existe, eliminar primero para evitar duplicados
            val logoutItem = menu.findItem(MENU_LOGOUT_ID)
            if (logoutItem != null) {
                menu.removeItem(MENU_LOGOUT_ID)
            }
            // Añadir nuevo item de logout
            menu.add(0, MENU_LOGOUT_ID, Menu.NONE, "Cerrar Sesión 🔒")
        }

        // Mostrar mensaje de bienvenida con rol
        val headerView = navigationView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.tvUsername)
        val tvRole = headerView.findViewById<TextView>(R.id.tvRole)

        // Actualizar según usuario logueado
        if (isLoggedIn) {
            val username = authManager.getUsername() ?: "Usuario"
            val role = if (isAdmin) "Administrador" else "Usuario"
            tvUsername.text = username
            tvRole.text = role
        } else {
            tvUsername.text = "Invitado"
            tvRole.text = "Sin sesión"
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
                    authManager.clearAll()
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