package com.example.openshelf.actividades;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarcarComoDevueltoActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ProgressBar pbCargando;

    private TextInputLayout tilSeleccionarPrestamo;
    private AutoCompleteTextView autoCompletePrestamos;

    private LinearLayout layoutDetallesPrestamo;
    private TextView tvUsuarioDetalle, tvLibroDetalle, tvFechaDetalle;

    private MaterialButton btnConfirmarDevolucion;

    // Guardo los ids del prestamo y del libro seleccionado para usarlos al confirmar
    private int idPrestamoSeleccionado = -1;
    private int idLibroSeleccionado    = -1;

    // Uso una clase interna para representar cada prestamo activo en el desplegable,
    // de forma que puedo mostrar un texto legible pero conservar los ids internos
    private static class ItemPrestamo {
        int    idPrestamo;
        int    idLibro;
        String etiqueta;
        String usuario;
        String libro;
        String fechaPrestamo;

        ItemPrestamo(int idPrestamo, int idLibro, String etiqueta,
                     String usuario, String libro, String fechaPrestamo) {
            this.idPrestamo   = idPrestamo;
            this.idLibro      = idLibro;
            this.etiqueta     = etiqueta;
            this.usuario      = usuario;
            this.libro        = libro;
            this.fechaPrestamo = fechaPrestamo;
        }

        // El autocomplete usa este metodo para mostrar el texto en el desplegable
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    // Lista de prestamos activos cargada desde la base de datos al abrir la pantalla
    private final List<ItemPrestamo> listaPrestamos = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcar_como_devuelto);

        inicializarVistas();
        configurarToolbar();
        configurarBotones();
        cargarPrestamosActivos();
    }

    private void inicializarVistas() {
        toolbar                  = findViewById(R.id.toolbar);
        pbCargando               = findViewById(R.id.pbCargando);

        tilSeleccionarPrestamo   = findViewById(R.id.tilSeleccionarPrestamo);
        autoCompletePrestamos    = findViewById(R.id.autoCompletePrestamos);

        layoutDetallesPrestamo   = findViewById(R.id.layoutDetallesPrestamo);
        tvUsuarioDetalle         = findViewById(R.id.tvUsuarioDetalle);
        tvLibroDetalle           = findViewById(R.id.tvLibroDetalle);
        tvFechaDetalle           = findViewById(R.id.tvFechaDetalle);

        btnConfirmarDevolucion   = findViewById(R.id.btnConfirmarDevolucion);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarBotones() {
        // Cuando el usuario selecciona un prestamo del desplegable muestro sus detalles
        autoCompletePrestamos.setOnItemClickListener((parent, view, position, id) -> {
            ItemPrestamo seleccionado = listaPrestamos.get(position);

            idPrestamoSeleccionado = seleccionado.idPrestamo;
            idLibroSeleccionado    = seleccionado.idLibro;

            // Relleno la seccion de detalles con los datos del prestamo elegido
            tvUsuarioDetalle.setText("Usuario: " + seleccionado.usuario);
            tvLibroDetalle.setText("Libro: " + seleccionado.libro);
            tvFechaDetalle.setText("Fecha de prestamo: " + seleccionado.fechaPrestamo);

            layoutDetallesPrestamo.setVisibility(View.VISIBLE);
            btnConfirmarDevolucion.setEnabled(true);
        });

        btnConfirmarDevolucion.setOnClickListener(v -> confirmarDevolucion());
    }

    // Cargo todos los prestamos activos al abrir la pantalla para rellenar el desplegable
    private void cargarPrestamosActivos() {
        mostrarProgreso(true);

        executor.execute(() -> {
            List<ItemPrestamo> resultado = new ArrayList<>();

            try (Connection con = ConexionMySQL.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT p.id_prestamo, p.id_libro, p.fecha_prestamo, " +
                                 "u.nombre, l.titulo " +
                                 "FROM prestamos p " +
                                 "JOIN usuarios u ON p.id_usuario = u.id_usuario " +
                                 "JOIN libros l ON p.id_libro = l.id_libro " +
                                 "WHERE p.estado = 'activo' " +
                                 "ORDER BY p.fecha_prestamo DESC")) {

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int    idPrestamo    = rs.getInt("id_prestamo");
                    int    idLibro       = rs.getInt("id_libro");
                    String nombre        = rs.getString("nombre");
                    String titulo        = rs.getString("titulo");
                    String fechaPrestamo = rs.getString("fecha_prestamo");

                    // La etiqueta es lo que ve el usuario en el desplegable
                    String etiqueta = nombre + " — " + titulo;

                    resultado.add(new ItemPrestamo(
                            idPrestamo, idLibro, etiqueta,
                            nombre, titulo, fechaPrestamo));
                }

            } catch (SQLException e) {
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> mostrarToast("Error al cargar prestamos: " + mensajeError));
            }

            runOnUiThread(() -> {
                mostrarProgreso(false);
                listaPrestamos.clear();
                listaPrestamos.addAll(resultado);

                if (listaPrestamos.isEmpty()) {
                    // Informo de que no hay prestamos pendientes de devolver
                    mostrarToast("No hay prestamos activos en este momento");
                    tilSeleccionarPrestamo.setError("No hay prestamos activos");
                } else {
                    // Conecto la lista al autocomplete para que muestre las opciones
                    ArrayAdapter<ItemPrestamo> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            listaPrestamos);
                    autoCompletePrestamos.setAdapter(adapter);
                    tilSeleccionarPrestamo.setError(null);
                }
            });
        });
    }

    // Marco el prestamo como devuelto y el libro como disponible en una sola transaccion
    private void confirmarDevolucion() {
        if (idPrestamoSeleccionado == -1 || idLibroSeleccionado == -1) {
            mostrarToast("Selecciona un prestamo primero");
            return;
        }

        mostrarProgreso(true);
        btnConfirmarDevolucion.setEnabled(false);

        executor.execute(() -> {
            boolean exito = false;

            try (Connection con = ConexionMySQL.obtenerConexion()) {
                con.setAutoCommit(false);

                try {
                    // Primera operacion: actualizo el estado del prestamo a devuelto
                    PreparedStatement psUpdate = con.prepareStatement(
                            "UPDATE prestamos SET estado = 'devuelto' WHERE id_prestamo = ?");
                    psUpdate.setInt(1, idPrestamoSeleccionado);
                    psUpdate.executeUpdate();
                    psUpdate.close();

                    // Segunda operacion: vuelvo a dejar el libro disponible en el catalogo
                    PreparedStatement psLibro = con.prepareStatement(
                            "UPDATE libros SET estado = 'disponible' WHERE id_libro = ?");
                    psLibro.setInt(1, idLibroSeleccionado);
                    psLibro.executeUpdate();
                    psLibro.close();

                    con.commit();
                    exito = true;

                } catch (SQLException e) {
                    // Revierto todo si alguna de las dos operaciones falla
                    con.rollback();
                    final String mensajeError = e.getMessage();
                    runOnUiThread(() -> mostrarToast("Error al registrar la devolucion: " + mensajeError));
                }

            } catch (SQLException e) {
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> mostrarToast("Error de conexion: " + mensajeError));
            }

            final boolean resultado = exito;

            runOnUiThread(() -> {
                mostrarProgreso(false);
                btnConfirmarDevolucion.setEnabled(true);

                if (resultado) {
                    mostrarToast("Devolucion registrada correctamente");
                    finish();
                }
            });
        });
    }

    private void mostrarProgreso(boolean mostrar) {
        pbCargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}