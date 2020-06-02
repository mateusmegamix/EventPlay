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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.eventplay.Adapter.AdapterComentario;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Helper.RecyclerItemClickListener;
import com.projeto.eventplay.Modelo.Comentario;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ComentarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView coment;
    private Button enviar;
    private Evento evento;
    private FirebaseAuth usuarioAutenticacao;
    private RecyclerView recyclerComentario;
    private AdapterComentario adapterComentario;
    private DatabaseReference eventoUsuarioRef;
    private android.app.AlertDialog dialog;
    //private List<Evento> comentarios = new ArrayList<>();
    private String idEvento;
    private String categoriaEvento;
    private Preferencia preferencia;
    private DatabaseReference databaseReference;
    private String keyComentario;
    private ArrayList<Comentario> comentarios = new ArrayList<Comentario>();
    private ValueEventListener valueEventListenerComentarios;
    private DatabaseReference firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentario);

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        inicializarComponentes();

        preferencia = new Preferencia(ComentarioActivity.this);

        Bundle extra = getIntent().getExtras();
        if(extra!=null){
            idEvento = extra.getString("IdEvento");
            categoriaEvento = extra.getString("CategoriaEvento");
        }

//        eventoUsuarioRef = ConfiguracaoFireBase.getFireBase()
//                .child("comentarios")
//                .child(ConfiguracaoFireBase.getIdUsuario());

        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.btEnviar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validarComentario();
                coment.setText(" ");

            }
        });

        recuperarEventos();

        recyclerComentario.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentario.setHasFixedSize(true);
        adapterComentario = new AdapterComentario(comentarios, this);
        recyclerComentario.setAdapter(adapterComentario);

        recyclerComentario.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerComentario,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
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


    }

    private void recuperarEventos(){

        dialog = new SpotsDialog.Builder()
        .setContext( this )
        .setMessage("Carregando Comentarios...")
        .setCancelable( false )
        .build();
        dialog.show();

        firebase = ConfiguracaoFireBase.getFireBase().child("eventos").child(categoriaEvento).child(idEvento).child("Comentario");

        valueEventListenerComentarios = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comentarios.clear();
                for(DataSnapshot dados: dataSnapshot.getChildren()){
                    Comentario comentario = dados.getValue(Comentario.class);
                    comentarios.add(comentario);
                }
                adapterComentario.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }

        };
        firebase.addValueEventListener(valueEventListenerComentarios);


//
//        eventoUsuarioRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                comentarios.clear();
//                for(DataSnapshot ds : dataSnapshot.getChildren()){
//                    comentarios.add(ds.getValue(Evento.class));
//                }
//                Collections.reverse(comentarios);
//                adapterComentario.notifyDataSetChanged();
//
//                dialog.dismiss();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    public void validarComentario(){

        //evento = configurarComentario();

        if( !coment.getText().toString().isEmpty() ){

            salvarComentario();

        }else{
            Toast.makeText(this, "Digite um comentario", Toast.LENGTH_SHORT).show();
        }
    }

    public void salvarComentario(){

        Comentario comentario = new Comentario();

        comentario.setComentario(coment.getText().toString());
        comentario.setNomeUsuario(preferencia.getCHAVE_NOME());
        comentario.setIdUsuario(preferencia.getIdentificador());

        databaseReference = ConfiguracaoFireBase.getFireBase();
        DatabaseReference pushRef = databaseReference.child("eventos").child(categoriaEvento).child(idEvento).child("Comentario").push();
        keyComentario = pushRef.getKey();
        pushRef.setValue(comentario).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String idComentario = keyComentario;
                DatabaseReference slc1 = ConfiguracaoFireBase.getFireBase();
                DatabaseReference hopperRefslc1 = slc1.child("eventos").child(categoriaEvento).child(idEvento).child("Comentario").child(idComentario);// com base na key do nó da tb_solucao, eu mudo os atributos listados abaixo
                Map<String, Object> hopperUpdatesslc1 = new HashMap<>();
                hopperUpdatesslc1.put("idComentario", idComentario);
                hopperRefslc1.updateChildren(hopperUpdatesslc1);
                Toast.makeText(ComentarioActivity.this, "Cometario salvo com Sucesso!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Erro ao salver o comentário!",Toast.LENGTH_SHORT).show();
            }
        });
        String idComentario = databaseReference.getKey();
    }

    private Evento configurarComentario(){

        String comentario  = coment.getText().toString();

        Evento evento = new Evento();
        evento.setComentario( comentario );

        return evento;

    }

    //============================ (MENU) =========================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog alerta;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_map:
                return true;
            case R.id.item_favoritos:
                abrirFavoritos();
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
        Intent intent = new Intent(ComentarioActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventos(){
        Intent intent = new Intent( ComentarioActivity.this, MeusEventosCriados.class );
        startActivity(intent);
    }

    private void abrirContato(){
        Intent intent = new Intent (ComentarioActivity.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (ComentarioActivity.this, Ajuda.class);
        startActivity(intent);
    }

    public void inicializarComponentes(){
        recyclerComentario = findViewById(R.id.IdRecyclerComentario);
        coment = (TextView) findViewById(R.id.editMensagem);

    }

    private void abrirFavoritos(){
        Intent intent = new Intent (ComentarioActivity.this, Favoritos.class);
        startActivity(intent);
    }
}
