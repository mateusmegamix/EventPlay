package com.projeto.eventplay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.eventplay.Adapter.AdapterEventos;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Helper.RecyclerItemClickListener;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.R;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class Favoritos extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;
    private RecyclerView recyclerEventos;
    private List<Evento> listaFavoritos = new ArrayList<>();
    private AdapterEventos adapterEventos;
    private DatabaseReference eventoUsuarioRef;
    private android.app.AlertDialog dialog;
    private Preferencia preferencia;
    private ArrayList<String> listFavoritos = new ArrayList<String>();
    private ValueEventListener valueEventListenerFavoritos;
    private DatabaseReference firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        eventoUsuarioRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos");

        inicializarComponentes();

        preferencia = new Preferencia(Favoritos.this);

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);


        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
        recyclerEventos.setHasFixedSize(true);
        adapterEventos = new AdapterEventos(listaFavoritos, this);
        recyclerEventos.setAdapter(adapterEventos);

        firebase = ConfiguracaoFireBase.getFireBase().child("usuario").child(preferencia.getIdentificador()).child("EventosFavoritos");

        valueEventListenerFavoritos = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listFavoritos.clear();
                for(DataSnapshot dados: dataSnapshot.getChildren()){
                    listFavoritos.add(dados.getKey());
                }
                recuperarFavoritos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        };
        firebase.addListenerForSingleValueEvent(valueEventListenerFavoritos);



        recyclerEventos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEventos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Evento eventoSelecionado = listaFavoritos.get(position);
                                Intent i = new Intent (Favoritos.this, DetalhesProduto.class);
                                preferencia.setEventoSelecionado(eventoSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, final int position) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Favoritos.this);
                                //Configurações de Dialog
                                builder.setTitle("Excluir Evento");
                                builder.setMessage("Você deseja realmente excluir evento?");
                                builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Evento eventoSelecionado = listaFavoritos.get(position);
                                        eventoSelecionado.removerFavorito();
                                        Toast.makeText(Favoritos.this, "Evento foi excluído", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Intent intent = new Intent(MeusEventos.this, MeusEventos.class);
                                        //startActivity(intent);
                                    }
                                });
                                alerta = builder.create();
                                alerta.show();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void recuperarFavoritos(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Carregando Eventos...")
                .setCancelable( false )
                .build();
        dialog.show();

        eventoUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaFavoritos.clear();
                for(DataSnapshot categorias: dataSnapshot.getChildren()){
                    for (DataSnapshot eventos: categorias.getChildren()){
                        Evento evento = eventos.getValue(Evento.class);
                        for(String key: listFavoritos){
                            if(evento.getId().equals(key)){
                                listaFavoritos.add(evento);
                            }
                        }
                    }
                }


//                for(DataSnapshot ds : dataSnapshot.getChildren()){
//                    listaFavoritos.add(ds.getValue(Evento.class));
//                }
//                Collections.reverse(listaFavoritos);
                adapterEventos.notifyDataSetChanged();

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog alerta;

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_map:
                abrirMapa();
                return true;
            case R.id.item_favoritos:
                abrirFavoritos();
                return true;
            case R.id.item_eventos:
                abrirMeusEventosCriados();
                return true;
            case R.id.item_ajuda:
                abrirAjuda();
                return true;
            case R.id.item_contato:
                abrirContato();
                return true;
            case R.id.item_sair:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                //Configurações de Dialog
                builder.setTitle("Sair");
                builder.setMessage("Você deseja sair do Event Play?");
                builder.setPositiveButton("Sair", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deslogarUsuario();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alerta = builder.create();
                alerta.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deslogarUsuario () {
        usuarioAutenticacao.signOut();
        //LoginManager.getInstance().logOut();
        Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Favoritos.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventosCriados(){
        Intent intent = new Intent( Favoritos.this, MeusEventosCriados.class );
        startActivity(intent);

    }

    private void abrirMapa(){
        Intent intent = new Intent( Favoritos.this, MapaActivity.class );
        startActivity(intent);

    }

    private void abrirContato(){
        Intent intent = new Intent (Favoritos.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (Favoritos.this, Ajuda.class);
        startActivity(intent);
    }

    private void abrirFavoritos(){
        Intent intent = new Intent (Favoritos.this, Favoritos.class);
        startActivity(intent);
    }

    public void inicializarComponentes(){

        recyclerEventos = findViewById(R.id.recyclerEventosFavoritos);
    }

}
