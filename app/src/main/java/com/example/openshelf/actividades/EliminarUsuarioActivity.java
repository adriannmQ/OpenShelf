package com.example.openshelf.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.example.openshelf.Usuario;
import com.google.android.material.button.MaterialButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EliminarUsuarioActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteUsuarios;
    private LinearLayout         layoutDetalles;
    private TextView             tvNombreDetalle, tvEmailDetalle, tvRolDetalle;
    private MaterialButton       btnEliminar;
    private ProgressBar          pbCargando;

    private List<Usuario>  listaUsuarios = new ArrayList<>();
    private Usuario        usuarioSeleccionado;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_usuario);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Vistas
        autoCompleteUsuarios = findViewById(R.id.autoCompleteUsuarios);
        layoutDetalles       = findViewById(R.id.layoutDetallesUsuario);
        tvNombreDetalle      = findViewById(R.id.tvNombreDetalle);
        tvEmailDetalle       = findViewById(R.id.tvEmailDetalle);
        tvRolDetalle         = findViewById(R.id.tvRolDetalle);
        btnEliminar          = findViewById(R.id.btnEliminar);
        pbCargando           = findViewById(R.id.pbCargando);

        // Al seleccionar un usuario de la lista desplegable
        autoCompleteUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            usuarioSeleccionado = listaUsuarios.get(position);
            mostrarDetalles(usuarioSeleccionado);
        });

        // Botón Eliminar
        btnEliminar.setOnClickListener(v -> {
            if (usuarioSeleccionado != null) {
                mostrarDialogoConfirmacion();
            }
        });

        cargarUsuariosDesdeDB();
    }

    private void mostrarDetalles(Usuario u) {
        tvNombreDetalle.setText("Nombre: " + u.getNombre());
        tvEmailDetalle.setText("Email: " + u.getEmail());
        tvRolDetalle.setText("Rol: " + u.getRol());
        layoutDetalles.setVisibility(View.VISIBLE);
        btnEliminar.setEnabled(true);
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar a " + usuarioSeleccionado.getNombre() + "?\n\nEsta acción borrará permanentemente el registro de la base de datos.")
                .setPositiveButton("ELIMINAR", (dialog, which) -> ejecutarEliminacion())
                .setNegativeButton("CANCELAR", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void ejecutarEliminacion() {
        pbCargando.setVisibility(View.VISIBLE);
        btnEliminar.setEnabled(false);

        executor.execute(() -> {
            try (Connection conn = ConexionMySQL.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) {

                ps.setInt(1, usuarioSeleccionado.getId());
                int filasAfectadas = ps.executeUpdate();

                handler.post(() -> {
                    pbCargando.setVisibility(View.GONE);
                    if (filasAfectadas > 0) {
                        Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Volvemos atrás al terminar
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el usuario", Toast.LENGTH_SHORT).show();
                        btnEliminar.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                handler.post(() -> {
                    pbCargando.setVisibility(View.GONE);
                    btnEliminar.setEnabled(true);
                    Toast.makeText(this, "Error de base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void cargarUsuariosDesdeDB() {
        pbCargando.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            List<Usuario> usuarios = new ArrayList<>();
            List<String> nombres = new ArrayList<>();

            try (Connection conn = ConexionMySQL.obtenerConexion();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id_usuario, nombre, email, rol FROM usuarios ORDER BY nombre ASC")) {

                while (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("rol")
                    );
                    usuarios.add(u);
                    nombres.add(u.getNombre() + " (" + u.getEmail() + ")");
                }

                handler.post(() -> {
                    pbCargando.setVisibility(View.GONE);
                    listaUsuarios = usuarios;
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres);
                    autoCompleteUsuarios.setAdapter(adapter);
                });

            } catch (Exception e) {
                handler.post(() -> {
                    pbCargando.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
