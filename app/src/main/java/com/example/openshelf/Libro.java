package com.example.openshelf;

// Esta clase representa un libro dentro de la aplicación OpenShelf.
// Se trata de un objeto de modelo, es decir, su único propósito es
// almacenar los datos de un libro y dármelos cuando los necesito.
// En una arquitectura clásica esta clase corresponde a la capa de modelo.
public class Libro {

    // Defino los atributos que describen un libro.
    // Los declaro privados para protegerlos y solo accedo a ellos
    // a través de los métodos getter y setter que defino más abajo.
    private int    id;        // identificador único que corresponde a id_libro en la base de datos
    private String titulo;    // título del libro
    private String autor;     // nombre del autor o autores
    private String isbn;      // código ISBN único del libro, corresponde al campo isbn de la tabla
    private String genero;    // género literario al que pertenece
    private String estado;    // estado del libro: 'disponible' o 'prestado', según el ENUM de la BD


    // Con este constructor creo un objeto libro pasándole todos sus datos de golpe.
    // Lo uso, por ejemplo, cuando cargo los libros desde la base de datos y
    // necesito convertir cada fila en un objeto manejable desde Java.
    public Libro(int id, String titulo, String autor, String isbn, String genero, String estado) {
        this.id     = id;
        this.titulo = titulo;
        this.autor  = autor;
        this.isbn   = isbn;
        this.genero = genero;
        this.estado = estado;
    }


    // Los métodos getter me permiten leer el valor de cada atributo desde fuera
    // de esta clase sin tener que hacer los atributos públicos directamente.
    // Esto es lo que se conoce como encapsulamiento en programación orientada a objetos.

    public int    getId()      { return id; }
    public String getTitulo()  { return titulo; }
    public String getAutor()   { return autor; }
    public String getIsbn()    { return isbn; }
    public String getGenero()  { return genero; }
    public String getEstado()  { return estado; }


    // Los métodos setter me permiten modificar el valor de un atributo de forma controlada.
    // Si en el futuro necesito añadir alguna validación antes de asignar un valor,
    // solo tengo que hacerlo aquí sin tocar el resto del código.

    public void setId(int id)         { this.id = id; }
    public void setTitulo(String t)   { this.titulo = t; }
    public void setAutor(String a)    { this.autor = a; }
    public void setIsbn(String i)     { this.isbn = i; }
    public void setGenero(String g)   { this.genero = g; }
    public void setEstado(String e)   { this.estado = e; }


    // Sobreescribo toString para tener una representación legible del objeto.
    // Me resulta muy útil durante el desarrollo para depurar usando Log.d()
    // y ver los datos de un libro directamente en el logcat de Android Studio.
    @Override
    public String toString() {
        return "Libro{id=" + id +
               ", titulo=" + titulo +
               ", autor=" + autor +
               ", isbn=" + isbn +
               ", estado=" + estado + "}";
    }
}
