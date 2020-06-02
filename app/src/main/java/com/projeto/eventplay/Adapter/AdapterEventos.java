package com.projeto.eventplay.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.Modelo.Local;
import com.projeto.eventplay.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterEventos extends RecyclerView.Adapter<AdapterEventos.MyViewHolder> {

    private List<Evento> eventos;
    Local local;
    private Context context;

    public AdapterEventos(List<Evento> eventos, Context constext) {
        this.eventos = eventos;
        this.context = constext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_evento, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Evento evento = eventos.get(position);
        holder.titulo.setText(evento.getNomeEn());
        holder.tipo.setText(evento.getTipoEn());

        List<String> urlFotos = evento.getFotos();
        if(urlFotos!=null){
            String urlCapa = urlFotos.get(0);
            Picasso.get().load(urlCapa).into(holder.foto);
        }


    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        TextView tipo;
        ImageView foto;


        public MyViewHolder(View itemView){
            super(itemView);

            titulo = itemView.findViewById(R.id.textTitulo);
            tipo = itemView.findViewById(R.id.txtComentario);
            foto = itemView.findViewById(R.id.imageEvento);

        }
    }

}
