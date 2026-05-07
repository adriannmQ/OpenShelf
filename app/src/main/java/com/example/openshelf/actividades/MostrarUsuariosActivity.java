package com.example.openshelf.actividades;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MostrarUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private SearchView   searchView;
    private TextView     tvSinResultados;

    // FAB principal y opciones del menú secundario
    private FloatingActionButton         fabMenu;
    private ExtendedFloatingActionButton fabCrear;
    private ExtendedFloatingActionButton fabModificar;
    private ExtendedFloatingActionButton fabEliminar;
    private View                         dimOverlay;

    private boolean menuAbierto = false;

    private UsuarioAdapter adapter;
    private List<Usuario>  listaCompleta = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_usuarios);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Vistas
        rvUsuarios      = findViewById(R.id.rvUsuarios);
        searchView      = findViewById(R.id.searchView);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        fabMenu         = findViewById(R.id.fabMenu);
        fabCrear        = findViewById(R.id.fabCrear);
        fabModificar    = findViewById(R.id.fabModificar);
        fabEliminar     = findViewById(R.id.fabEliminar);
        dimOverlay      = findViewById(R.id.dimOverlay);

        // RecyclerView
        adapter = new UsuarioAdapter(new ArrayList<>());
        adapter.setOnUsuarioClickListener((usuario, position) -> {
            Intent intent = new Intent(this, CambiarContrasenaActivity.class);
            intent.putExtra("id_usuario",  usuario.getId());
            intent.putExtra("nombre",      usuario.getNombre());
            intent.putExtra("email",       usuario.getEmail());
            intent.putExtra("rol",         usuario.getRol());
            startActivity(intent);
        });

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);

        // ── FAB menú
        fabMenu.setOnClickListener(v -> toggleMenu());
        dimOverlay.setOnClickListener(v -> cerrarMenu());

        fabCrear.setOnClickListener(v -> {
            cerrarMenu();
            startActivity(new Intent(this, CrearUsuarioActivity.class));
        });

        fabModificar.setOnClickListener(v -> {
            cerrarMenu();
            Toast.makeText(this, "Selecciona un usuario de la lista para modificarlo", Toast.LENGTH_SHORT).show();
        });

        fabEliminar.setOnClickListener(v -> {
            cerrarMenu();
            startActivity(new Intent(this, EliminarUsuarioActivity.class));
        });

        // ── SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }

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

        cargarUsuariosDesdeDB();
    }

    private void toggleMenu() {
        if (menuAbierto) cerrarMenu();
        else abrirMenu();
    }

    private void abrirMenu() {
        menuAbierto = true;
        dimOverlay.setVisibility(View.VISIBLE);
        dimOverlay.animate().alpha(1f).setDuration(200).start();

        fabCrear.setVisibility(View.VISIBLE);
        fabModificar.setVisibility(View.VISIBLE);
        fabEliminar.setVisibility(View.VISIBLE);

        fabCrear.animate().translationY(-56f * 3).alpha(1f).setDuration(200).start();
        fabModificar.animate().translationY(-56f * 2).alpha(1f).setDuration(200).start();
        fabEliminar.animate().translationY(-56f * 1).alpha(1f).setDuration(200).start();

        fabMenu.setImageResource(R.drawable.ic_close);
    }

    private void cerrarMenu() {
        menuAbierto = false;
        dimOverlay.animate().alpha(0f).setDuration(150)
                .withEndAction(() -> dimOverlay.setVisibility(View.GONE)).start();

        fabCrear.animate().translationY(0f).alpha(0f).setDuration(150)
                .withEndAction(() -> fabCrear.setVisibility(View.GONE)).start();
        fabModificar.animate().translationY(0f).alpha(0f).setDuration(150)
                .withEndAction(() -> fabModificar.setVisibility(View.GONE)).start();
        fabEliminar.animate().translationY(0f).alpha(0f).setDuration(150)
                .withEndAction(() -> fabEliminar.setVisibility(View.GONE)).start();

        fabMenu.setImageResource(R.drawable.ic_add_circle);
    }

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

                handler.post(() -> {
                    listaCompleta = usuarios;
                    adapter.actualizarLista(new ArrayList<>(listaCompleta));
                    tvSinResultados.setVisibility(listaCompleta.isEmpty() ? View.VISIBLE : View.GONE);
                });

            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuariosDesdeDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
