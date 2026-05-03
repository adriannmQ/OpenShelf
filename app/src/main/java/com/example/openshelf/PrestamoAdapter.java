package com.example.openshelf;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder> {

    private final List<Prestamo> listaPrestamos;
    private OnPrestamoClickListener listener;

    public interface OnPrestamoClickListener {
        void onPrestamoClick(Prestamo prestamo, int position);
    }

    public PrestamoAdapter(List<Prestamo> listaPrestamos) {
        this.listaPrestamos = listaPrestamos;
    }

    public void setOnPrestamoClickListener(OnPrestamoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo, parent, false);
        return new PrestamoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PrestamoViewHolder holder, int position) {
        Prestamo prestamo = listaPrestamos.get(position);

        holder.tvIdPrestamo.setText("#" + prestamo.getId());
        holder.tvTituloPrestamoLibro.setText(prestamo.getTituloLibro());
        holder.tvNombreUsuarioPrestamo.setText(prestamo.getNombreUsuario());
        holder.tvFechaPrestamo.setText("Desde: " + prestamo.getFechaPrestamo());

        if (prestamo.getEstado().equals("activo")) {
            holder.tvEstadoPrestamo.setText("Activo");
            holder.tvEstadoPrestamo.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvEstadoPrestamo.setText("Devuelto");
            holder.tvEstadoPrestamo.setTextColor(Color.parseColor("#1565C0"));
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID) {
                    listener.onPrestamoClick(listaPrestamos.get(pos), pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaPrestamos.size();
    }

    public void actualizarLista(List<Prestamo> nuevaLista) {
        listaPrestamos.clear();
        listaPrestamos.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdPrestamo;
        TextView tvTituloPrestamoLibro;
        TextView tvNombreUsuarioPrestamo;
        TextView tvFechaPrestamo;
        TextView tvEstadoPrestamo;

        public PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdPrestamo            = itemView.findViewById(R.id.tvIdPrestamo);
            tvTituloPrestamoLibro   = itemView.findViewById(R.id.tvTituloPrestamoLibro);
            tvNombreUsuarioPrestamo = itemView.findViewById(R.id.tvNombreUsuarioPrestamo);
            tvFechaPrestamo         = itemView.findViewById(R.id.tvFechaPrestamo);
            tvEstadoPrestamo        = itemView.findViewById(R.id.tvEstadoPrestamo);
        }
    }
}
