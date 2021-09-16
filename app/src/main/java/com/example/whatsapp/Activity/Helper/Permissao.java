package com.example.whatsapp.Activity.Helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        if(Build.VERSION.SDK_INT >= 23 ) {

             List<String> listaPermissoes = new ArrayList<>();

             //PERCORRER AS PERMISSÕES UE FORAM PASSADAS, VERIFICANDO UMA A UMA SE JÁ TEM A PERMISSÃO LIBERADA
             for( String permissao : permissoes ) {
                Boolean temPermissao =  ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;

                if( !temPermissao ) listaPermissoes.add(permissao);
             }

             //CASO A LISTA ESTEJA VAZIA, NÃO É NECESSÁRIO SOLICITAR PERMISSÃO
            if(listaPermissoes.isEmpty()) return true;
            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            //SOLICITAR PERMISSÃO
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);

        }

        return true;
    }
}
