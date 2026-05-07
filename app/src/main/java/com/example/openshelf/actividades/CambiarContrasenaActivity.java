package com.example.openshelf.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
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
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Esta pantalla permite cambiar la contraseña de un usuario existente.
// Solo necesito el email para identificar al usuario y la nueva contraseña que quiero asignarle.
public class CambiarContrasenaActivity extends AppCompatActivity {

    private TextInputLayout   tilEmail, tilNuevaContrasena, tilConfirmarContrasena;
    private TextInputEditText etEmail, etNuevaContrasena, etConfirmarContrasena;
    private MaterialButton    btnGuardar, btnCancelar;
    private LinearProgressIndicator progressBar;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilEmail               = findViewById(R.id.tilEmail);
        tilNuevaContrasena     = findViewById(R.id.tilNuevaContrasena);
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena);
        etEmail                = findViewById(R.id.etEmail);
        etNuevaContrasena      = findViewById(R.id.etNuevaContrasena);
        etConfirmarContrasena  = findViewById(R.id.etConfirmarContrasena);
        btnGuardar             = findViewById(R.id.btnGuardar);
        btnCancelar            = findViewById(R.id.btnCancelar);
        progressBar            = findViewById(R.id.progressBar);

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> validarYCambiar());
    }


    private void validarYCambiar() {

        // Limpio los errores anteriores
        tilEmail.setError(null);
        tilNuevaContrasena.setError(null);
        tilConfirmarContrasena.setError(null);

        String email               = etEmail.getText()               != null ? etEmail.getText().toString().trim()               : "";
        String nuevaContrasena     = etNuevaContrasena.getText()     != null ? etNuevaContrasena.getText().toString()             : "";
        String confirmarContrasena = etConfirmarContrasena.getText() != null ? etConfirmarContrasena.getText().toString()         : "";

        boolean valido = true;

        // Compruebo que el email tenga un formato correcto
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Introduce un correo válido");
            valido = false;
        }

        // Compruebo que la nueva contraseña tenga al menos 6 caracteres
        if (TextUtils.isEmpty(nuevaContrasena) || nuevaContrasena.length() < 6) {
            tilNuevaContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            valido = false;
        }

        // Compruebo que la confirmación coincide con la contraseña nueva
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            tilConfirmarContrasena.setError("Las contraseñas no coinciden");
            valido = false;
        }

        if (!valido) return;

        setFormEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        final String emailFinal           = email;
        final String nuevaContrasenaFinal = nuevaContrasena;

        executor.execute(() -> {
            try (Connection conn = ConexionMySQL.obtenerConexion()) {

                // Primero compruebo que el email existe en la base de datos.
                // Si no existe no tiene sentido intentar actualizar nada.
                String checkSql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, emailFinal);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();

                    if (rs.getInt(1) == 0) {
                        handler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            setFormEnabled(true);
                            tilEmail.setError("No existe ningún usuario con ese correo");
                        });
                        return;
                    }
                }

                // El email existe, así que actualizo solo la columna contrasena
                String updateSql = "UPDATE usuarios SET contrasena = ? WHERE email = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, nuevaContrasenaFinal);
                    updateStmt.setString(2, emailFinal);
                    updateStmt.executeUpdate();
                }

                // Todo fue bien, aviso al usuario y cierro la pantalla
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    setFormEnabled(true);
                    Toast.makeText(this, "Error al cambiar la contraseña: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }


    private void setFormEnabled(boolean enabled) {
        etEmail.setEnabled(enabled);
        etNuevaContrasena.setEnabled(enabled);
        etConfirmarContrasena.setEnabled(enabled);
        btnGuardar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}