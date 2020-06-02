package com.projeto.eventplay.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Base64Custom;
import com.projeto.eventplay.Helper.Permissao;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Modelo.Usuario;
import com.projeto.eventplay.R;

import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView email;
    private TextView senha;
    private Button botaoLogar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;
    private DatabaseReference firebaseDataReferece;
    private ValueEventListener valueEventListenerUsuario;
    private String identificadorUsuarioLogado;
    private GoogleSignInAccount account;
    private android.app.AlertDialog dialog;
    private GoogleApiClient googleApiClient;

    int RC_SIGN_IN = 0;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;

    private Button loginFacebook;
    private CallbackManager callbackManager;

    Firebase firebase;

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        signInButton = findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (getString(R.string.clientIdGoogleSign))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        //mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        verificaUsuarioLogado();

        //validar permissões
        Permissao.validapermissao(1, this, permissoes);

        email = (TextView) findViewById(R.id.txtEmail);
        senha = (TextView) findViewById(R.id.txtSenha);
        botaoLogar = (Button) findViewById(R.id.logarId);
        loginFacebook = (Button) findViewById(R.id.login_button);

        botaoLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usuario = new Usuario();
                usuario.setEmail(email.getText().toString());
                usuario.setSenha(senha.getText().toString());
                validaLogin();
            }
        });

        loginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(Login.this, "Successo ao fazer Login", Toast.LENGTH_SHORT).show();
                        abrirTelaPrincipal();
                        handleFacebookAcessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Login.this, "Login cancelado", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(Login.this, "Não foi possível fazer Login", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleFacebookAcessToken(AccessToken Token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(Token.getToken());
        autenticacao.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Autentificação falhou!", Toast.LENGTH_SHORT).show();
                                }
                                abrirTelaPrincipal();
                            }
                        }
                );
    }



    //=========================================LOGIN GMAIL==========================================

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            account = result.getSignInAccount();
            firebaseAuthWithGoogle();
        } else{
            Toast.makeText(Login.this, "Falha ao Autenticar!", Toast.LENGTH_SHORT).show();
        }

    }

    private void firebaseAuthWithGoogle(){
        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Carregando ...")
                .setCancelable( false )
                .build();
        dialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        autenticacao.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    usuario = new Usuario();
                    usuario.setNome(account.getDisplayName());
                    usuario.setDataNasc("");
                    usuario.setCpf("");
                    usuario.setEmail(account.getEmail());
                    usuario.setSenha("");

                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificadorUsuario);

                    Preferencia preferencias = new Preferencia(Login.this);
                    preferencias.salvarDados(identificadorUsuario, usuario.getNome());

                    DatabaseReference fb = ConfiguracaoFireBase.getFireBase();
                    fb.child("usuario").child(identificadorUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                DatabaseReference referenceFirebase = ConfiguracaoFireBase.getFireBase();
                                referenceFirebase.child("usuario").child(usuario.getId()).setValue(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Login.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                                        AbreTelaPrincipal();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String error = e.getMessage();
                                        Toast.makeText(Login.this, "Erro: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                AbreTelaPrincipal();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                else{
                    Toast.makeText(Login.this,"Falha na autentição!",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    private void AbreTelaPrincipal(){
        dialog.dismiss();
        Intent intent = new Intent(Login.this,Principal.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account != null) {
//            startActivity(new Intent(Login.this, Principal.class));
//        }
        super.onStart();
        FirebaseUser user = autenticacao.getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(Login.this,Principal.class);
            startActivity(intent);
        }
    }

    //==============================================================================================

    private void verifyLogin() {
        final Usuario usuario = new Usuario();
        firebase.authWithPassword(
                usuario.getEmail(),
                usuario.getSenha(),
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(Login.this, "Erro ao acessar", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    public void telaUsuario(View view) {
        Intent intent = new Intent(Login.this, CadastroUsuario.class);
        startActivity(intent);
    }

    private void validaLogin() {
        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete( Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    identificadorUsuarioLogado = Base64Custom.codificarBase64(usuario.getEmail());

                    firebaseDataReferece = ConfiguracaoFireBase.getFireBase()
                            .child("usuario")
                            .child(identificadorUsuarioLogado);

                    valueEventListenerUsuario = new ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            Usuario usuarioRecuperado = dataSnapshot.getValue(Usuario.class);
                            Preferencia preferencias = new Preferencia(Login.this);
                            preferencias.salvarDados(identificadorUsuarioLogado, usuarioRecuperado.getNome());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    firebaseDataReferece.addListenerForSingleValueEvent(valueEventListenerUsuario);
                    Toast.makeText(Login.this, "Sucesso ao fazer login!", Toast.LENGTH_SHORT).show();
                    abrirTelaPrincipal();
                } else {
                    Toast.makeText(Login.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void verificaUsuarioLogado() {
        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
        if (autenticacao.getCurrentUser() != null) {
            abrirTelaPrincipal();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
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
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(Login.this, Principal.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

