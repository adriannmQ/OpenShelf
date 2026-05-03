package com.example.openshelf;

public class Prestamo {

    private int    id;
    private String nombreUsuario;
    private String tituloLibro;
    private String fechaPrestamo;
    private String fechaDevolucion;
    private String estado;

    public Prestamo(int id, String nombreUsuario, String tituloLibro,
                    String fechaPrestamo, String fechaDevolucion, String estado) {
        this.id              = id;
        this.nombreUsuario   = nombreUsuario;
        this.tituloLibro     = tituloLibro;
        this.fechaPrestamo   = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado          = estado;
    }

    public int    getId()              { return id; }
    public String getNombreUsuario()   { return nombreUsuario; }
    public String getTituloLibro()     { return tituloLibro; }
    public String getFechaPrestamo()   { return fechaPrestamo; }
    public String getFechaDevolucion() { return fechaDevolucion; }
    public String getEstado()          { return estado; }

    public void setId(int id)                      { this.id = id; }
    public void setNombreUsuario(String n)         { this.nombreUsuario = n; }
    public void setTituloLibro(String t)           { this.tituloLibro = t; }
    public void setFechaPrestamo(String f)         { this.fechaPrestamo = f; }
    public void setFechaDevolucion(String f)       { this.fechaDevolucion = f; }
    public void setEstado(String e)                { this.estado = e; }
}
