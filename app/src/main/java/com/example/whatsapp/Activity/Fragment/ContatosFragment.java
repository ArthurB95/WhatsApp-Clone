package com.example.whatsapp.Activity.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.Activity.Adapter.ContatoAdapter;
import com.example.whatsapp.Activity.Adapter.ConversasAdapter;
import com.example.whatsapp.Activity.ChatActivity;
import com.example.whatsapp.Activity.Configuracao.ConfiguracaoFirebase;
import com.example.whatsapp.Activity.GrupoActivity;
import com.example.whatsapp.Activity.Helper.RecyclerItemClickListener;
import com.example.whatsapp.Activity.Helper.UsuarioFirebase;
import com.example.whatsapp.Activity.Model.Conversa;
import com.example.whatsapp.Activity.Model.Usuario;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatoAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contatos, container, false);

        //CONFIGURAÇÕES INICIAIS
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();


        //CONFIGURAR ADAPTER
        adapter = new ContatoAdapter(listaContatos, getActivity());


        //CONFIGURAR RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter(adapter);

        //CONFIGURAR EVENTO DE CLIQUE NO RECYCLERVIEW
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get(position);

                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if(cabecalho) {

                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);

                                } else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);

                                    startActivity(i);
                                }

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        adicionarMenuNovoGrupo();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }

    public void recuperarContatos(){

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                limparListaContatos();

                for(DataSnapshot dados: dataSnapshot.getChildren()) {

                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();

                    if(!emailUsuarioAtual.equals( usuario.getEmail() )) {

                        listaContatos.add(usuario);

                    }

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void limparListaContatos() {

        listaContatos.clear();
        adicionarMenuNovoGrupo();

    }

    public void adicionarMenuNovoGrupo() {
        /*Define usuário com e-mail vazio
        em caso de e-mail vazio o usuário será utilizado como
        cabeçalho, exibindo novo grupo
         */

        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listaContatos.add(itemGrupo);
    }

    public void pesquisarContatos(String texto) {

        //Log.d("pesquisa", texto);

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for(Usuario usuario : listaContatos) {

            String nome = usuario.getNome().toLowerCase();
            if(nome.contains(texto)) {
                listaContatosBusca.add(usuario);
            }

        }

        adapter = new ContatoAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos(){

        adapter = new ContatoAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

}
