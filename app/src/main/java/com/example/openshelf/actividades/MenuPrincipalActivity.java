package com.example.openshelf.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.R;
import com.google.android.material.card.MaterialCardView;

public class MenuPrincipalActivity extends AppCompatActivity {

    private MaterialCardView cardVerLibros, cardAnadirLibro, cardUsuarios, cardPrestamos;
    private MaterialCardView cardMarcarPrestado, cardMarcarDevuelto;
    private Button           btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        inicializarVistas();
        configurarListeners();

        // Recojo el nombre del usuario que viene del login
        String usuario = getIntent().getStringExtra("USUARIO");
        if (usuario != null) {
            TextView tvBienvenida = findViewById(R.id.tvBienvenida);
            tvBienvenida.setText("Bienvenid@, " + usuario);
        }
    }

    private void inicializarVistas() {
        cardVerLibros       = findViewById(R.id.cardVerLibros);
        cardAnadirLibro     = findViewById(R.id.cardAnadirLibro);
        cardUsuarios        = findViewById(R.id.cardUsuarios);
        cardPrestamos       = findViewById(R.id.cardPrestamos);
        cardMarcarPrestado  = findViewById(R.id.cardMarcarPrestado);
        cardMarcarDevuelto  = findViewById(R.id.cardMarcarDevuelto);
        btnCerrarSesion     = findViewById(R.id.btnCerrarSesion);
    }

    private void configurarListeners() {
        cardVerLibros.setOnClickListener(v ->
                startActivity(new Intent(this, ListarLibrosActivity.class))
        );

        cardAnadirLibro.setOnClickListener(v ->
                startActivity(new Intent(this, AnadirLibroActivity.class))
        );

        cardUsuarios.setOnClickListener(v ->
                startActivity(new Intent(this, UsuariosActivity.class))
        );

        cardPrestamos.setOnClickListener(v ->
                startActivity(new Intent(this, PrestamosActivity.class))
        );

        cardMarcarPrestado.setOnClickListener(v ->
                startActivity(new Intent(this, AnadirPrestamoActivity.class))
        );

        cardMarcarDevuelto.setOnClickListener(v ->
                startActivity(new Intent(this, MarcarComoDevueltoActivity.class))
        );

        btnCerrarSesion.setOnClickListener(v -> {
            // Cerramos sesión volviendo al Login y limpiando el stack de actividades
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
