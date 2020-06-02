package com.projeto.eventplay.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.projeto.eventplay.Modelo.Evento;

public class Preferencia {
    private Context contexto;
    private SharedPreferences preferences;
    private String NOME_ARQUIVO = "eventplay.preferencias";
    private int MODE = 0;
    private SharedPreferences.Editor editor;
    private String CHAVE_IDENTIFICADOR = "identificadorUsuarioLogado";
    private String CHAVE_NOME = "nomeUsuarioLogado";
    private static final String EVENTO_SELECIONADO = "EVENTO_SELECIONADO";

    public Preferencia(Context contextoParametro){
        contexto = contextoParametro;
        preferences = contexto.getSharedPreferences(NOME_ARQUIVO,MODE);
        editor = preferences.edit();
    }

    public void salvarDados (String identificadorUsuario, String nomeUsuario){
        editor.putString(CHAVE_IDENTIFICADOR, identificadorUsuario);
        editor.putString(CHAVE_NOME, nomeUsuario);
        editor.commit();
    }

    public String getIdentificador(){ return preferences.getString(CHAVE_IDENTIFICADOR, null); }
    public String getCHAVE_NOME() { return preferences.getString(CHAVE_NOME, null); }

    public void setEventoSelecionado(Evento evento){
        String json = new Gson().toJson(evento);
        editor.putString(EVENTO_SELECIONADO,json);
        editor.commit();
    }

    public Evento getEventoSelecionado(){
        String json = preferences.getString(EVENTO_SELECIONADO,null);
        if(json == null){
            return new Evento();
        }
        Evento evento = new Gson().fromJson(json, Evento.class);
        return evento;
    }
}
