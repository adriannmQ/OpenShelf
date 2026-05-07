package com.example.openshelf.actividades;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.openshelf.R;
import com.google.android.material.card.MaterialCardView;

public class UsuariosActivity extends AppCompatActivity {

    private MaterialCardView cardCrearUsuario;
    private MaterialCardView cardBorrarUsuario;
    private MaterialCardView cardModificarUsuario;
    private MaterialCardView cardMostrarUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Enlace de vistas
        cardCrearUsuario    = findViewById(R.id.cardCrearUsuario);
        cardBorrarUsuario   = findViewById(R.id.cardBorrarUsuario);
        cardModificarUsuario = findViewById(R.id.cardModificarUsuario);
        cardMostrarUsuarios = findViewById(R.id.cardMostrarUsuarios);

        // Listeners de navegación
        cardCrearUsuario.setOnClickListener(v -> {
            startActivity(new Intent(this, CrearUsuarioActivity.class));
        });

        cardBorrarUsuario.setOnClickListener(v -> {
            startActivity(new Intent(this, EliminarUsuarioActivity.class));
        });

        cardModificarUsuario.setOnClickListener(v -> {
            startActivity(new Intent(this, CambiarContrasenaActivity.class));
        });

        cardMostrarUsuarios.setOnClickListener(v -> {
            startActivity(new Intent(this, MostrarUsuariosActivity.class));
        });
    }
}
