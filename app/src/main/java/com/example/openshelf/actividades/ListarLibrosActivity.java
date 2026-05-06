package com.example.openshelf.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openshelf.Libro;
import com.example.openshelf.LibroAdapter;
import com.example.openshelf.R;
import com.example.openshelf.BaseDatos.ConexionMySQL;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// Esta clase muestra la lista de libros almacenados en la base de datos.
// Me encargo de tres cosas: cargar los libros desde MySQL en un hilo secundario,
// permitir buscarlos por título o autor, y navegar hacia atrás o hacia añadir libro.
public class ListarLibrosActivity extends AppCompatActivity {

    // Declaro las vistas que necesito en esta pantalla
    private MaterialToolbar      toolbar;
    private RecyclerView         rvLibros;
    private SearchView           searchView;
    private FloatingActionButton fabAnadirLibro;
    private TextView             tvSinResultados;

    // El adapter gestiona cómo se pinta cada libro en el RecyclerView
    private LibroAdapter  adapter;

    // Guardo la lista completa para poder restaurarla al borrar el filtro de búsqueda
    private List<Libro> listaCompleta = new ArrayList<>();

    // Uso un hilo secundario para la consulta a la base de datos,
    // igual que hago en el login, para no bloquear la interfaz
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_libros);

        // Enlazo todas las vistas con sus identificadores del layout
        toolbar         = findViewById(R.id.toolbar);
        rvLibros        = findViewById(R.id.rvLibros);
        searchView      = findViewById(R.id.searchView);
        fabAnadirLibro  = findViewById(R.id.fabAnadirLibro);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        configurarToolbar();
        configurarRecyclerView();
        configurarBuscador();
        configurarFab();

        // Lanzo la carga de libros desde la base de datos nada más abrir la pantalla
        cargarLibrosDesdeBD();
    }


    // Configuro la toolbar para que actúe como ActionBar y así el icono
    // de flecha hacia atrás que ya tiene definido en el xml funcione correctamente.
    private void configurarToolbar() {
        setSupportActionBar(toolbar);

        // Registro el listener del botón de navegación (la flecha de atrás).
        // Con finish() cierro esta activity y Android me devuelve automáticamente
        // a la pantalla anterior de la pila de actividades.
        toolbar.setNavigationOnClickListener(v -> finish());
    }


    // Preparo el RecyclerView con un layout lineal vertical y un adapter vacío.
    // El adapter se rellenará en cuanto lleguen los datos de la base de datos.
    private void configurarRecyclerView() {
        adapter = new LibroAdapter(this, new ArrayList<>());
        adapter.setOnLibroClickListener((libro, position) ->
                Toast.makeText(this,
                        "Seleccionado: " + libro.getTitulo(),
                        Toast.LENGTH_SHORT).show()
        );
        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvLibros.setAdapter(adapter);
    }


    // Configuro el buscador para que filtre la lista cada vez que el usuario escribe.
    // Trabajo siempre contra listaCompleta para no perder libros al borrar el texto.
    private void configurarBuscador() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Libro> filtrada;

                if (newText == null || newText.trim().isEmpty()) {
                    // Si el campo está vacío muestro todos los libros
                    filtrada = listaCompleta;
                } else {
                    String texto = newText.toLowerCase();
                    filtrada = listaCompleta.stream()
                            .filter(l -> l.getTitulo().toLowerCase().contains(texto)
                                    || l.getAutor().toLowerCase().contains(texto))
                            .collect(Collectors.toList());
                }

                adapter.actualizarLista(filtrada);

                // Muestro el mensaje de sin resultados solo si la lista filtrada está vacía
                tvSinResultados.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
                return true;
            }
        });
    }


    // Configuro el botón flotante para navegar hacia la pantalla de añadir libro
    private void configurarFab() {
        fabAnadirLibro.setOnClickListener(v ->
                startActivity(new Intent(this, AnadirLibroActivity.class))
        );
    }


    // Lanzo la consulta a MySQL en el hilo secundario.
    // Mientras se ejecuta, el usuario puede seguir interactuando con la interfaz
    // porque el hilo principal no está bloqueado.
    private void cargarLibrosDesdeBD() {
        executor.submit(() -> {
            // Construyo la lista aquí dentro, en el hilo secundario,
            // y al terminar la paso a la interfaz mediante runOnUiThread
            List<Libro> libros = new ArrayList<>();

            // Uso try-with-resources para cerrar automáticamente la conexión
            // y la sentencia al salir del bloque, haya error o no
            try (
                    Connection        conexion  = ConexionMySQL.obtenerConexion();
                    PreparedStatement sentencia = conexion.prepareStatement(
                            // Ajusta los nombres de columna según los que tengas
                            // definidos en tu tabla Libros de la base de datos
                            "SELECT id_libro, titulo, autor, isbn, genero, estado FROM Libros"
                    )
            ) {
                ResultSet resultado = sentencia.executeQuery();

                // Recorro cada fila del resultado y creo un objeto Libro por cada una
                while (resultado.next()) {
                    libros.add(new Libro(
                            resultado.getInt("id_libro"),
                            resultado.getString("titulo"),
                            resultado.getString("autor"),
                            resultado.getString("isbn"),
                            resultado.getString("genero"),
                            resultado.getString("estado")
                    ));
                }

                // La consulta fue bien: actualizo la interfaz con los libros obtenidos
                actualizarInterfaz(libros, null);

            } catch (Exception e) {
                // Si algo falla mando el mensaje de error para mostrarlo en un toast
                actualizarInterfaz(null, e.getMessage());
            }
        });
    }


    // Vuelvo al hilo principal para poder tocar las vistas de forma segura.
    // Recibo la lista de libros si todo fue bien, o el mensaje de error si algo falló.
    private void actualizarInterfaz(List<Libro> libros, String error) {
        runOnUiThread(() -> {
            if (error != null) {
                // Muestro el error y el mensaje de lista vacía para que la pantalla
                // no quede en blanco sin ninguna explicación
                Toast.makeText(this,
                        "Error al cargar libros: " + error,
                        Toast.LENGTH_LONG).show();
                tvSinResultados.setVisibility(View.VISIBLE);
                return;
            }

            if (libros == null || libros.isEmpty()) {
                // La consulta funcionó pero no hay libros en la base de datos todavía
                tvSinResultados.setVisibility(View.VISIBLE);
                return;
            }

            // Guardo la lista completa para que el buscador pueda filtrar sobre ella
            listaCompleta = libros;

            // Actualizo el adapter con los datos reales que vienen de la base de datos
            adapter.actualizarLista(listaCompleta);
            tvSinResultados.setVisibility(View.GONE);
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libero el ExecutorService al destruir la activity para no dejar
        // hilos corriendo innecesariamente en segundo plano
        executor.shutdown();
    }
}