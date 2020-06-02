package com.projeto.eventplay.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projeto.eventplay.Modelo.Comentario;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyViewHolder> {

    private ArrayList<Comentario> comentarios;
    private Context context;

    public AdapterComentario(ArrayList<Comentario> eventos, Context constext) {
        this.comentarios = eventos;
        this.context = constext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentario, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Comentario comentario = comentarios.get(position);
        //Continua...
        holder.comentario.setText(comentario.getComentario());
        holder.usuario.setText(comentario.getNomeUsuario());

    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView comentario;
        TextView usuario;


        public MyViewHolder(View itemView){
            super(itemView);


            comentario = itemView.findViewById(R.id.txtComentario);
            usuario = itemView.findViewById(R.id.txtNomeUsuario);

        }
    }

}
