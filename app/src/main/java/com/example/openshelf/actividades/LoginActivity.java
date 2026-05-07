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

// Esta pantalla es el punto de entrada de la aplicación.
// Solo permito el acceso a usuarios con rol de administrador.
// Si las credenciales son correctas pero el rol es usuario, bloqueo el acceso
// y muestro un mensaje explicando por qué no puede entrar.
public class LoginActivity extends AppCompatActivity {

    // Vistas del formulario de login
    private EditText etEmail;
    private EditText etContrasena;
    private Button   btnLogin;

    // El ExecutorService me permite lanzar la consulta a la base de datos
    // en un hilo secundario para no bloquear la interfaz mientras trabaja.
    // Sin esto, Android lanzaría un NetworkOnMainThreadException.
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enlazo las vistas con sus ids del layout
        etEmail      = findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        btnLogin     = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarLogin();
            }
        });
    }


    // Valido que los campos no estén vacíos antes de ir a la base de datos.
    // Si algo falla muestro el error en el propio campo y no continúo.
    private void intentarLogin() {

        String usuario    = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(usuario)) {
            etEmail.setError("El correo no puede estar vacío");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("La contraseña no puede estar vacía");
            etContrasena.requestFocus();
            return;
        }

        // Deshabilito el botón para que el usuario no pulse dos veces
        // mientras la consulta está en curso
        btnLogin.setEnabled(false);

        // Lanzo la consulta en el hilo secundario
        executor.submit(() -> consultarBaseDeDatos(usuario, contrasena));
    }


    // Busco al usuario en la base de datos comprobando email, contraseña y rol.
    // Este método se ejecuta siempre en el hilo secundario, nunca en el principal.
    private void consultarBaseDeDatos(String usuario, String contrasena) {

        try (
                Connection        conexion  = ConexionMySQL.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        // Traigo el nombre y el rol para saber si el usuario puede entrar
                        "SELECT nombre, rol FROM usuarios " +
                                "WHERE email = ? AND contrasena = ?"
                )
        ) {
            sentencia.setString(1, usuario);
            sentencia.setString(2, contrasena);

            ResultSet resultado = sentencia.executeQuery();

            if (resultado.next()) {
                String nombre = resultado.getString("nombre");
                String rol    = resultado.getString("rol");

                // Las credenciales son correctas, pero solo dejo entrar si el rol es admin.
                // Uso equals() al revés para evitar un NullPointerException
                // si rol llegara como nulo desde la base de datos.
                if ("admin".equals(rol)) {
                    gestionarResultado(true, nombre);
                } else {
                    // El usuario existe pero no tiene permisos de administrador
                    gestionarResultado(false, "sin_permiso");
                }

            } else {
                // No encontré ninguna fila con esas credenciales
                gestionarResultado(false, null);
            }

        } catch (Exception e) {
            // Capturo cualquier error de conexión y lo paso a la interfaz
            gestionarResultado(false, e.getMessage());
        }
    }


    // Vuelvo al hilo principal con runOnUiThread para poder tocar las vistas.
    // Según el resultado muestro un mensaje distinto al usuario.
    private void gestionarResultado(boolean exito, String datos) {
        runOnUiThread(() -> {

            // Reactivo el botón siempre, haya ido bien o mal
            btnLogin.setEnabled(true);

            if (exito) {
                Toast.makeText(this, "Bienvenid@, " + datos, Toast.LENGTH_SHORT).show();
                navegarAlMenuPrincipal(datos);

            } else {
                String mensaje;

                if ("sin_permiso".equals(datos)) {
                    // Credenciales correctas pero sin rol de administrador
                    mensaje = "No tienes permiso para acceder a esta aplicación";
                } else if (datos != null) {
                    // Error técnico de conexión con la base de datos
                    mensaje = "Error de conexión: " + datos;
                } else {
                    // El email o la contraseña no coinciden con ningún usuario
                    mensaje = "Correo o contraseña incorrectos";
                }

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }


    // Navego al menú principal pasando el nombre del usuario como extra del intent
    private void navegarAlMenuPrincipal(String usuario) {
        Intent intent = new Intent(LoginActivity.this, MenuPrincipalActivity.class);
        intent.putExtra("USUARIO", usuario);
        startActivity(intent);
        // Cierro el login para que el usuario no pueda volver atrás con el botón de retroceso
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libero el hilo secundario al cerrar la pantalla
        executor.shutdown();
    }
}