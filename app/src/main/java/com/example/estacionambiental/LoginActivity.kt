package com.example.estacionambiental

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnCrearCuenta: Button
    private lateinit var cbMostrarPassword: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta)
        cbMostrarPassword = findViewById(R.id.cbMostrarPassword)

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        btnCrearCuenta.setOnClickListener {
            crearCuenta()
        }

        cbMostrarPassword.setOnCheckedChangeListener { _, mostrar ->
            if (mostrar) {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            abrirPrincipal()
        }
    }

    private fun iniciarSesion() {

        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Completa todos los campos")
            return
        }

        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener(this) { task ->

                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    mostrarMensaje("Inicio de sesión correcto")
                    abrirPrincipal()
                } else {
                    mostrarMensaje(
                        "No se pudo iniciar sesión: ${task.exception?.message ?: "verifica tus datos"}"
                    )
                }
            }
    }

    private fun crearCuenta() {

        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Completa todos los campos")
            return
        }

        if (password.length < 6) {
            mostrarMensaje("La contraseña debe tener al menos 6 caracteres")
            return
        }

        btnCrearCuenta.isEnabled = false

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener(this) { task ->

                btnCrearCuenta.isEnabled = true

                if (task.isSuccessful) {
                    mostrarMensaje("Cuenta creada correctamente")
                    abrirPrincipal()
                } else {
                    mostrarMensaje(
                        "No se pudo crear la cuenta: ${task.exception?.message ?: "revisa los datos"}"
                    )
                }
            }
    }

    private fun abrirPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}
