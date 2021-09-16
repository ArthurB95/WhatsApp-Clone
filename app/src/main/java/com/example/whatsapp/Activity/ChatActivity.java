package com.example.whatsapp.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activity.Adapter.MensagensAdapter;
import com.example.whatsapp.Activity.Configuracao.ConfiguracaoFirebase;
import com.example.whatsapp.Activity.Helper.Base64Custom;
import com.example.whatsapp.Activity.Helper.UsuarioFirebase;
import com.example.whatsapp.Activity.Model.Conversa;
import com.example.whatsapp.Activity.Model.Grupo;
import com.example.whatsapp.Activity.Model.Mensagem;
import com.example.whatsapp.Activity.Model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleIamgeViewFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText editMensagem;
    private ImageView imageCamera;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    //IDENTIFICADOR USUARIOS REMETENTE E DESTINATARIO
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private static final int SELECAO_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //CONFIGURAR TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //CONFIGURAÇÕES INICIAIS
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleIamgeViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);

        //RECUPERAR DADOS DO USUARIO REMETENTE
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //RECUPERAR DADOS DO USUÁRIO DESTINATÁRIO
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {

            if(bundle.containsKey("chatGrupo")) {
               grupo = (Grupo) bundle.getSerializable("chatGrupo");
               idUsuarioDestinatario = grupo.getId();
                textViewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();

                if(foto != null) {
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleIamgeViewFoto);
                } else {
                    circleIamgeViewFoto.setImageResource(R.drawable.padrao);
                }

            } else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();

                if(foto != null) {
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleIamgeViewFoto);
                } else {
                    circleIamgeViewFoto.setImageResource(R.drawable.padrao);
                }

                //RECUPERAR DADOS USUARIO DESTINATARIO
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }

        }

        //CONFIGURAÇÃO ADAPTER
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //CONFIGURAÇÃO RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);


        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

        //EVENTO DE CLIQUE NA CAMERA
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(i.resolveActivity(getPackageManager()) != null ){

                    startActivityForResult(i, SELECAO_CAMERA);

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try{

                switch(requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }

                if(imagem != null) {

                    //RECUPERAR DADOS DA IMAGEM PARA O FIREBASE
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //CRIAR NOME IMAGEM
                    String nomeImagem = UUID.randomUUID().toString();

                    //CONFIGURAR REFERENCIA DO FIREBASE
                    StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer o upload!");
                            Toast.makeText(ChatActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //NOVA VERSÃO DO FIREBASE
                            /*imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                }
                            })*/

                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                            if(usuarioDestinatario != null) { //MENSAGEM NORMAL

                                Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario(idUsuarioRemetente);
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setImagem(downloadUrl);

                                //SALVAR MENSAGEM REMETENT
                                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                //SALVAR MENSAGEM PARA O DESTINATARIO
                                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                            } else {//MENSAGEM EM GRUPO

                                for(Usuario membro: grupo.getMembros()) {

                                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                                    mensagem.setMensagem( "imagem.jpg" );
                                    mensagem.setNome(usuarioRemetente.getNome());
                                    mensagem.setImagem(downloadUrl);

                                    //SALVAR MENSAGEM PARA O MEMBRO
                                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                    //SALVAR CONVERSA
                                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );

                                }

                            }

                            Toast.makeText(ChatActivity.this,
                                    "Sucesso ao enviar imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void enviarMensagem(View view) {

        String textoMensagem = editMensagem.getText().toString();

        if(!textoMensagem.isEmpty()){

            if( usuarioDestinatario != null ) {

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //SALVAR MENSAGEM PARA O REMETENTE
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //SALVAR MENSAGEM PARA O DESTINATARIO
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //SALVAR CONVERSA REMENTE
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

                //SALVAR CONVERSA DESTINATARIO
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

            } else {

                for(Usuario membro: grupo.getMembros()) {

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                    mensagem.setMensagem( textoMensagem );

                    mensagem.setNome(usuarioRemetente.getNome());

                    //SALVAR MENSAGEM PARA O MEMBRO
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //SALVAR CONVERSA
                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );

                }

            }

        } else {

            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar!",
                    Toast.LENGTH_LONG).show();

        }

    }

    private void salvarConversa(String idRemente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup) {

        //SALVAR CONVERSA REMETENTE
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if(isGroup) { //CONVERSA DE GRUPO

            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);

        } else { //CONVERSA NORMAL

            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");

        }

        conversaRemetente.salvar();

    }

    private void salvarMensagem(String idRemente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagensRef = database.child("mensagens");

        mensagensRef.child(idRemente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //LIMPAR TEXTO
        editMensagem.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens() {

          mensagens.clear();

          childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
              @Override
              public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                  Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                  mensagens.add(mensagem);
                  adapter.notifyDataSetChanged();

              }

              @Override
              public void onChildChanged(DataSnapshot dataSnapshot, String s) {

              }

              @Override
              public void onChildRemoved(DataSnapshot dataSnapshot) {

              }

              @Override
              public void onChildMoved(DataSnapshot dataSnapshot, String s) {

              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });

    }

}
