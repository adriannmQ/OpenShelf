package com.example.openshelf;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Esta clase representa la pantalla de inicio de sesión de OpenShelf.
// Me encargo de tres cosas principales: mostrar los campos de usuario y contraseña,
// validar que no estén vacíos antes de continuar, y navegar al menú principal
// si las credenciales introducidas son correctas.
public class LoginActivity extends AppCompatActivity {

    // Declaro las vistas que voy a necesitar en esta pantalla
    private EditText etEmail;       // campo de texto donde el usuario escribe su correo
    private EditText etContrasena;  // campo de texto donde el usuario escribe su contraseña
    private Button   btnLogin;      // botón que el usuario pulsa para intentar entrar

    // Defino las credenciales de prueba de forma fija.
    // En una aplicación real estas credenciales vendrían de una base de datos o de una api,
    // pero para el prototipo me basta con compararlas directamente aquí.
    private static final String USUARIO_VALIDO    = "admin";
    private static final String CONTRASENA_VALIDA = "1234";


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
    // y decido si puedo dejarle pasar o debo mostrarle un mensaje de error.
    private void intentarLogin() {

        // Uso trim() para eliminar espacios en blanco al inicio y al final,
        // así evito que un espacio accidental cuente como texto válido
        String usuario   = etEmail.getText().toString().trim();
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

        // Si ambos campos tienen contenido, comparo con las credenciales válidas
        if (usuario.equals(USUARIO_VALIDO) && contrasena.equals(CONTRASENA_VALIDA)) {

            // Muestro un toast de bienvenida, que es un mensaje breve que aparece
            // flotando en pantalla y desaparece solo al cabo de unos segundos
            Toast.makeText(this, "Bienvenido, " + usuario, Toast.LENGTH_SHORT).show();

            // Si las credenciales son correctas, navego al menú principal
            navegarAlMenuPrincipal(usuario);

        } else {
            // Si las credenciales no coinciden, aviso al usuario sin revelar
            // cuál de los dos campos es el incorrecto
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
        }
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
}
