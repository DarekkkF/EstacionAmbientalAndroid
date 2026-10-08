package com.example.estacionambiental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertReference: DatabaseReference

    private lateinit var tvUsuario: TextView
    private lateinit var tvConexion: TextView
    private lateinit var tvIndice: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvLuz: TextView
    private lateinit var tvGas: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var layoutAlerta: LinearLayout
    private lateinit var tvAlertaMensaje: TextView
    private lateinit var btnAlerta: Button
    private lateinit var btnAtenderAlerta: Button
    private lateinit var btnCerrarSesion: Button

    private var alertaInicializada = false
    private var alertaAnterior = false
    private var mensajeAnterior = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val usuario = auth.currentUser
        if (usuario == null) {
            volverAlLogin()
            return
        }

        database = FirebaseDatabase.getInstance()
            .getReference("estaciones/estacion01/actual")

        alertReference = FirebaseDatabase.getInstance()
            .getReference("control/estacion01")

        tvUsuario = findViewById(R.id.tvUsuario)
        tvConexion = findViewById(R.id.tvConexion)
        tvIndice = findViewById(R.id.tvIndice)
        tvEstado = findViewById(R.id.tvEstado)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvHumedad = findViewById(R.id.tvHumedad)
        tvLuz = findViewById(R.id.tvLuz)
        tvGas = findViewById(R.id.tvGas)
        tvUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion)
        layoutAlerta = findViewById(R.id.layoutAlerta)
        tvAlertaMensaje = findViewById(R.id.tvAlertaMensaje)
        btnAlerta = findViewById(R.id.btnAlerta)
        btnAtenderAlerta = findViewById(R.id.btnAtenderAlerta)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        tvUsuario.text = usuario.email ?: "Usuario autenticado"

        leerDatos()
        escucharAlertas()

        btnAlerta.setOnClickListener {
            enviarAlerta()
        }

        btnAtenderAlerta.setOnClickListener {
            atenderAlerta()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun leerDatos() {

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) {
                    tvConexion.text = "● Esperando datos"
                    tvConexion.setTextColor(getColor(R.color.warning_text))
                    tvUltimaActualizacion.text =
                        "Última actualización: todavía no hay datos"
                    return
                }

                val temperatura = obtenerDouble(snapshot, "temperatura")
                val humedad = obtenerDouble(snapshot, "humedad")
                val luz = obtenerInt(snapshot, "luz")
                val gas = obtenerInt(snapshot, "gas")
                val indice = obtenerInt(snapshot, "indiceConfort")
                val estado =
                    snapshot.child("estado").getValue(String::class.java)
                        ?: "Sin datos"

                tvTemperatura.text = "%.1f °C".format(Locale.getDefault(), temperatura)
                tvHumedad.text = "%.1f %%".format(Locale.getDefault(), humedad)
                tvLuz.text = luz.toString()
                tvGas.text = gas.toString()
                tvIndice.text = "$indice/100"
                tvEstado.text = estado

                tvConexion.text = "● En línea"
                tvConexion.setTextColor(getColor(R.color.success_text))

                val timestamp = obtenerLong(snapshot, "timestamp")
                if (timestamp > 0) {
                    tvUltimaActualizacion.text =
                        "Última actualización: ${formatearFecha(timestamp)}"
                } else {
                    tvUltimaActualizacion.text =
                        "Última actualización: disponible en tiempo real"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                tvConexion.text = "● Sin acceso"
                tvConexion.setTextColor(getColor(R.color.alert_text))
                Toast.makeText(
                    this@MainActivity,
                    "No se pudieron consultar los datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun escucharAlertas() {

        alertReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val alerta =
                    snapshot.child("alerta").getValue(Boolean::class.java) ?: false

                val mensaje =
                    snapshot.child("mensaje").getValue(String::class.java)
                        ?: "Revisar ambiente"

                if (alerta) {
                    layoutAlerta.visibility = View.VISIBLE
                    tvAlertaMensaje.text = mensaje

                    val nuevaAlerta =
                        !alertaInicializada || !alertaAnterior || mensaje != mensajeAnterior

                    if (nuevaAlerta) {
                        Toast.makeText(
                            this@MainActivity,
                            "ALERTA: $mensaje",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    layoutAlerta.visibility = View.GONE
                }

                alertaAnterior = alerta
                mensajeAnterior = mensaje
                alertaInicializada = true
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "No se pudo escuchar la alerta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun enviarAlerta() {

        val email = auth.currentUser?.email ?: "Usuario"

        val datos = mapOf(
            "alerta" to true,
            "mensaje" to "Revisar las condiciones ambientales de la estación.",
            "actualizadoPor" to email
        )

        btnAlerta.isEnabled = false

        alertReference.setValue(datos)
            .addOnSuccessListener {
                btnAlerta.isEnabled = true
                Toast.makeText(
                    this,
                    "Alerta enviada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                btnAlerta.isEnabled = true
                Toast.makeText(
                    this,
                    "No se pudo enviar la alerta",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun atenderAlerta() {

        val email = auth.currentUser?.email ?: "Usuario"

        val datos = mapOf(
            "alerta" to false,
            "mensaje" to "Alerta atendida",
            "actualizadoPor" to email
        )

        alertReference.setValue(datos)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Alerta marcada como atendida",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "No se pudo actualizar la alerta",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cerrarSesion() {
        auth.signOut()
        volverAlLogin()
    }

    private fun volverAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun obtenerDouble(snapshot: DataSnapshot, campo: String): Double {
        return (snapshot.child(campo).value as? Number)?.toDouble() ?: 0.0
    }

    private fun obtenerInt(snapshot: DataSnapshot, campo: String): Int {
        return (snapshot.child(campo).value as? Number)?.toInt() ?: 0
    }

    private fun obtenerLong(snapshot: DataSnapshot, campo: String): Long {
        return (snapshot.child(campo).value as? Number)?.toLong() ?: 0L
    }

    private fun formatearFecha(timestamp: Long): String {
        var tiempo = timestamp

        // El ESP32 envía time(nullptr) en segundos.
        // Firebase también puede recibir milisegundos.
        if (tiempo < 1_000_000_000_000L) {
            tiempo *= 1000
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        )

        return formato.format(Date(tiempo))
    }
}
