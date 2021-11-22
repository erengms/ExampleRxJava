package com.example.examplerxjava.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.examplerxjava.R;
import com.example.examplerxjava.model.Place;
import com.example.examplerxjava.roomdb.PlaceDao;
import com.example.examplerxjava.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.examplerxjava.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private ActivityResultLauncher<String> permissionLauncher;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private SharedPreferences sharedPreferences;
    private boolean info;

    private PlaceDatabase db;
    private PlaceDao placeDao;

    private double selectedLatitude;
    private double selectedLongitude;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

         sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.examplerxjava", MODE_PRIVATE);
         info = false;

         db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build(); //Places database ismi
         placeDao = db.placeDao();

         selectedLatitude = 0.0;
         selectedLongitude = 0.0;

         binding.saveButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //veri tabanı işlemlerini main thread'de yapma
                Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);

                //thread -> Main thread (UI), Default (CPU Intensive), IO Thread(network-internetten bir veri istemek gibi- , database)
                //placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); //io thread de yap

                 //disposable mantığıyla yapacagız, daha verimli
                 compositeDisposable.add(placeDao.insert(place)
                         .subscribeOn(Schedulers.io()) //io threadde çalış
                         .observeOn(AndroidSchedulers.mainThread()) //main threadde gözlemle
                         .subscribe(MapsActivity.this::handleResponse) //işlem bittiğinde bir metodu çalıştır. handleResponse()
                 );

             }
         });

         binding.deleteButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {


                /* compositeDisposable.delete(placeDao.delete()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(MapsActivity.this::handleResponse)
                 );*/

             }
         });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);//long click'i set edelim

        binding.saveButton.setEnabled(false);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                //shared pref'e yazalım, location her değiştiğinde kamera oraya kaymasın
                info = sharedPreferences.getBoolean("info", false);
                if (!info){
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                    sharedPreferences.edit().putBoolean("info", true).apply();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.getRoot(), "Haritalar için izin vermelisiniz", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Tamam", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // requestPermission
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        }).show();
            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0 , 0, locationListener);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null){
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 13));
            }

            mMap.setMyLocationEnabled(true); // konumumuzu mavi nokta olarak haritada gösterir.
        }


    }

    private void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //permission granted
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0 , 0, locationListener);

                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null){
                            LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 13));
                        }
                    }

                } else {
                    Toast.makeText(MapsActivity.this, "Konum izni vermeniz gerekir.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear(); //haritayı temizle, eski markerler silinir
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);
    }

    private void handleResponse(){
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
    }
}