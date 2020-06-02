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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.projeto.eventplay.Adapter.AdapterEventos;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Helper.RecyclerItemClickListener;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.R;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CadastroEventos extends AppCompatActivity {

    private FirebaseAuth usuarioAutenticacao;
    private Toolbar toolbar;
    private RecyclerView recyclerEventosPublicos;
    private Button buttonCategoria;
    private AdapterEventos adapterEventos;
    private List<Evento> listaEventos = new ArrayList<>();
    private DatabaseReference eventosPublicosRef;
    private android.app.AlertDialog dialog;
    private String filtroCategoria = "";
    private boolean filtrandoPorCategoria = false;
    private Preferencia preferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_eventos);

        inicializarComponentes();

        preferencia = new Preferencia(CadastroEventos.this);

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        eventosPublicosRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos");

        //Cofigurar recyclerView

        recyclerEventosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerEventosPublicos.setHasFixedSize(true);
        adapterEventos = new AdapterEventos(listaEventos, this);
        recyclerEventosPublicos.setAdapter(adapterEventos);

        recuperarEventosPublicos();

        //Aplicar evento de clique
        recyclerEventosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEventosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Evento eventoSelecionado = listaEventos.get(position);
                                preferencia.setEventoSelecionado(eventoSelecionado);
                                Intent i = new Intent (CadastroEventos.this, DetalhesProduto.class);
                                //i.putExtra("eventoSelecionado", eventoSelecionado);
                                startActivity(i);
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

        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

    }

    //=================================================================================================

    public void filtrarCategoria(){

        android.app.AlertDialog.Builder dialogCategoria = new android.app.AlertDialog.Builder(this);
        dialogCategoria.setTitle("Escolha uma categoria");

        //Configurar spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinnerCategoria.setAdapter( adapter );

        dialogCategoria.setView(viewSpinner);

        dialogCategoria.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                recuperarEventosPorCategoria();
                filtrandoPorCategoria = true;
            }
        });

        dialogCategoria.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        android.app.AlertDialog dialog = dialogCategoria.create();
        dialog.show();

    }

    //=================================================================================================

    public void recuperarEventosPorCategoria(){

        //Configura nó por Categoria
        eventosPublicosRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos")
                .child(filtroCategoria);

        eventosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaEventos.clear();
                for (DataSnapshot eventos: dataSnapshot.getChildren() ) {

                    Evento evento = eventos.getValue(Evento.class);
                    listaEventos.add(evento);

                }

                Collections.reverse( listaEventos );
                adapterEventos.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void recuperarEventosPublicos(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Carregando Eventos...")
                .setCancelable( false )
                .build();
        dialog.show();

        listaEventos.clear();
        eventosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot categorias: dataSnapshot.getChildren()){
                    for (DataSnapshot eventos: categorias.getChildren()){
                        Evento evento = eventos.getValue(Evento.class);
                        listaEventos.add(evento);
                    }
                }
                Collections.reverse(listaEventos);
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
        inflater.inflate(R.menu.menu_pesquisa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog alerta;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_map:
                //abrirMapa();
                return true;
            case R.id.item_pesquisa:
                filtrarCategoria();
                return true;
            case R.id.item_eventos:
                abrirMeusEventos();
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
                builder.setMessage("Você deseja realmente sair do Event Play?");
                builder.setPositiveButton("Sair", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deslogarUsuario();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deslogarUsuario() {
        usuarioAutenticacao.signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CadastroEventos.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventos(){
        Intent intent = new Intent( CadastroEventos.this, MeusEventosCriados.class );
        startActivity(intent);
    }

    public void inicializarComponentes(){
        recyclerEventosPublicos = findViewById(R.id.recyclerEventosPublicos);
    }

    private void abrirMapa(){
        Intent intent = new Intent( CadastroEventos.this, MapaActivity.class );
        startActivity(intent);
    }

    private void abrirContato(){
        Intent intent = new Intent (CadastroEventos.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (CadastroEventos.this, Ajuda.class);
        startActivity(intent);
    }
}