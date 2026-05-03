package com.example.openshelf;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Esta clase es el adapter del RecyclerView que muestra la lista de libros.
// Su responsabilidad es hacer de puente entre los datos, que son objetos Libro,
// y las vistas que el usuario ve en pantalla, que son las filas del listado.
// El RecyclerView no sabe nada sobre los datos; me delega completamente esa tarea.
//
// Extiendo RecyclerView.Adapter parametrizado con mi propio ViewHolder,
// lo que me obliga a implementar tres métodos concretos que Android necesita.
public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    // Guardo la lista de libros y el contexto que necesito para inflar el layout
    private final List<Libro> listaLibros;
    private final Context     context;

    // Guardo una referencia al listener de clicks para notificarlo cuando sea necesario
    private OnLibroClickListener listener;


    // Defino una interfaz para que la activity pueda reaccionar cuando el usuario
    // pulse sobre un libro de la lista sin que el adapter tenga que conocer la activity.
    // Este patrón se llama observer y desacopla el adapter de quien lo usa.
    public interface OnLibroClickListener {
        // Este método lo implementará la activity para saber qué libro se ha pulsado
        // y en qué posición de la lista se encontraba
        void onLibroClick(Libro libro, int position);
    }


    // Recibo el contexto y la lista de libros al construir el adapter.
    // El contexto lo necesito para poder inflar el layout xml de cada fila.
    public LibroAdapter(Context context, List<Libro> listaLibros) {
        this.context     = context;
        this.listaLibros = listaLibros;
    }


    // Este método permite que la activity registre su listener para recibir
    // los eventos de click. Si nadie lo registra, simplemente no hago nada al pulsar.
    public void setOnLibroClickListener(OnLibroClickListener listener) {
        this.listener = listener;
    }


    // Android llama a onCreateViewHolder cuando necesita una vista nueva porque
    // no hay ninguna reciclada disponible en ese momento.
    // Aquí inflo el layout xml de una fila y creo un ViewHolder que guarda
    // las referencias a sus vistas internas para no tener que buscarlas cada vez.
    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater convierte el archivo xml item_libro en un objeto View en memoria.
        // El parámetro false indica que no quiero adjuntarlo al parent todavía,
        // porque el RecyclerView se encargará de hacerlo en el momento adecuado.
        View vistaFila = LayoutInflater
                .from(context)
                .inflate(R.layout.item_libro, parent, false);

        return new LibroViewHolder(vistaFila);
    }


    // Android llama a onBindViewHolder cuando tiene que rellenar una vista,
    // ya sea nueva o reutilizada de otra fila que ha salido de pantalla.
    // Aquí cojo el libro que corresponde a la posición indicada y vuelco
    // sus datos en los textviews del ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {

        // Obtengo el libro que corresponde a esta posición en la lista
        Libro libroActual = listaLibros.get(position);

        // Relleno cada textview con el dato correspondiente del libro.
        // Los IDs usados aquí coinciden exactamente con los definidos en item_libro.xml.
        holder.tvTituloLibro.setText(libroActual.getTitulo());
        holder.tvAutorLibro.setText(libroActual.getAutor());
        holder.tvGeneroLibro.setText(libroActual.getGenero());

        // El estado viene como String desde el ENUM de la BD ('disponible' o 'prestado').
        // Cambio el color del texto dinámicamente según su valor.
        if (libroActual.getEstado().equals("disponible")) {
            holder.tvEstadoLibro.setText("Disponible");
            holder.tvEstadoLibro.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvEstadoLibro.setText("Prestado");
            holder.tvEstadoLibro.setTextColor(Color.parseColor("#C62828"));
        }

        // Si alguien ha registrado un listener, asigno el click a la vista raíz de la fila.
        // Uso getAdapterPosition() en lugar de position porque este valor puede haber
        // quedado desactualizado si se han insertado o eliminado elementos mientras tanto.
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID) {
                    listener.onLibroClick(listaLibros.get(pos), pos);
                }
            });
        }
    }


    // Android llama a getItemCount para saber cuántas filas en total debe gestionar.
    // Simplemente devuelvo el tamaño de mi lista de libros.
    @Override
    public int getItemCount() {
        return listaLibros.size();
    }


    // LibroViewHolder implementa el patrón viewholder.
    // Su único propósito es guardar las referencias a las vistas de una fila
    // para que onBindViewHolder pueda acceder a ellas sin repetir llamadas a
    // findViewById, lo cual mejora el rendimiento del scroll notablemente.
    public static class LibroViewHolder extends RecyclerView.ViewHolder {

        // Los nombres de estas variables coinciden exactamente con los IDs
        // definidos en el layout item_libro.xml
        TextView tvTituloLibro;
        TextView tvAutorLibro;
        TextView tvGeneroLibro;
        TextView tvEstadoLibro;

        // Recibo la vista raíz de la fila y busco dentro de ella cada subvista por su id.
        // Este constructor solo se ejecuta una vez por cada ViewHolder creado,
        // no una vez por cada libro mostrado, que es justo lo que quiero conseguir.
        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTituloLibro = itemView.findViewById(R.id.tvTituloLibro);
            tvAutorLibro  = itemView.findViewById(R.id.tvAutorLibro);
            tvGeneroLibro = itemView.findViewById(R.id.tvGeneroLibro);
            tvEstadoLibro = itemView.findViewById(R.id.tvEstadoLibro);
        }
    }
}
