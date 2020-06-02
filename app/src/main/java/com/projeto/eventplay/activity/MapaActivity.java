package com.projeto.eventplay.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.eventplay.FireBase.ConfiguracaoFireBase;
import com.projeto.eventplay.Helper.Preferencia;
import com.projeto.eventplay.Modelo.Evento;
import com.projeto.eventplay.Modelo.Local;
import com.projeto.eventplay.Modelo.Usuario;
import com.projeto.eventplay.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;

    private Local local;
    private Usuario usuario;
    private Evento evento;

    private LatLng localUsuario;

    private LatLng localEvento;

    private Marker marcadorEvento;

    private int raio;
    private DatabaseReference eventosPublicosRef;
    private List<Evento> listEventos = new ArrayList<>();
    private DecimalFormat df;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastKnowLocation;
    private List<Marker> listMarker = new ArrayList<>();
    private Preferencia preferencia;
    private String origem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        preferencia = new Preferencia(MapaActivity.this);
        inicializarComponentes();

        //marcador();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (origem.equals("Principal")) {
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (origem.equals("Principal")) {
                    for (Marker marcacao : listMarker) {
                        if (marcacao.getId().equals(marker.getId())) {
                            for (Evento evento : listEventos) {
                                if (evento.getId().equals(marcacao.getTag())) {
                                    preferencia.setEventoSelecionado(evento);
                                    Intent i = new Intent(getApplicationContext(), DetalhesProduto.class);
                                    startActivity(i);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });


        //Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //recuperarLocalizacaoEvento();
        recuperarLocalizacaoUsuario();

    }

    public void inicializarComponentes() {

        usuarioAutenticacao = ConfiguracaoFireBase.getFirebaseAutenticaticacao();

        toolbar = (Toolbar) findViewById(R.id.toolbarPrincipal);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left_white);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            origem = extra.getString("origem");
            raio = extra.getInt("raio");
        }
        //recuperarEventosPorCategoria();
        //Toast.makeText(MapaActivity.this,"Raio: "+ raio,Toast.LENGTH_SHORT).show();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    //=============================== MENU =========================================================

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
                builder.setMessage("Você deseja realmente sair do Event Play?");
                builder.setPositiveButton("Sair", new DialogInterface.OnClickListener() {

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
        Intent intent = new Intent(MapaActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void abrirMeusEventosCriados() {
        Intent intent = new Intent(MapaActivity.this, MeusEventosCriados.class);
        startActivity(intent);

    }

    private void abrirContato() {
        Intent intent = new Intent(MapaActivity.this, Contato.class);
        startActivity(intent);
    }

    private void abrirAjuda() {
        Intent intent = new Intent(MapaActivity.this, Ajuda.class);
        startActivity(intent);
    }

    private void abrirFavoritos() {
        Intent intent = new Intent(MapaActivity.this, Favoritos.class);
        startActivity(intent);
    }

    //====================== Localização do Evento ====================================================


//    private void recuperarLocalizacaoEvento() {
//
//
//
////        LatLng localServico = new LatLng(latServico,longServico);
////        mMap.addMarker(new MarkerOptions()
////                .position(localServico)
////                .title(tituloServico)
////                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_flag)));
//
//
////        //exibe marcador do evento
////        adicionarMarcadorEvento(localEvento, evento.getNomeEn());
////
////        //exibe marcador do evento
////        LatLng localEvento = new LatLng(
////                Double.parseDouble(local.getLatitude()),
////                Double.parseDouble(local.getLongitude())
////        );
////        adicionarMarcadorEvento(localEvento, "Evento");
//
//    }

//    private void adicionarMarcadorEvento (LatLng localizacao, String titulo){
//
//        marcadorEvento = mMap.addMarker(
//                new MarkerOptions()
//                        .position(localizacao)
//                        .title(titulo)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icone_evento))
//        );
//
//    }


    //===================================Localização do usuario=====================================
    private void recuperarLocalizacaoUsuario() {

        obtainLastKnowLocation();
        //Solicitar atualizações de localização
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
//            locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    10000,
//                    10,
//                    locationListener
//            );
//        }
    }

    private void obtainLastKnowLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastKnowLocation = task.getResult();
                            localUsuario = new LatLng(mLastKnowLocation.getLatitude(), mLastKnowLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(localUsuario)
                                    .title("Você")
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_usuario_local)));

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 11));
                            if (origem.equals("Principal")) {
                                recuperarEventosPorCategoria();
                            } else if (origem.equals("DetalhesProduto")) {
                                recuperaEventoNoMapa();
                            }
                        } else {
                            mLastKnowLocation = null;
                            Toast.makeText(getApplicationContext(), "Não foi possivel obter a localização!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void recuperaEventoNoMapa() {
        Evento evento = preferencia.getEventoSelecionado();
        Local local = evento.getLocal();
        LatLng localEvento = new LatLng(Float.valueOf(local.getLatitude()),Float.valueOf(local.getLongitude()));

        mMap.addMarker(new MarkerOptions()
        .position(localEvento)
        .title(evento.getNomeEn())
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
        .showInfoWindow();

    }

    public void recuperarEventosPorCategoria(){

        //Configura nó por Categoria

        eventosPublicosRef = ConfiguracaoFireBase.getFireBase()
                .child("eventos");

        eventosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listEventos.clear();
                for (DataSnapshot eventos: dataSnapshot.getChildren() ) {
                    String data = eventos.getKey();
                    for(DataSnapshot dt: dataSnapshot.child(data).getChildren()){
                        Evento evento = dt.getValue(Evento.class);
                        try{
                            Local localEvento = evento.getLocal();
                            float[] results = new float[1];
                            Location.distanceBetween(localUsuario.latitude, localUsuario.longitude,Float.valueOf(localEvento.getLatitude()),Float.valueOf(localEvento.getLongitude()),results);
                            float kmFormatado = results[0]/1000; //df.format(results[0]/1000);
                            if(kmFormatado<=raio){
                                listEventos.add(evento);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

                //Marcar no mapa
                for(Evento evento: listEventos){
                    Local local = evento.getLocal();
                    LatLng localEvento = new LatLng(Float.valueOf(local.getLatitude()),Float.valueOf(local.getLongitude()));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(localEvento)
                    .title(evento.getNomeEn())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    marker.setTag(evento.getId());
                    listMarker.add(marker);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

