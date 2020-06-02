package com.projeto.eventplay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.R;

public class Principal extends AppCompatActivity {

    private FirebaseAuth usuarioAutenticacao;
    private Toolbar toolbar;
    private ImageView botaoEventosCadastrados;
    private ImageView botaoCadastroEventos;
    private EditText txtRaio;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        botaoEventosCadastrados = (ImageView) findViewById(R.id.btnEventosCad);
        botaoCadastroEventos = (ImageView) findViewById(R.id.btnCadEventos);


        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);

        botaoEventosCadastrados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Principal.this, CadastroEventos.class));
            }
        });

        botaoCadastroEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Principal.this, EventosCadastrados.class));
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

    @Override
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

    private void deslogarUsuario() {
        usuarioAutenticacao.signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Principal.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventosCriados(){
        Intent intent = new Intent( Principal.this, MeusEventosCriados.class );
        startActivity(intent);

    }

    private void abrirMapa(){
        //Abrir caixa de dialogo para informar o raio da pesquisa
        ExibeDialogo();


    }

    private void abrirContato(){
        Intent intent = new Intent (Principal.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (Principal.this, Ajuda.class);
        startActivity(intent);
    }

    private void abrirFavoritos(){
        Intent intent = new Intent (Principal.this, Favoritos.class);
        startActivity(intent);
    }

    private void ExibeDialogo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.DialogStyle);
        builder.setPositiveButton("Pesquisar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!txtRaio.getText().toString().isEmpty()){
                    Intent intent = new Intent( Principal.this, MapaActivity.class );
                    intent.putExtra("raio",Integer.valueOf(txtRaio.getText().toString()));
                    intent.putExtra("origem","Principal");
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Preencha o campo!",Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancelar", null);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        dialog.setTitle("Visualizar Eventos");
        View dialogLayout = inflater.inflate(R.layout.layout_dialog_filtro_raio, null);
        txtRaio = (EditText) dialogLayout.findViewById(R.id.txt_raio);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.show();
    }
}