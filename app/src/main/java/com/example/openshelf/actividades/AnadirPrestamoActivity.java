package com.example.openshelf.actividades;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.example.openshelf.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnadirPrestamoActivity extends AppCompatActivity {

    // Referencias a las vistas del layout
    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressBar;

    private TextInputLayout tilEmail, tilIsbn, tilFechaPrestamo, tilFechaDevolucion;
    private TextInputEditText etEmail, etIsbn, etFechaPrestamo, etFechaDevolucion;

    private TextView tvNombreUsuario, tvTituloLibro;

    private MaterialButton btnConfirmar, btnCancelar;

    // Guardo el id del usuario y del libro una vez verificados,
    // para no tener que volver a consultarlos al confirmar el prestamo
    private int idUsuarioVerificado = -1;
    private int idLibroVerificado   = -1;

    // Uso un executor para lanzar las operaciones de red y base de datos
    // en un hilo secundario, evitando bloquear el hilo principal de la ui
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Formato de fecha que usare tanto para mostrar como para guardar en la base de datos
    private final SimpleDateFormat formatoFecha =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_prestamo);

        inicializarVistas();
        configurarToolbar();
        rellenarFechaHoy();
        configurarListenerEmail();
        configurarListenerIsbn();
        configurarSelectorFechaDevolucion();
        configurarBotones();
    }

    // Enlazo cada variable con su vista correspondiente del layout
    private void inicializarVistas() {
        toolbar            = findViewById(R.id.toolbar);
        progressBar        = findViewById(R.id.progressBar);

        tilEmail           = findViewById(R.id.tilEmail);
        tilIsbn            = findViewById(R.id.tilIsbn);
        tilFechaPrestamo   = findViewById(R.id.tilFechaPrestamo);
        tilFechaDevolucion = findViewById(R.id.tilFechaDevolucion);

        etEmail            = findViewById(R.id.etEmail);
        etIsbn             = findViewById(R.id.etIsbn);
        etFechaPrestamo    = findViewById(R.id.etFechaPrestamo);
        etFechaDevolucion  = findViewById(R.id.etFechaDevolucion);

        tvNombreUsuario    = findViewById(R.id.tvNombreUsuario);
        tvTituloLibro      = findViewById(R.id.tvTituloLibro);

        btnConfirmar       = findViewById(R.id.btnConfirmar);
        btnCancelar        = findViewById(R.id.btnCancelar);
    }

    // Configuro la flecha de retroceso de la toolbar para que cierre la actividad
    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // Relleno el campo de fecha de prestamo con la fecha actual al abrir la pantalla,
    // ya que el prestamo siempre se realiza en el dia de hoy
    private void rellenarFechaHoy() {
        String hoy = formatoFecha.format(new Date());
        etFechaPrestamo.setText(hoy);
    }

    // Escucho cuando el usuario termina de escribir el email para verificarlo
    // automaticamente sin necesidad de pulsar ningun boton
    private void configurarListenerEmail() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();

                // Solo consulto la base de datos si el email tiene un formato minimo valido
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    buscarUsuarioPorEmail(email);
                } else {
                    // Si el email no es valido limpio los datos del usuario anterior
                    tvNombreUsuario.setVisibility(View.GONE);
                    idUsuarioVerificado = -1;
                    tilEmail.setError(null);
                }
            }
        });
    }

    // Busco en la base de datos el usuario cuyo email coincide con el introducido
    private void buscarUsuarioPorEmail(String email) {
        mostrarProgreso(true);

        executor.execute(() -> {
            String nombreEncontrado = null;
            int    idEncontrado     = -1;

            try (Connection con = ConexionMySQL.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT id_usuario, nombre FROM usuarios WHERE email = ? LIMIT 1")) {

                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    idEncontrado     = rs.getInt("id_usuario");
                    nombreEncontrado = rs.getString("nombre");
                }

            } catch (SQLException e) {
                // Guardo el mensaje para mostrarlo en el hilo principal
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> mostrarToast("Error al buscar usuario: " + mensajeError));
            }

            // Capturo las variables finales para usarlas dentro del runOnUiThread
            final String nombre = nombreEncontrado;
            final int    id     = idEncontrado;

            runOnUiThread(() -> {
                mostrarProgreso(false);

                if (nombre != null) {
                    // Muestro el nombre del usuario verificado debajo del campo de email
                    idUsuarioVerificado = id;
                    tvNombreUsuario.setText("Usuario encontrado: " + nombre);
                    tvNombreUsuario.setVisibility(View.VISIBLE);
                    tilEmail.setError(null);
                } else {
                    // Informo al usuario de que no existe ninguna cuenta con ese email
                    idUsuarioVerificado = -1;
                    tvNombreUsuario.setVisibility(View.GONE);
                    tilEmail.setError("No existe un usuario con ese correo");
                }
            });
        });
    }

    // Escucho los cambios en el campo isbn para verificar el libro automaticamente
    private void configurarListenerIsbn() {
        etIsbn.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String isbn = s.toString().trim();

                // Compruebo que el isbn tiene al menos 10 caracteres antes de consultar
                if (isbn.length() >= 10) {
                    buscarLibroPorIsbn(isbn);
                } else {
                    tvTituloLibro.setVisibility(View.GONE);
                    idLibroVerificado = -1;
                    tilIsbn.setError(null);
                }
            }
        });
    }

    // Consulto la base de datos para obtener el titulo y disponibilidad del libro
    private void buscarLibroPorIsbn(String isbn) {
        mostrarProgreso(true);

        executor.execute(() -> {
            String tituloEncontrado = null;
            int    idEncontrado     = -1;
            boolean disponible      = false;

            try (Connection con = ConexionMySQL.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT id_libro, titulo, estado FROM libros WHERE isbn = ? LIMIT 1")) {

                ps.setString(1, isbn);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    idEncontrado     = rs.getInt("id_libro");
                    tituloEncontrado = rs.getString("titulo");
                    disponible = rs.getString("estado").equals("disponible");
                }

            } catch (SQLException e) {
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> mostrarToast("Error al buscar libro: " + mensajeError));
            }

            final String  titulo      = tituloEncontrado;
            final int     id          = idEncontrado;
            final boolean estaDisp    = disponible;

            runOnUiThread(() -> {
                mostrarProgreso(false);

                if (titulo != null && estaDisp) {
                    // El libro existe y esta disponible para prestarse
                    idLibroVerificado = id;
                    tvTituloLibro.setText("Libro encontrado: " + titulo);
                    tvTituloLibro.setVisibility(View.VISIBLE);
                    tilIsbn.setError(null);
                } else if (titulo != null) {
                    // El libro existe pero ya esta prestado a otro usuario
                    idLibroVerificado = -1;
                    tvTituloLibro.setVisibility(View.GONE);
                    tilIsbn.setError("Este libro esta prestado actualmente");
                } else {
                    // No hay ningun libro con ese isbn en el catalogo
                    idLibroVerificado = -1;
                    tvTituloLibro.setVisibility(View.GONE);
                    tilIsbn.setError("No existe un libro con ese isbn");
                }
            });
        });
    }

    // Abro un dialogo de seleccion de fecha cuando el usuario pulsa el campo de devolucion
    private void configurarSelectorFechaDevolucion() {
        View.OnClickListener abrirCalendario = v -> {
            Calendar calendario = Calendar.getInstance();

            // Establezco la fecha minima como manana para que no pueda elegir hoy o antes
            calendario.add(Calendar.DAY_OF_MONTH, 1);
            int ano = calendario.get(Calendar.YEAR);
            int mes  = calendario.get(Calendar.MONTH);
            int dia  = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        // Formateo la fecha seleccionada y la muestro en el campo
                        Calendar seleccionado = Calendar.getInstance();
                        seleccionado.set(y, m, d);
                        etFechaDevolucion.setText(formatoFecha.format(seleccionado.getTime()));
                    },
                    ano, mes, dia
            );

            // Impido que el usuario elija una fecha anterior a manana
            dialog.getDatePicker().setMinDate(calendario.getTimeInMillis());
            dialog.show();
        };

        etFechaDevolucion.setOnClickListener(abrirCalendario);
        tilFechaDevolucion.setEndIconOnClickListener(abrirCalendario);
    }

    // Configuro las acciones de los dos botones del formulario
    private void configurarBotones() {
        btnConfirmar.setOnClickListener(v -> validarYConfirmar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    // Valido que todos los campos esten correctamente rellenos antes de insertar el prestamo
    private void validarYConfirmar() {
        String fechaDevolucion = obtenerTexto(etFechaDevolucion);

        if (idUsuarioVerificado == -1) {
            tilEmail.setError("Introduce un correo valido y verificado");
            return;
        }

        if (idLibroVerificado == -1) {
            tilIsbn.setError("Introduce un isbn valido y con disponibilidad");
            return;
        }

        if (fechaDevolucion.isEmpty()) {
            tilFechaDevolucion.setError("Selecciona una fecha de devolucion");
            return;
        }

        // Antes de insertar compruebo en la base de datos si el usuario
        // ya tiene un prestamo activo, para evitar que acumule varios libros
        mostrarProgreso(true);
        btnConfirmar.setEnabled(false);

        String fechaPrestamo = obtenerTexto(etFechaPrestamo);

        executor.execute(() -> {
            boolean tienePrestamoActivo = false;

            try (Connection con = ConexionMySQL.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT COUNT(*) FROM prestamos WHERE id_usuario = ? AND estado = 'activo'")) {

                ps.setInt(1, idUsuarioVerificado);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    tienePrestamoActivo = rs.getInt(1) > 0;
                }

            } catch (SQLException e) {
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> {
                    mostrarProgreso(false);
                    btnConfirmar.setEnabled(true);
                    mostrarToast("Error al verificar prestamos del usuario: " + mensajeError);
                });
                return;
            }

            final boolean bloqueado = tienePrestamoActivo;

            runOnUiThread(() -> {
                if (bloqueado) {
                    // Informo al bibliotecario de que ese usuario ya tiene un libro en prestamo
                    mostrarProgreso(false);
                    btnConfirmar.setEnabled(true);
                    tilEmail.setError("Este usuario ya tiene un prestamo activo");
                } else {
                    // El usuario no tiene libros pendientes, procedo con el prestamo
                    insertarPrestamo(idUsuarioVerificado, idLibroVerificado, fechaPrestamo, fechaDevolucion);
                }
            });
        });
    }

    // Inserto el prestamo en la base de datos y marco el libro como no disponible
    private void insertarPrestamo(int idUsuario, int idLibro, String fechaPrestamo, String fechaDevolucion) {

        executor.execute(() -> {
            boolean exito = false;

            try (Connection con = ConexionMySQL.obtenerConexion()) {
                // Desactivo el autocommit para tratar las dos operaciones como una sola transaccion
                con.setAutoCommit(false);

                try {
                    // Primera operacion: inserto el registro del prestamo
                    PreparedStatement psInsert = con.prepareStatement(
                            "INSERT INTO prestamos (id_usuario, id_libro, fecha_prestamo, fecha_devolucion) " +
                                    "VALUES (?, ?, ?, ?)");
                    psInsert.setInt(1, idUsuario);
                    psInsert.setInt(2, idLibro);
                    psInsert.setString(3, fechaPrestamo);
                    psInsert.setString(4, fechaDevolucion);
                    psInsert.executeUpdate();
                    psInsert.close();

                    // Segunda operacion: marco el libro como no disponible para evitar prestamos duplicados
                    PreparedStatement psUpdate = con.prepareStatement(
                            "UPDATE libros SET estado = 'prestado' WHERE id_libro = ?");
                    psUpdate.setInt(1, idLibro);
                    psUpdate.executeUpdate();
                    psUpdate.close();

                    // Solo confirmo los cambios si las dos operaciones han salido bien
                    con.commit();
                    exito = true;

                } catch (SQLException e) {
                    // Si algo falla revierto los cambios para no dejar la base de datos en un estado inconsistente
                    con.rollback();
                    final String mensajeError = e.getMessage();
                    runOnUiThread(() -> mostrarToast("Error al guardar el prestamo: " + mensajeError));
                }

            } catch (SQLException e) {
                final String mensajeError = e.getMessage();
                runOnUiThread(() -> mostrarToast("Error de conexion: " + mensajeError));
            }

            final boolean resultado = exito;

            runOnUiThread(() -> {
                mostrarProgreso(false);
                btnConfirmar.setEnabled(true);

                if (resultado) {
                    mostrarToast("Prestamo registrado correctamente");
                    finish();
                }
            });
        });
    }

    // Controlo la visibilidad de la barra de progreso segun si hay una operacion en curso
    private void mostrarProgreso(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    // Muestro un mensaje corto al usuario de forma centralizada
    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // Extraigo el texto de un campo de texto eliminando espacios al principio y al final
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libero el executor al destruir la actividad para evitar fugas de memoria
        executor.shutdown();
    }
}