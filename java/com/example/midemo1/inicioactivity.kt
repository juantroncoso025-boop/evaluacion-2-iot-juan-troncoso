package com.example.midemo1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InicioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicioactivity)

        auth = FirebaseAuth.getInstance()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val etId = findViewById<EditText>(R.id.etId)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser
        val nombreUser = user?.displayName ?: "Usuario"
        tvWelcome.text = "¡Bienvenido a Mi Acceso,\n$nombreUser!"


        btnIngresar.setOnClickListener {
            val id = etId.text.toString()
            val nombre = etNombre.text.toString()

            if (id.isNotEmpty() && nombre.isNotEmpty()) {
                val urlString = "http://192.168.137.98/test1/ingreso.php?id=$id&nombre=$nombre"
                ejecutarPeticionWeb(urlString) { respuesta ->
                    Toast.makeText(this, "Respuesta: $respuesta", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Completa el ID y el Nombre", Toast.LENGTH_SHORT).show()
            }
        }


        btnBuscar.setOnClickListener {
            val id = etId.text.toString()

            if (id.isNotEmpty()) {
                val urlString = "http://192.168.137.98/test1/buscar_producto.php?id=$id"
                ejecutarPeticionWeb(urlString) { respuesta ->
                    if (respuesta != "No existe") {
                        etNombre.setText(respuesta)
                        Toast.makeText(this, "¡Datos encontrados!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "El ID no existe en la base de datos", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Ingresa un ID para buscar", Toast.LENGTH_SHORT).show()
            }
        }


        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun ejecutarPeticionWeb(urlString: String, onResult: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            var resultado = "Error de conexión"
            try {
                val url = URL(urlString)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"
                conexion.connectTimeout = 5000
                conexion.readTimeout = 5000

                if (conexion.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conexion.inputStream))
                    val stringBuilder = StringBuilder()
                    var linea: String?
                    while (reader.readLine().also { linea = it } != null) {
                        stringBuilder.append(linea)
                    }
                    reader.close()
                    resultado = stringBuilder.toString()
                }
            } catch (e: Exception) {
                resultado = "Excepción: ${e.message}"
            }

            withContext(Dispatchers.Main) {
                onResult(resultado)
            }
        }
    }
}