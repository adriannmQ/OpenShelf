package com.example.openshelf.BaseDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase responsable de centralizar la conexión a la base de datos.
// La mantengo separada del resto de la lógica para que sea fácil
// cambiar los datos de conexión sin tocar otras clases.
public class ConexionMySQL {
    // Dirección del servidor donde tengo alojada la base de datos.
    // Si estoy probando en local con el emulador, uso 10.0.2.2
    // en lugar de localhost, porque el emulador trata localhost
    // como su propia máquina virtual, no como mi PC.
    private static final String URL =
            "jdbc:mysql://10.0.2.2:3306/openshelf" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true"+
                    "&serverTimezone=Europe/Madrid"  ;

    // Credenciales de acceso al servidor MySQL.
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "";

    // Devuelvo un objeto Connection listo para ejecutar consultas.
    // Lanzo la excepción hacia arriba para que quien me llame
    // decida cómo gestionarla (normalmente mostrando un Toast).
    public static Connection obtenerConexion() throws SQLException {
        try {
            // Cargo el driver JDBC de MySQL en memoria.
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No encuentro el driver JDBC: " + e.getMessage());
        }

        // Creo y devuelvo la conexión con los parámetros definidos arriba.
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

}
