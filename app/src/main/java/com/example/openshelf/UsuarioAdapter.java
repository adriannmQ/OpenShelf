package com.example.openshelf;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario, int position);
    }

    public UsuarioAdapter(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public void setOnUsuarioClickListener(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);

        holder.tvInicialUsuario.setText(String.valueOf(usuario.getNombre().charAt(0)).toUpperCase());
        holder.tvNombreUsuario.setText(usuario.getNombre());
        holder.tvEmailUsuario.setText(usuario.getEmail());

        if (usuario.getRol().equals("admin")) {
            holder.tvRolUsuario.setText("Administrador");
            holder.tvRolUsuario.setTextColor(Color.parseColor("#C62828"));
        } else {
            holder.tvRolUsuario.setText("Usuario");
            holder.tvRolUsuario.setTextColor(Color.parseColor("#1565C0"));
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID) {
                    listener.onUsuarioClick(listaUsuarios.get(pos), pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void actualizarLista(List<Usuario> nuevaLista) {
        listaUsuarios.clear();
        listaUsuarios.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicialUsuario;
        TextView tvNombreUsuario;
        TextView tvEmailUsuario;
        TextView tvRolUsuario;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInicialUsuario = itemView.findViewById(R.id.tvInicialUsuario);
            tvNombreUsuario  = itemView.findViewById(R.id.tvNombreUsuario);
            tvEmailUsuario   = itemView.findViewById(R.id.tvEmailUsuario);
            tvRolUsuario     = itemView.findViewById(R.id.tvRolUsuario);
        }
    }
}
