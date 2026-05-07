package com.example.openshelf.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrearUsuarioActivity extends AppCompatActivity {

    private TextInputLayout      tilNombre, tilEmail, tilContrasena, tilRol;
    private TextInputEditText    etNombre, etEmail, etContrasena;
    private AutoCompleteTextView spinnerRol;
    private MaterialButton       btnGuardar, btnCancelar;
    private LinearProgressIndicator progressBar;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Vistas
        tilNombre    = findViewById(R.id.tilNombre);
        tilEmail     = findViewById(R.id.tilEmail);
        tilContrasena  = findViewById(R.id.tilContrasena);
        tilRol       = findViewById(R.id.tilRol);
        etNombre     = findViewById(R.id.etNombre);
        etEmail      = findViewById(R.id.etEmail);
        etContrasena   = findViewById(R.id.etContrasena);
        spinnerRol   = findViewById(R.id.spinnerRol);
        btnGuardar   = findViewById(R.id.btnGuardar);
        btnCancelar  = findViewById(R.id.btnCancelar);
        progressBar  = findViewById(R.id.progressBar);

        // Roles disponibles
        String[] roles = {"usuario", "administrador"};
        ArrayAdapter<String> adapterRol = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRol.setAdapter(adapterRol);
        spinnerRol.setText(roles[0], false); // valor por defecto

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private void validarYGuardar() {
        // Limpiar errores anteriores
        tilNombre.setError(null);
        tilEmail.setError(null);
        tilContrasena.setError(null);
        tilRol.setError(null);

        String nombre   = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String email    = etEmail.getText()  != null ? etEmail.getText().toString().trim()  : "";
        String contrasena = etContrasena.getText()!= null ? etContrasena.getText().toString()     : "";
        String rol      = spinnerRol.getText().toString().trim();

        boolean valido = true;

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre es obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Introduce un correo válido");
            valido = false;
        }
        if (contrasena.length() < 6) {
            tilContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            valido = false;
        }
        if (TextUtils.isEmpty(rol)) {
            tilRol.setError("Selecciona un rol");
            valido = false;
        }

        if (!valido) return;

        // Deshabilitar UI mientras guarda
        setFormEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try (Connection conn = ConexionMySQL.obtenerConexion()) {

                // Comprobar si el email ya existe
                String checkSql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, email);
                    var rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        handler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            setFormEnabled(true);
                            tilEmail.setError("Este correo ya está registrado");
                        });
                        return;
                    }
                }

                // Insertar usuario
                // NOTA: en producción la contraseña debe hashearse (ej. BCrypt).
                // Aquí se guarda en texto plano sólo como prototipo.
                String sql = "INSERT INTO usuarios (nombre, email, contrasena, rol) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nombre);
                    stmt.setString(2, email);
                    stmt.setString(3, contrasena);
                    stmt.setString(4, rol);
                    stmt.executeUpdate();
                }

                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // volvemos a la lista
                });

            } catch (Exception e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    setFormEnabled(true);
                    Toast.makeText(this,
                            "Error al crear usuario: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setFormEnabled(boolean enabled) {
        etNombre.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etContrasena.setEnabled(enabled);
        spinnerRol.setEnabled(enabled);
        btnGuardar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}