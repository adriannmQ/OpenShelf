package com.example.openshelf

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MenuPrincipalActivity : AppCompatActivity() {

    private lateinit var cardVerLibros: MaterialCardView
    private lateinit var cardAnadirLibro: MaterialCardView
    private lateinit var cardUsuarios: MaterialCardView
    private lateinit var cardPrestamos: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_admin)

        cardVerLibros   = findViewById(R.id.cardVerLibros)
        cardAnadirLibro = findViewById(R.id.cardAnadirLibro)
        cardUsuarios    = findViewById(R.id.cardUsuarios)
        cardPrestamos   = findViewById(R.id.cardPrestamos)

        val usuario = intent.getStringExtra("USUARIO")
        if (usuario != null) {
            val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
            tvBienvenida.text = "Bienvenido, $usuario"
        }

        cardVerLibros.setOnClickListener {
            startActivity(Intent(this, ListarLibrosActivity::class.java))
        }

        cardAnadirLibro.setOnClickListener {
            startActivity(Intent(this, AnadirLibroActivity::class.java))
        }

        cardUsuarios.setOnClickListener {
            startActivity(Intent(this, UsuariosActivity::class.java))
        }

        cardPrestamos.setOnClickListener {
            startActivity(Intent(this, PrestamosActivity::class.java))
        }
    }
}