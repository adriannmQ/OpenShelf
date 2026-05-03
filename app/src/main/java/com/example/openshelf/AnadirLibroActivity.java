package com.example.openshelf;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AnadirLibroActivity extends AppCompatActivity {

    private MaterialToolbar    toolbar;
    private TextInputLayout    tilTitulo, tilAutor, tilIsbn;
    private TextInputEditText  etTitulo, etAutor, etIsbn;
    private AutoCompleteTextView actvGenero;
    private RadioGroup         rgEstado;
    private MaterialButton     btnGuardarLibro, btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_libro);

        toolbar        = findViewById(R.id.toolbar);
        tilTitulo      = findViewById(R.id.tilTitulo);
        tilAutor       = findViewById(R.id.tilAutor);
        tilIsbn        = findViewById(R.id.tilIsbn);
        etTitulo       = findViewById(R.id.etTitulo);
        etAutor        = findViewById(R.id.etAutor);
        etIsbn         = findViewById(R.id.etIsbn);
        actvGenero     = findViewById(R.id.actvGenero);
        rgEstado       = findViewById(R.id.rgEstado);
        btnGuardarLibro = findViewById(R.id.btnGuardarLibro);
        btnCancelar    = findViewById(R.id.btnCancelar);

        toolbar.setNavigationOnClickListener(v -> finish());

        String[] generos = {"Ciencia ficción", "Fantasía", "Histórica", "Misterio",
                "Realismo mágico", "Distopía", "Cuentos", "Clásico", "Romance", "Terror"};
        ArrayAdapter<String> adaptadorGenero = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, generos);
        actvGenero.setAdapter(adaptadorGenero);

        btnGuardarLibro.setOnClickListener(v -> guardarLibro());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarLibro() {
        String titulo = etTitulo.getText().toString().trim();
        String autor  = etAutor.getText().toString().trim();
        String isbn   = etIsbn.getText().toString().trim();

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

        Toast.makeText(this, "Libro \"" + titulo + "\" guardado correctamente",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
