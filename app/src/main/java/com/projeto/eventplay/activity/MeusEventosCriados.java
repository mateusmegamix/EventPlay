package com.projeto.eventplay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

import com.facebook.login.LoginManager;
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
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MeusEventosCriados extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;
    private RecyclerView recyclerEventos;
    private List<Evento> eventos = new ArrayList<>();
    private AdapterEventos adapterEventos;
    private DatabaseReference eventoUsuarioRef;
    private android.app.AlertDialog dialog;
    private Preferencia preferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_eventos_criados);

        preferencia = new Preferencia(MeusEventosCriados.this);

        eventoUsuarioRef = ConfiguracaoFireBase.getFireBase()
                .child("meus_eventos")
                .child(ConfiguracaoFireBase.getIdUsuario());

        inicializarComponentes();

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), EventosCadastrados.class));
            }
        });

        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
        recyclerEventos.setHasFixedSize(true);
        adapterEventos = new AdapterEventos(eventos, this);
        recyclerEventos.setAdapter(adapterEventos);

        recuperarEventos();

        recyclerEventos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEventos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Evento eventoSelecionado = eventos.get(position);
                                preferencia.setEventoSelecionado(eventoSelecionado);
                                Intent i = new Intent (MeusEventosCriados.this, DetalhesProduto.class);
                                //i.putExtra("eventoSelecionado", eventoSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, final int position) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(MeusEventosCriados.this);
                                //Configurações de Dialog
                                builder.setTitle("Excluir Evento");
                                builder.setMessage("Você deseja realmente excluir evento?");
                                builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Evento eventoSelecionado = eventos.get(position);
                                        eventoSelecionado.remover();
                                        Toast.makeText(MeusEventosCriados.this, "Evento foi excluído", Toast.LENGTH_SHORT).show();
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
    private void recuperarEventos(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Carregando Eventos...")
                .setCancelable( false )
                .build();
        dialog.show();

        eventoUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventos.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    eventos.add(ds.getValue(Evento.class));
                }
                Collections.reverse(eventos);
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
        inflater.inflate(R.menu.menu_meus_eventos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog alerta;

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_novo:
                abrirEventosCadastrados();
                return true;
            case R.id.item_favoritos:
                abrirFavoritos();
                return true;
            case R.id.item_eventos:
                return true;
            case R.id.item_ajuda:
                abrirAjuda();
                return true;
            case R.id.item_contato:
                abrirContato();
                return true;
            case R.id.item_sair:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

                //Configurações de Dialog
                builder2.setTitle("Sair");
                builder2.setMessage("Você deseja realmente sair do Event Play?");
                builder2.setPositiveButton("Sair", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deslogarUsuario();
                    }
                });
                builder2.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alerta = builder2.create();
                alerta.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deslogarUsuario() {
        usuarioAutenticacao.signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MeusEventosCriados.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirEventosCadastrados(){
        Intent intent = new Intent(MeusEventosCriados.this, EventosCadastrados.class);
        startActivity(intent);
        finish();
    }

    private void abrirContato(){
        Intent intent = new Intent (MeusEventosCriados.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (MeusEventosCriados.this, Ajuda.class);
        startActivity(intent);
    }

    public void inicializarComponentes(){

        recyclerEventos = findViewById(R.id.recyclerEventos);
    }

    private void abrirFavoritos(){
        Intent intent = new Intent (MeusEventosCriados.this, Favoritos.class);
        startActivity(intent);
    }

}

