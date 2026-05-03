package com.example.openshelf;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrestamosActivity extends AppCompatActivity {

    private RecyclerView         rvPrestamos;
    private SearchView           searchView;
    private FloatingActionButton fabNuevoPrestamo;
    private TextView             tvSinResultados;

    private PrestamoAdapter      adapter;
    private List<Prestamo>       listaCompleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamos);

        rvPrestamos      = findViewById(R.id.rvPrestamos);
        searchView       = findViewById(R.id.searchView);
        fabNuevoPrestamo = findViewById(R.id.fabNuevoPrestamo);
        tvSinResultados  = findViewById(R.id.tvSinResultados);

        listaCompleta = obtenerPrestamosDePrueba();

        adapter = new PrestamoAdapter(new ArrayList<>(listaCompleta));
        adapter.setOnPrestamoClickListener((prestamo, position) ->
            Toast.makeText(this, "Préstamo seleccionado: #" + prestamo.getId(), Toast.LENGTH_SHORT).show()
        );

        rvPrestamos.setLayoutManager(new LinearLayoutManager(this));
        rvPrestamos.setAdapter(adapter);

        fabNuevoPrestamo.setOnClickListener(v ->
            Toast.makeText(this, "Próximamente: nuevo préstamo", Toast.LENGTH_SHORT).show()
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Prestamo> filtrada;
                if (newText == null || newText.trim().isEmpty()) {
                    filtrada = listaCompleta;
                } else {
                    String texto = newText.toLowerCase();
                    filtrada = listaCompleta.stream()
                            .filter(p -> p.getNombreUsuario().toLowerCase().contains(texto)
                                      || p.getTituloLibro().toLowerCase().contains(texto))
                            .collect(Collectors.toList());
                }
                adapter.actualizarLista(filtrada);
                tvSinResultados.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
                return true;
            }
        });
    }

    private List<Prestamo> obtenerPrestamosDePrueba() {
        List<Prestamo> lista = new ArrayList<>();
        lista.add(new Prestamo(1, "Carlos López",  "1984",                    "2024-01-10", null,         "activo"));
        lista.add(new Prestamo(2, "María Pérez",   "Don Quijote",             "2024-01-08", "2024-01-20", "devuelto"));
        lista.add(new Prestamo(3, "Juan Martínez", "El señor de los anillos", "2024-01-15", null,         "activo"));
        lista.add(new Prestamo(4, "Laura Sánchez", "Dune",                    "2024-01-05", "2024-01-18", "devuelto"));
        lista.add(new Prestamo(5, "Carlos López",  "Ficciones",               "2024-01-20", null,         "activo"));
        return lista;
    }
}
