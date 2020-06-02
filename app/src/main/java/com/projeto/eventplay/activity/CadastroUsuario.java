package com.projeto.eventplay.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Base64Custom;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Modelo.Usuario;
import com.projeto.eventplay.R;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroUsuario extends AppCompatActivity {

    private TextView nome;
    private TextView dataNasc;
    private TextView cpf;
    private TextView email;
    private TextView senha;

    private Toolbar toolbar;

    //Cadastro
    private Button botaoCadastrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    //Imagens
    private CircleImageView imagemView;
    private Uri filePath;


    private final int PICK_IMAGE_REQUEST = 71;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);

        toolbar = (Toolbar) findViewById(R.id.toolbarGenerica);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        //Firebase Init
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        nome = (TextView) findViewById(R.id.nomeId);
        dataNasc = (TextView) findViewById(R.id.dataNascId);
        cpf = (TextView) findViewById(R.id.cpfId);
        email = (TextView) findViewById(R.id.emailId);
        senha = (TextView) findViewById(R.id.senhaId);
        botaoCadastrar = (Button) findViewById(R.id.cadastrarId);
        imagemView = (CircleImageView) findViewById(R.id.imageUsuarioId);

        imagemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        SimpleMaskFormatter simpleMaskCodData = new SimpleMaskFormatter("NN/NN/NNNN");
        SimpleMaskFormatter simpleMaskCodCpf = new SimpleMaskFormatter("NNN.NNN.NNN-NN");

        MaskTextWatcher maskData = new MaskTextWatcher(dataNasc, simpleMaskCodData);
        MaskTextWatcher maskCpf = new MaskTextWatcher(cpf, simpleMaskCodCpf);

        dataNasc.addTextChangedListener(maskData);
        cpf.addTextChangedListener(maskCpf);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuario = new Usuario();
                usuario.setNome(nome.getText().toString());
                usuario.setDataNasc(dataNasc.getText().toString());
                usuario.setCpf(cpf.getText().toString());
                usuario.setEmail(email.getText().toString());
                usuario.setSenha(senha.getText().toString());

                cadastrarUsuario();
            }
        });

    }

    private void cadastrarUsuario(){
        // ATENÇÃO: Mudei a maneira de como vc estava cadastrando, da sua maneira estava fazendo com que as vezes o usuário não era salvo no firebase.
        if(usuario.getNome().isEmpty() || usuario.getDataNasc().isEmpty() || usuario.getCpf().isEmpty() || usuario.getEmail().isEmpty() || usuario.getSenha().isEmpty()){
            Toast.makeText(CadastroUsuario.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
        }
        else{
            autenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();
            autenticacao.createUserWithEmailAndPassword(
                    usuario.getEmail(),
                    usuario.getSenha()
            ).addOnCompleteListener(CadastroUsuario.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificadorUsuario);
                    uploadImage();

                    Preferencia preferencias = new Preferencia(CadastroUsuario.this);
                    preferencias.salvarDados(identificadorUsuario, usuario.getNome());

                    DatabaseReference referenceFirebase = ConfiguracaoFireBase.getFireBase();
                    referenceFirebase.child("usuario").child(identificadorUsuario).setValue(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CadastroUsuario.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                            abrirLogin();
                            autenticacao.signOut();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = e.getMessage();
                            Toast.makeText(CadastroUsuario.this, "Erro: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });


                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String error = e.getMessage();
                    Toast.makeText(CadastroUsuario.this, "Erro: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImage(){
        if(filePath!=null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Carregando...");
            progressDialog.show();

            StorageReference ref = storageReference.child("imagem/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(CadastroUsuario.this, "Carregou", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CadastroUsuario.this, "Falhou" +e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Carregou"+(int)progress+"%");
                        }
                    });
        }
    }

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void abrirLogin (){
        Intent intent = new Intent (CadastroUsuario.this, Login.class);
        startActivity( intent );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST &&  resultCode == RESULT_OK
                && data!= null && data.getData() != null ){

            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imagemView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
