package com.example.examplerxjava.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;
import androidx.room.rxjava3.RxRoom;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.example.examplerxjava.R;
import com.example.examplerxjava.adapter.PlaceAdapter;
import com.example.examplerxjava.databinding.ActivityMainBinding;
import com.example.examplerxjava.model.Place;
import com.example.examplerxjava.roomdb.PlaceDao;
import com.example.examplerxjava.roomdb.PlaceDatabase;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private PlaceDatabase db;
    private PlaceDao placeDao;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private PlaceAdapter placeAdapter;

    private SearchView searchView;

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
        placeAdapter = new PlaceAdapter(placeList);
        binding.recyclerView.setAdapter(placeAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.travel_menu, menu);

        MenuItem item = menu.findItem(R.id.search_all);
        searchView = (SearchView) item.getActionView();
        search();

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

    private void search() {

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<String> emitter) throws Throwable {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (!emitter.isDisposed()){
                            emitter.onNext(newText); //Girilen her terimden sonra Observer'ın onNext Metoduna düş
                        }

                        return false;

                    }
                });
            }
        })
                .distinctUntilChanged() //Önceden arama yapılan bir terim için tekrar arama yapma
                .debounce(200, TimeUnit.MILLISECONDS) //Girilen terimden sonra 200ms bekle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                        Log.d("@@@@", "Search: " + s);
                        if (placeAdapter != null) placeAdapter.getFilter().filter(s);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d("@@@@", "error search: " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("@@@@", "Complete search: " );

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
    }
}