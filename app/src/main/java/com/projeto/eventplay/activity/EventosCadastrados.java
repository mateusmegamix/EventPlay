package com.projeto.eventplay.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Permissao;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.Modelo.Local;
import com.projeto.eventplay.Modelo.Usuario;
import com.projeto.eventplay.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class EventosCadastrados extends AppCompatActivity
        implements View.OnClickListener {


    private FirebaseAuth autenticacao;

    private Usuario usuario;
    private Evento evento;

    private Toolbar toolbar;

    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoCategoria;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaURLFotos = new ArrayList<>();

    private TextView nomeEvento;
    private TextView tipoEvento;
    private TextView dataEvento;
    private TextView horaEvento;
    private TextView descEvento;

    private StorageReference storage;

    private android.app.AlertDialog dialog;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText editLocal;
    private LatLng localEvento;

    private Local local = new Local();

    private Button btnCriar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_cadastrados);

        Permissao.validapermissao(1, this, permissoes);

        inicializarComponentes();
        carregarDadosSpinner();

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        //Firebase Init
        storage = ConfiguracaoFireBase.getFirebaseStorage();

        //Mascaras
        SimpleMaskFormatter simpleMaskCodData = new SimpleMaskFormatter("NN/NN/NNNN");
        SimpleMaskFormatter simpleMaskCodHora = new SimpleMaskFormatter("NN:NN");

        MaskTextWatcher maskData = new MaskTextWatcher(dataEvento, simpleMaskCodData);
        MaskTextWatcher maskHora = new MaskTextWatcher(horaEvento, simpleMaskCodHora);

        dataEvento.addTextChangedListener(maskData);
        horaEvento.addTextChangedListener(maskHora);

        //Botão para salvar
        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pega o local antes de savlar, valida se o local esta vazio.
                // Um meio de saber se o usuário ja clicou no botao do gps
                validarDadosEvento();
            }
        });

    }

