package com.example.openshelf;

public class Usuario {

    private int    id;
    private String nombre;
    private String email;
    private String rol;

    public Usuario(int id, String nombre, String email, String rol) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
        this.rol    = rol;
    }

    public int    getId()     { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }
    public String getRol()    { return rol; }

    public void setId(int id)         { this.id = id; }
    public void setNombre(String n)   { this.nombre = n; }
    public void setEmail(String e)    { this.email = e; }
    public void setRol(String r)      { this.rol = r; }
}
