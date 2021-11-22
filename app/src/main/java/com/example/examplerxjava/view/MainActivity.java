package com.example.examplerxjava.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.examplerxjava.R;
import com.example.examplerxjava.adapter.PlaceAdapter;
import com.example.examplerxjava.databinding.ActivityMainBinding;
import com.example.examplerxjava.model.Place;
import com.example.examplerxjava.roomdb.PlaceDao;
import com.example.examplerxjava.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private PlaceDatabase db;
    private PlaceDao placeDao;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        compositeDisposable.add(placeDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(MainActivity.this::handleResponse) //insert ile delete bana bişey döndürmüyordu ama getAll List<Place> döndürüyor. Bu listeyi, veriyi handleResponse metodunda işleyebiirim.
        );

    }

    private void handleResponse(List<Place> placeList) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdapter placeAdapter = new PlaceAdapter(placeList);
        binding.recyclerView.setAdapter(placeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.travel_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_place) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
    }
}