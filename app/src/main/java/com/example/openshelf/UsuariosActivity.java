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

public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView          rvUsuarios;
    private SearchView            searchView;
    private FloatingActionButton  fabNuevoUsuario;
    private TextView              tvSinResultados;

    private UsuarioAdapter        adapter;
    private List<Usuario>         listaCompleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        rvUsuarios      = findViewById(R.id.rvUsuarios);
        searchView      = findViewById(R.id.searchView);
        fabNuevoUsuario = findViewById(R.id.fabNuevoUsuario);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        listaCompleta = obtenerUsuariosDePrueba();

        adapter = new UsuarioAdapter(new ArrayList<>(listaCompleta));
        adapter.setOnUsuarioClickListener((usuario, position) ->
            Toast.makeText(this, "Seleccionado: " + usuario.getNombre(), Toast.LENGTH_SHORT).show()
        );

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);

        fabNuevoUsuario.setOnClickListener(v ->
            Toast.makeText(this, "Próximamente: añadir usuario", Toast.LENGTH_SHORT).show()
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Usuario> filtrada;
                if (newText == null || newText.trim().isEmpty()) {
                    filtrada = listaCompleta;
                } else {
                    String texto = newText.toLowerCase();
                    filtrada = listaCompleta.stream()
                            .filter(u -> u.getNombre().toLowerCase().contains(texto)
                                      || u.getEmail().toLowerCase().contains(texto))
                            .collect(Collectors.toList());
                }
                adapter.actualizarLista(filtrada);
                tvSinResultados.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
                return true;
            }
        });
    }

    private List<Usuario> obtenerUsuariosDePrueba() {
        List<Usuario> lista = new ArrayList<>();
        lista.add(new Usuario(1, "Ana García",    "ana@email.com",    "admin"));
        lista.add(new Usuario(2, "Carlos López",  "carlos@email.com", "usuario"));
        lista.add(new Usuario(3, "María Pérez",   "maria@email.com",  "usuario"));
        lista.add(new Usuario(4, "Juan Martínez", "juan@email.com",   "usuario"));
        lista.add(new Usuario(5, "Laura Sánchez", "laura@email.com",  "admin"));
        return lista;
    }
}
