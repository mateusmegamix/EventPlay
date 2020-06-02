package com.projeto.eventplay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.R;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;


public class Contato extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contato);

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);
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
                //abrirMapa();
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
        Intent intent = new Intent(Contato.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventosCriados(){
        Intent intent = new Intent( Contato.this, MeusEventosCriados.class );
        startActivity(intent);

    }

    private void abrirMapa(){
        Intent intent = new Intent( Contato.this, MapaActivity.class );
        startActivity(intent);

    }

    private void abrirAjuda(){
        Intent intent = new Intent( Contato.this, Ajuda.class );
        startActivity(intent);

    }

    private void abrirFavoritos(){
        Intent intent = new Intent (Contato.this, Favoritos.class);
        startActivity(intent);
    }
}
