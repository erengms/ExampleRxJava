package com.example.examplerxjava.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.examplerxjava.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    //Completable, Flowable RxJava'dan gelen özellik

    @Insert
    public Completable insert(Place place);

    @Delete
    public Completable delete(Place place);

    @Query("SELECT *  FROM Place") //Liste döndürecek
    public Flowable<List<Place>>  getAll();

    /*@Query("SELECT *  FROM Place WHERE name = :nameInput") //Filtreleme yapabiliriz getAll("test"); ismi "test" olanları getir gibi.
    List<Place> getAll(String nameInput); */
}
