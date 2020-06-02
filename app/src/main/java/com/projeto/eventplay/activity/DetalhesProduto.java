package com.projeto.eventplay.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.R;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.HashMap;
import java.util.Map;

public class DetalhesProduto extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView categoria;
    private TextView titulo;
    private TextView tipo;
    private TextView data;
    private TextView hora;
    private TextView descricao;
    private Evento eventoSelecionado;
    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;
    private ImageButton imageButton;
    private Preferencia preferencia;

    //botão
    private Button botaoComentario;
    private Button botaoRota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_produto);

        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        inicializarComponentes();
        preferencia = new Preferencia(DetalhesProduto.this);
        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        //recuperar dados do evento
        //eventoSelecionado = (Evento) getIntent().getSerializableExtra("eventoSelecionado");
        eventoSelecionado = preferencia.getEventoSelecionado();

        if (eventoSelecionado != null){
            categoria.setText(eventoSelecionado.getCategoria());
            titulo.setText(eventoSelecionado.getNomeEn());
            tipo.setText(eventoSelecionado.getTipoEn());
            data.setText(eventoSelecionado.getDataEn());
            hora.setText(eventoSelecionado.getHoraEn());
            descricao.setText(eventoSelecionado.getDescEv());

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = eventoSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };

            carouselView.setPageCount(eventoSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);

        }

        botaoRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getApplicationContext(), MapaActivity.class );
                intent.putExtra("raio",0);
                intent.putExtra("origem","DetalhesProduto");
                startActivity(intent);
            }
        });

        botaoComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalhesProduto.this, ComentarioActivity.class);
                intent.putExtra("IdEvento",eventoSelecionado.getId());
                intent.putExtra("CategoriaEvento",eventoSelecionado.getCategoria());
                startActivity(intent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference slc1 = ConfiguracaoFireBase.getFireBase();
                DatabaseReference hopperRefslc1 = slc1.child("usuario").child(preferencia.getIdentificador()).child("EventosFavoritos").child(eventoSelecionado.getId());// com base na key do nó da tb_solucao, eu mudo os atributos listados abaixo
                Map<String, Object> hopperUpdatesslc1 = new HashMap<>();
                hopperUpdatesslc1.put("id", eventoSelecionado.getId());
                hopperRefslc1.updateChildren(hopperUpdatesslc1);
                Toast.makeText(getApplicationContext(),"Evento Favoritado!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselView);
        categoria = findViewById(R.id.textCategoriaDetalhe);
        titulo = findViewById(R.id.textTituloDetalhe);
        tipo = findViewById(R.id.textTipoDetalhe);
        data = findViewById(R.id.textDataDetalhe);
        hora = findViewById(R.id.textHoraDetalhe);
        descricao = findViewById(R.id.textDescricaoDetalhe);
        botaoComentario = (Button) findViewById(R.id.btnMensagem);
        imageButton = (ImageButton) findViewById(R.id.imageStar);
        botaoRota = (Button) findViewById(R.id.buttonChegar);
    }

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
        Intent intent = new Intent(DetalhesProduto.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventos(){
        Intent intent = new Intent( DetalhesProduto.this, MeusEventosCriados.class );
        startActivity(intent);
    }

    private void abrirContato(){
        Intent intent = new Intent (DetalhesProduto.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (DetalhesProduto.this, Ajuda.class);
        startActivity(intent);
    }

    private void abrirFavoritos(){
        Intent intent = new Intent (DetalhesProduto.this, Favoritos.class);
        startActivity(intent);
    }
}
