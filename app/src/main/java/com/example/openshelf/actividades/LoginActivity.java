package com.example.openshelf.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openshelf.R;
import com.example.openshelf.BaseDatos.ConexionMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Esta clase representa la pantalla de inicio de sesión de OpenShelf.
// Me encargo de tres cosas principales: mostrar los campos de usuario y contraseña,
// validar que no estén vacíos antes de continuar, y navegar al menú principal
// si las credenciales introducidas son correctas según la base de datos.
public class LoginActivity extends AppCompatActivity {

    // Declaro las vistas que voy a necesitar en esta pantalla
    private EditText etEmail;       // campo de texto donde el usuario escribe su correo
    private EditText etContrasena;  // campo de texto donde el usuario escribe su contraseña
    private Button   btnLogin;      // botón que el usuario pulsa para intentar entrar

    // Creo un pool de un único hilo para las operaciones de red.
    // Esto me permite enviar la consulta a MySQL fuera del hilo principal
    // y evitar así el NetworkOnMainThreadException que bloquearía la app.
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    // onCreate es el primer método que Android ejecuta cuando crea esta activity.
    // Aquí establezco el layout visual y enlazo cada variable con su vista correspondiente.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Le indico a Android qué archivo xml quiero usar como interfaz de esta pantalla.
        // El nombre activity_login corresponde al archivo activity_login.xml.
        setContentView(R.layout.activity_login);

        // Busco cada vista en el layout usando su identificador único.
        // Estos IDs coinciden exactamente con los definidos en activity_login.xml.
        etEmail      = findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        btnLogin     = findViewById(R.id.btnLogin);

        // Registro un listener en el botón para que, cuando el usuario lo pulse,
        // se ejecute el método que intenta hacer login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarLogin();
            }
        });
    }


    // En este método recojo los valores que el usuario ha escrito, los valido
    // y decido si puedo lanzar la consulta a la base de datos o mostrar un error.
    private void intentarLogin() {

        // Uso trim() para eliminar espacios en blanco al inicio y al final,
        // así evito que un espacio accidental cuente como texto válido
        String usuario    = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // Primera validación: compruebo que el campo de usuario no esté vacío.
        // TextUtils.isEmpty devuelve verdadero si la cadena es nula o tiene longitud cero.
        if (TextUtils.isEmpty(usuario)) {
            // Con setError muestro un mensaje de error directamente dentro del campo,
            // lo cual resulta más claro para el usuario que un simple toast
            etEmail.setError("El usuario no puede estar vacío");
            etEmail.requestFocus(); // muevo el cursor a este campo para facilitar la corrección
            return; // salgo del método para no seguir ejecutando nada más
        }

        // Segunda validación: compruebo que el campo de contraseña tampoco esté vacío
        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("La contraseña no puede estar vacía");
            etContrasena.requestFocus();
            return;
        }

        // Deshabilito el botón mientras la consulta está en curso para evitar
        // que el usuario pulse varias veces y lance peticiones duplicadas.
        btnLogin.setEnabled(false);

        // Envío la tarea al hilo secundario. Todo lo que esté dentro de este
        // executor.submit() se ejecuta fuera del hilo principal de la interfaz.
        executor.submit(() -> consultarBaseDeDatos(usuario, contrasena));
    }


    // Realizo la consulta sql en el hilo secundario.
    // Nunca toco la interfaz directamente desde aquí; para eso uso runOnUiThread.
    private void consultarBaseDeDatos(String usuario, String contrasena) {

        // Uso try-with-resources para que Connection y PreparedStatement
        // se cierren solos aunque ocurra una excepción a mitad de la consulta.
        try (
                Connection        conexion  = ConexionMySQL.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        // Uso PreparedStatement en lugar de concatenar cadenas directamente,
                        // ya que esto me protege frente a inyecciones sql.
                        // Ajusta los nombres de columna según los que tengas en tu tabla Usuarios.
                        "SELECT nombre FROM usuarios " +
                                "WHERE email = ? AND contrasena = ? "
                )
        ) {
            // Sustituyo los signos de interrogación por los valores reales del formulario.
            sentencia.setString(1, usuario);
            sentencia.setString(2, contrasena);

            ResultSet resultado = sentencia.executeQuery();

            if (resultado.next()) {
                // La consulta devolvió una fila: el usuario existe y la contraseña es correcta.
                // Recojo el nombre para usarlo en el mensaje de bienvenida.
                String nombre = resultado.getString("nombre");
                gestionarResultado(true, nombre);
            } else {
                // No encontré ninguna fila: las credenciales no coinciden con ningún usuario.
                gestionarResultado(false, null);
            }

        } catch (Exception e) {
            // Si hay un error de conexión o de consulta, lo muestro en la interfaz.
            gestionarResultado(false, e.getMessage());
        }
    }


    // Vuelvo al hilo principal mediante runOnUiThread para poder tocar la interfaz
    // de forma segura con el resultado que llegó desde el hilo secundario.
    private void gestionarResultado(boolean exito, String datos) {
        runOnUiThread(() -> {

            // Vuelvo a habilitar el botón siempre, haya ido bien o mal.
            btnLogin.setEnabled(true);

            if (exito) {
                Toast.makeText(this, "Bienvenido, " + datos, Toast.LENGTH_SHORT).show();

                // Navego al menú principal pasándole el nombre del usuario,
                // exactamente igual que hacía antes con las credenciales fijas.
                navegarAlMenuPrincipal(datos);

            } else {
                // Si datos tiene contenido es un mensaje de error técnico;
                // si es nulo es simplemente que las credenciales no eran válidas.
                String mensaje = (datos != null)
                        ? "Error de conexión: " + datos
                        : "Correo o contraseña incorrectos";

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }


    // Este método construye un intent explícito hacia MenuPrincipalActivity y
    // le adjunta el nombre del usuario para que la siguiente pantalla pueda usarlo.
    // Un intent es el mecanismo que usa Android para comunicar y moverse entre activities.
    private void navegarAlMenuPrincipal(String usuario) {

        // Creo el intent indicando desde qué activity parto y a cuál quiero ir
        Intent intent = new Intent(LoginActivity.this, MenuPrincipalActivity.class);

        // Adjunto el nombre de usuario como un extra, que funciona como un dato
        // que viaja junto al intent usando un sistema de clave y valor
        intent.putExtra("USUARIO", usuario);

        // Lanzo la activity destino
        startActivity(intent);

        // Llamo a finish() para cerrar esta activity y que el usuario no pueda
        // volver a la pantalla de login pulsando el botón de retroceso del dispositivo
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libero los recursos del ExecutorService cuando la activity se destruye
        // para no dejar hilos corriendo en segundo plano sin control.
        executor.shutdown();
    }
}