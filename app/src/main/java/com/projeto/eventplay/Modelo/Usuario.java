package com.projeto.eventplay.Modelo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String idUsuario;
    private String nome;
    private String dataNasc;
    private String cpf;
    private String email;
    private String senha;

    private String latitude;
    private String longitude;

    /*public static String TOKEN = "com.aplication.eventplay.Usuario.TOKEN";
    public static String ID = "com.aplication.eventplay.Usuario.ID";*/

    public Usuario(){

    }

    public void salvar(){
        DatabaseReference referenceFirebase = ConfiguracaoFireBase.getFireBase();
        referenceFirebase.child("usuario").child (getId()).setValue(this);
    }

    public String getId() {
        return idUsuario;
    }

    public void setId(String id) {
        this.idUsuario = id;
    }

    /*public void saveIdSP (Context context, String token){
        LibraryClass.saveSP(context, ID, token);
    }

    public void retrieveIdSP (Context context){
        this.id = LibraryClass.getSP(context, ID);
    }*/

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /*private void setNomeInMap(Map<String, Object> map){
        if(getNome()!=null){
            map.put("name", getNome());
        }}*/

    public String getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(String dataNasc) {
        this.dataNasc = dataNasc;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /*public void saveTokenSP(Context context, String token){
        LibraryClass.saveSP(context, TOKEN, token);
    }

    public String getTokenSP(Context context){
        String token = LibraryClass.getSP(context, TOKEN);
        return (token);
    }

    public void saveDB(){
        Firebase firebase = LibraryClass.getFirebase();
        firebase = firebase.child("users").child(getId());

        setSenha(null);
        setId(null);
        firebase.setValue(this);
    }*/

}
