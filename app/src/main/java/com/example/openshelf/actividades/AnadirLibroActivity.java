package com.example.openshelf.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnadirLibroActivity extends AppCompatActivity {

    private MaterialToolbar      toolbar;
    private TextInputLayout      tilTitulo, tilAutor, tilIsbn;
    private TextInputEditText    etTitulo, etAutor, etIsbn;
    private AutoCompleteTextView actvGenero;
    private RadioGroup           rgEstado;
    private MaterialButton       btnGuardarLibro, btnCancelar;

    // Executor para operaciones de red/base de datos fuera del hilo principal
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_libro);

        toolbar         = findViewById(R.id.toolbar);
        tilTitulo       = findViewById(R.id.tilTitulo);
        tilAutor        = findViewById(R.id.tilAutor);
        tilIsbn         = findViewById(R.id.tilIsbn);
        etTitulo        = findViewById(R.id.etTitulo);
        etAutor         = findViewById(R.id.etAutor);
        etIsbn          = findViewById(R.id.etIsbn);
        actvGenero      = findViewById(R.id.actvGenero);
        rgEstado        = findViewById(R.id.rgEstado);
        btnGuardarLibro = findViewById(R.id.btnGuardarLibro);
        btnCancelar     = findViewById(R.id.btnCancelar);

        // ── Botón de flecha atrás en la toolbar ──────────────────────────────
        toolbar.setNavigationOnClickListener(v -> finish());

        // ── Géneros disponibles ───────────────────────────────────────────────
        String[] generos = {"Ciencia ficción", "Fantasía", "Histórica", "Misterio",
                "Realismo mágico", "Distopía", "Cuentos", "Clásico", "Romance", "Terror"};
        ArrayAdapter<String> adaptadorGenero = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, generos);
        actvGenero.setAdapter(adaptadorGenero);

        // ── Botones ───────────────────────────────────────────────────────────
        btnGuardarLibro.setOnClickListener(v -> guardarLibro());

        // btnCancelar cierra la actividad y vuelve a la pantalla anterior
        btnCancelar.setOnClickListener(v -> finish());
    }

    // ── Validación y guardado ─────────────────────────────────────────────────
    private void guardarLibro() {
        String titulo = etTitulo.getText().toString().trim();
        String autor  = etAutor.getText().toString().trim();
        String isbn   = etIsbn.getText().toString().trim();
        String genero = actvGenero.getText().toString().trim();

        // Validaciones en el hilo principal antes de tocar la BD
        if (titulo.isEmpty()) {
            tilTitulo.setError("El título no puede estar vacío");
            return;
        } else { tilTitulo.setError(null); }

        if (autor.isEmpty()) {
            tilAutor.setError("El autor no puede estar vacío");
            return;
        } else { tilAutor.setError(null); }

        if (isbn.isEmpty()) {
            tilIsbn.setError("El ISBN no puede estar vacío");
            return;
        } else { tilIsbn.setError(null); }

        String estado = (rgEstado.getCheckedRadioButtonId() == R.id.rbDisponible)
                ? "disponible" : "prestado";

        // Deshabilitamos el botón mientras se inserta para evitar doble envío
        btnGuardarLibro.setEnabled(false);

        // ── Operación de base de datos en hilo secundario ─────────────────────
        executor.execute(() -> {
            try (Connection conn = ConexionMySQL.obtenerConexion()) {

                String sql = "INSERT INTO libros (titulo, autor, isbn, genero, estado) " +
                        "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, titulo);
                    ps.setString(2, autor);
                    ps.setString(3, isbn);
                    ps.setString(4, genero.isEmpty() ? null : genero);
                    ps.setString(5, estado);
                    ps.executeUpdate();
                }

                // Volvemos al hilo principal para actualizar la UI
                handler.post(() -> {
                    Toast.makeText(this,
                            "Libro \"" + titulo + "\" guardado correctamente",
                            Toast.LENGTH_SHORT).show();
                    finish(); // cierra la actividad y vuelve atrás
                });

            } catch (Exception e) {
                handler.post(() -> {
                    btnGuardarLibro.setEnabled(true);
                    Toast.makeText(this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow(); // liberamos el executor al destruir la actividad
    }
}
