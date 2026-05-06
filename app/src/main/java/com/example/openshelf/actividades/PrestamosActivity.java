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
import com.example.openshelf.Prestamo;
import com.example.openshelf.PrestamoAdapter;
import com.example.openshelf.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PrestamosActivity extends AppCompatActivity {

    private RecyclerView         rvPrestamos;
    private SearchView           searchView;
    private FloatingActionButton fabNuevoPrestamo;
    private TextView             tvSinResultados;

    private PrestamoAdapter adapter;
    private List<Prestamo>  listaCompleta = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamos);

        rvPrestamos      = findViewById(R.id.rvPrestamos);
        searchView       = findViewById(R.id.searchView);
        fabNuevoPrestamo = findViewById(R.id.fabNuevoPrestamo);
        tvSinResultados  = findViewById(R.id.tvSinResultados);

        // Adapter vacío; se llenará al llegar los datos de la BD
        adapter = new PrestamoAdapter(new ArrayList<>());
        adapter.setOnPrestamoClickListener((prestamo, position) ->
                Toast.makeText(this, "Préstamo seleccionado: #" + prestamo.getId(), Toast.LENGTH_SHORT).show()
        );

        rvPrestamos.setLayoutManager(new LinearLayoutManager(this));
        rvPrestamos.setAdapter(adapter);

        fabNuevoPrestamo.setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: nuevo préstamo", Toast.LENGTH_SHORT).show()
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

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

        cargarPrestamosDesdeDB();
    }

    // ── Consulta en hilo secundario ───────────────────────────────────────────
    private void cargarPrestamosDesdeDB() {
        executor.execute(() -> {
            List<Prestamo> prestamos = new ArrayList<>();

            try (Connection conn = ConexionMySQL.obtenerConexion();
                 Statement  stmt = conn.createStatement();
                 ResultSet  rs   = stmt.executeQuery(
                         "SELECT p.id_prestamo, " +
                                 "       u.nombre        AS nombre_usuario, " +
                                 "       l.titulo        AS titulo_libro, " +
                                 "       p.fecha_prestamo, " +
                                 "       p.fecha_devolucion, " +
                                 "       p.estado " +
                                 "FROM   prestamos p " +
                                 "JOIN   usuarios  u ON u.id_usuario = p.id_usuario " +
                                 "JOIN   libros    l ON l.id_libro = p.id_libro " +
                                 "ORDER  BY p.id_prestamo DESC")) {

                while (rs.next()) {
                    prestamos.add(new Prestamo(
                            rs.getInt("id_prestamo"),
                            rs.getString("nombre_usuario"),
                            rs.getString("titulo_libro"),
                            rs.getString("fecha_prestamo"),
                            rs.getString("fecha_devolucion"), // puede ser null
                            rs.getString("estado")
                    ));
                }

                handler.post(() -> {
                    listaCompleta = prestamos;
                    adapter.actualizarLista(new ArrayList<>(listaCompleta));
                    tvSinResultados.setVisibility(listaCompleta.isEmpty() ? View.VISIBLE : View.GONE);
                });

            } catch (Exception e) {
                handler.post(() ->
                        Toast.makeText(this,
                                "Error al cargar préstamos: " + e.getMessage(),
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