//============================= SALVAR =============================================================

    public void validarDadosEvento(){

        evento = configurarEvento();

        if( listaFotosRecuperadas.size() != 0  ){
            if( !evento.getNomeEn().isEmpty() ){
                if( !evento.getTipoEn().isEmpty() ){
                    if( !evento.getDataEn().isEmpty() ){
                            if( !evento.getHoraEn().isEmpty()  ){
                                if( !evento.getDescEv().isEmpty() ){

                                    salvarEvento();


                                }else {
                                    exibirMensagemErro("Preencha o campo descrição");
                                }
                            }else {
                                exibirMensagemErro("Preencha o campo hora");
                            }
                        }else {
                            exibirMensagemErro("Preencha o campo data");
                        }
                    }else {
                        exibirMensagemErro("Preencha o campo tipo");
                    }
                }else {
                    exibirMensagemErro("Preencha o campo nome");
                }
            }
        }

    public void salvarEvento(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Salvando Evento")
                .setCancelable( false )
                .build();
        dialog.show();

        for (int i=0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i );
        }
    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){

        //Criar nó no storage
        StorageReference imagemEvento = storage.child("imagens")
                .child("eventos")
                .child( evento.getId() )
                .child("imagem"+contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemEvento.putFile( Uri.parse(urlString) );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                String urlConvertida = firebaseUrl.toString();

                listaURLFotos.add( urlConvertida );

                if( totalFotos == listaURLFotos.size() ){
                    evento.setFotos( listaURLFotos );
                    evento.setLocal(local);
                    evento.salvarEvento();
                    dialog.dismiss();
                    finish();
                    abrirTelaPrincipal();
                    Toast.makeText(EventosCadastrados.this, "Evento Cadastrado com Sucesso",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
        });

    }
    private void exibirMensagemErro(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    //Configurar Evento
    private Evento configurarEvento(){

        String nome  = nomeEvento.getText().toString();
        String tipo = tipoEvento.getText().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String data = dataEvento.getText().toString();
        String hora = horaEvento.getText().toString();
        String descricao = descEvento.getText().toString();

        Evento evento = new Evento();
        evento.setNomeEn( nome );
        evento.setTipoEn(tipo);
        evento.setCategoria(categoria);
        evento.setDataEn(data);
        evento.setHoraEn(hora);
        evento.setDescEv(descricao);

        return evento;

    }

    //==================================== Localização ============================================

    //botão
    public void marcarLocal(View view){

        String enderecoLocal = editLocal.getText().toString();

        if( !enderecoLocal.equals("") || enderecoLocal != null ){

            Address addressLocal = recuperarEndereco( enderecoLocal );
            if( addressLocal != null ){


                local.setCidade( addressLocal.getAdminArea() );
                local.setCep( addressLocal.getPostalCode() );
                local.setBairro( addressLocal.getSubLocality() );
                local.setRua( addressLocal.getThoroughfare() );
                local.setNumero( addressLocal.getFeatureName() );
                local.setLatitude( String.valueOf(addressLocal.getLatitude()) );
                local.setLongitude( String.valueOf(addressLocal.getLongitude()) );

                StringBuilder mensagem = new StringBuilder();
                mensagem.append( "Estado: " + local.getCidade() );
                mensagem.append( "\nRua: " + local.getRua() );
                mensagem.append( "\nBairro: " + local.getBairro() );
                mensagem.append( "\nNúmero: " + local.getNumero() );
                mensagem.append( "\nCep: " + local.getCep() );

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Confirme seu endereco!")
                        .setMessage(mensagem)
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(EventosCadastrados.this, "Local Confirmado",
                                        Toast.LENGTH_SHORT).show();


                            }
                        }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(EventosCadastrados.this, "Insira sua Localização",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            }else {
                Toast.makeText(this,
                        "Informe o local do evento!",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if( listaEnderecos != null && listaEnderecos.size() > 0 ){
                Address address = listaEnderecos.get(0);

                return address;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    //=============================== IMAGENS ==========================================================
    //Configurar Imagens
    @Override
    public void onClick(View v) {
        Log.d("onClick", "onClick: " + v.getId() );
        switch ( v.getId() ){
            case R.id.btnImagem1 :
                Log.d("onClick", "onClick: " );
                escolherImagem(1);
                break;
            case R.id.btnImagem2 :
                escolherImagem(2);
                break;
            case R.id.btnImagem3 :
                escolherImagem(3);
                break;
        }

    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //Recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if (requestCode == 1) {
                imagem1.setImageURI(imagemSelecionada);
            } else if (requestCode == 2) {
                imagem2.setImageURI(imagemSelecionada);
            } else if (requestCode == 3) {
                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);

        }

    }

    //================================= CATEGORIAS (FILTROS) =======================================

    //Filtro de Categorias
    private void carregarDadosSpinner(){

        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoCategoria.setAdapter( adapter );

    }

    //Componentes
    private void inicializarComponentes() {

        nomeEvento = findViewById(R.id.tNome);
        tipoEvento = findViewById(R.id.tTipo);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        dataEvento = findViewById(R.id.tData);
        horaEvento = findViewById(R.id.tHora);
        descEvento = findViewById(R.id.tDescricao);
        editLocal = findViewById(R.id.editLocal);
        imagem1 = findViewById(R.id.btnImagem1);
        imagem2 = findViewById(R.id.btnImagem2);
        imagem3 = findViewById(R.id.btnImagem3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        btnCriar = (Button) findViewById(R.id.btnSalvarId);

    }

    //========================= PERMISSÕES ==========================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults ){
            if( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //===================================== Menu ===================================================
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
        autenticacao.signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(this, "SignOut", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EventosCadastrados.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventos(){
        Intent intent = new Intent( EventosCadastrados.this, MeusEventosCriados.class );
        startActivity(intent);
    }

    private void abrirTelaPrincipal() {

        Intent intent = new Intent(EventosCadastrados.this, Principal.class);
        startActivity(intent);
    }

    private void abrirMapa(){
        Intent intent = new Intent( EventosCadastrados.this, MapaActivity.class );
        startActivity(intent);

    }

    private void abrirContato(){
        Intent intent = new Intent (EventosCadastrados.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda(){
        Intent intent = new Intent (EventosCadastrados.this, Ajuda.class);
        startActivity(intent);
    }

    private void abrirFavoritos(){
        Intent intent = new Intent (EventosCadastrados.this, Favoritos.class);
        startActivity(intent);
    }

}