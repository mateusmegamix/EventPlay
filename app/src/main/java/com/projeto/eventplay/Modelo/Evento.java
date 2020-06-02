package com.projeto.eventplay.Modelo;

import com.google.firebase.database.DatabaseReference;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;

import java.io.Serializable;
import java.util.List;

public class Evento implements Serializable {

    //Evento
    private String id;
    private String nomeEn;
    private String tipoEn;
    private String categoria;
    private String dataEn;
    private String horaEn;
    private String descEv;

    //Foto
    private List<String>fotos;

    //Local
    private Local local;

    //Comentario
    private String comentario;



    public Evento(){
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("meus_eventos");
        setId( eventoRef.push().getKey() );
    }

    public void salvarEvento(){

        String idUsuario = ConfiguracaoFireBase.getIdUsuario();
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("meus_eventos");

        eventoRef.child(idUsuario)
                .child(getId())
                .setValue(this);

        salvarEventoPublico();
        //salvarEventoFavorito();

    }

    public void salvarEventoPublico(){

        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos");

        eventoRef.child( getCategoria() )
                .child( getId() )
                .setValue(this);

    }

    public void salvarComentarioEvento(){

        String idUsuario = ConfiguracaoFireBase.getIdUsuario();
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos").child(getCategoria()).child(getId()).child("Comentarios");

        eventoRef.child(idUsuario)
                .child(getId())
                .setValue(this);

        //salvarEvento();

    }

    public void salvarEventoFavorito(){
        String idUsuario = ConfiguracaoFireBase.getIdUsuario();
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("favoritos");

        eventoRef.child(idUsuario)
                .child(getId())
                .setValue(this);

        //salvarEvento();

    }

//    public void salvarLocalEvento(Local local){
//        String idUsuario = ConfiguracaoFireBase.getIdUsuario();
//        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
//                .child("meus_eventos");
//
//        eventoRef.child(idUsuario)
//                .child(getId()).child("local")
//                .setValue(local);
//    }

    public void removerFavorito(){
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("usuario")
                .child("EventosFavoritos");
        setId( eventoRef.push().getKey() );

        eventoRef.removeValue();
    }

    public void remover(){

        String idUsuario = ConfiguracaoFireBase.getIdUsuario();
        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("meus_eventos")
                .child( idUsuario )
                .child( getId() );

        eventoRef.removeValue();
        removerEventoPublico();
    }

    public void removerEventoPublico(){

        DatabaseReference eventoRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos")
                .child( getCategoria() )
                .child( getId() );

        eventoRef.removeValue();

    }

    //Eventos
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeEn() {
        return nomeEn;
    }

    public void setNomeEn(String nomeEn) {
        this.nomeEn = nomeEn;
    }

    public String getTipoEn() {
        return tipoEn;
    }

    public void setTipoEn(String tipoEn) {
        this.tipoEn = tipoEn;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDataEn() {
        return dataEn;
    }

    public void setDataEn(String dataEn) {
        this.dataEn = dataEn;
    }

    public String getHoraEn() {
        return horaEn;
    }

    public void setHoraEn(String horaEn) {
        this.horaEn = horaEn;
    }

    public String getDescEv() {
        return descEv;
    }

    public void setDescEv(String descEv) {
        this.descEv = descEv;
    }


    public List<String> getFotos() {
        return fotos;
    }


    //Foto
    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }


    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    //Comentario

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}




