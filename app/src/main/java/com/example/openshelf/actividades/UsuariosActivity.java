package com.example.openshelf.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.example.openshelf.Usuario;
import com.example.openshelf.UsuarioAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView         rvUsuarios;
    private SearchView           searchView;
    private FloatingActionButton fabNuevoUsuario;
    private TextView             tvSinResultados;

    private UsuarioAdapter adapter;
    private List<Usuario>  listaCompleta = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        rvUsuarios      = findViewById(R.id.rvUsuarios);
        searchView      = findViewById(R.id.searchView);
        fabNuevoUsuario = findViewById(R.id.fabNuevoUsuario);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        // Arrancamos el adapter vacío; se llenará cuando lleguen los datos
        adapter = new UsuarioAdapter(new ArrayList<>());
        adapter.setOnUsuarioClickListener((usuario, position) ->
                Toast.makeText(this, "Seleccionado: " + usuario.getNombre(), Toast.LENGTH_SHORT).show()
        );

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);

        fabNuevoUsuario.setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: añadir usuario", Toast.LENGTH_SHORT).show()
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

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

        // Cargamos los usuarios desde la base de datos
        cargarUsuariosDesdeDB();
    }

    // ── Consulta en hilo secundario
    private void cargarUsuariosDesdeDB() {
        executor.execute(() -> {
            List<Usuario> usuarios = new ArrayList<>();

            try (Connection conn = ConexionMySQL.obtenerConexion();
                 Statement  stmt = conn.createStatement();
                 ResultSet  rs   = stmt.executeQuery("SELECT id_usuario, nombre, email, rol FROM usuarios")) {

                while (rs.next()) {
                    usuarios.add(new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("rol")
                    ));
                }

                // Volvemos al hilo principal para actualizar la UI
                handler.post(() -> {
                    listaCompleta = usuarios;
                    adapter.actualizarLista(new ArrayList<>(listaCompleta));
                    tvSinResultados.setVisibility(listaCompleta.isEmpty() ? View.VISIBLE : View.GONE);
                });

            } catch (Exception e) {
                handler.post(() ->
                        Toast.makeText(this,
                                "Error al cargar usuarios: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}