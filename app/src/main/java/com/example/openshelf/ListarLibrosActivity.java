package com.example.openshelf;

import android.content.Intent;
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

public class ListarLibrosActivity extends AppCompatActivity {

    private RecyclerView         rvLibros;
    private SearchView           searchView;
    private FloatingActionButton fabAnadirLibro;
    private TextView             tvSinResultados;

    private LibroAdapter         adapter;
    private List<Libro>          listaCompleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_libros);

        rvLibros        = findViewById(R.id.rvLibros);
        searchView      = findViewById(R.id.searchView);
        fabAnadirLibro  = findViewById(R.id.fabAnadirLibro);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        listaCompleta = obtenerLibrosDePrueba();

        adapter = new LibroAdapter(this, new ArrayList<>(listaCompleta));
        adapter.setOnLibroClickListener((libro, position) ->
            Toast.makeText(this, "Seleccionado: " + libro.getTitulo(), Toast.LENGTH_SHORT).show()
        );

        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvLibros.setAdapter(adapter);

        fabAnadirLibro.setOnClickListener(v ->
            startActivity(new Intent(this, AnadirLibroActivity.class))
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Libro> filtrada;
                if (newText == null || newText.trim().isEmpty()) {
                    filtrada = listaCompleta;
                } else {
                    String texto = newText.toLowerCase();
                    filtrada = listaCompleta.stream()
                            .filter(l -> l.getTitulo().toLowerCase().contains(texto)
                                      || l.getAutor().toLowerCase().contains(texto))
                            .collect(Collectors.toList());
                }
                adapter.actualizarLista(filtrada);
                tvSinResultados.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
                return true;
            }
        });
    }

    private List<Libro> obtenerLibrosDePrueba() {
        List<Libro> lista = new ArrayList<>();
        lista.add(new Libro(1, "Cien años de soledad",    "Gabriel García Márquez", "9780307474728", "Realismo mágico", "disponible"));
        lista.add(new Libro(2, "1984",                    "George Orwell",           "9780451524935", "Distopía",        "prestado"));
        lista.add(new Libro(3, "El nombre de la rosa",    "Umberto Eco",             "9780156001311", "Histórica",       "disponible"));
        lista.add(new Libro(4, "Ficciones",               "Jorge Luis Borges",       "9780802130211", "Cuentos",         "disponible"));
        lista.add(new Libro(5, "Don Quijote",             "Miguel de Cervantes",     "9788467032819", "Clásico",         "prestado"));
        lista.add(new Libro(6, "La sombra del viento",    "Carlos Ruiz Zafón",       "9788408163435", "Misterio",        "disponible"));
        lista.add(new Libro(7, "Dune",                    "Frank Herbert",           "9780441013593", "Ciencia ficción", "disponible"));
        lista.add(new Libro(8, "El señor de los anillos", "J.R.R. Tolkien",          "9780618640157", "Fantasía",        "prestado"));
        return lista;
    }
}
