package com.projeto.eventplay.FireBase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class ConfiguracaoFireBase {

    private static DatabaseReference referenceFireBase;
    private static FirebaseAuth autenticacao;
    private static StorageReference referenciaStorage;


    public static String getIdUsuario(){

        FirebaseAuth autenticacao = getFirebaseAutenticaticacao();
        return autenticacao.getCurrentUser().getUid();

    }


    public static DatabaseReference getFireBase(){

        if(referenceFireBase == null) {
            referenceFireBase = FirebaseDatabase.getInstance().getReference();
        }
        return referenceFireBase;
    }


    public static FirebaseAuth getFirebaseAutenticaticacao(){
        if (autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }


    public static StorageReference getFirebaseStorage(){
        if( referenciaStorage == null ){
            referenciaStorage = FirebaseStorage.getInstance().getReference();
        }
        return referenciaStorage;
    }

}
