package com.example.whatsapp.Activity.Configuracao;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static FirebaseAuth autenticacao;
    private static DatabaseReference firebase;
    private static StorageReference storage;

    //RETORNAR A INSTANCIA DO FIREBASEDATABASE
    public static DatabaseReference getFirebaseDatabase() {

        if(firebase == null) {
            firebase = FirebaseDatabase.getInstance().getReference();
        }

        return firebase;
    }

    //RETORNAR A INSTANCIA DO FIREBASEAUTH
    public static FirebaseAuth getFirebaseAutenticacao() {

        if(autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }

        return autenticacao;
    }

    //SALVAR IMAGEM NO FIREBASE
    public static StorageReference getFirebaseStorage(){

        if(storage == null){
            storage = FirebaseStorage.getInstance().getReference();
        }

        return storage;
    }
}
