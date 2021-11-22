package com.example.examplerxjava.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.examplerxjava.model.Place;

import java.util.List;

@Dao
public interface PlaceDao {

    @Insert
    void insert(Place place);

    @Delete
    void delete(Place place);

    @Query("SELECT *  FROM Place") //Liste döndürecek
    List<Place> getAll();

    /*@Query("SELECT *  FROM Place WHERE name = :nameInput") //Filtreleme yapabiliriz getAll("test"); ismi "test" olanları getir gibi.
    List<Place> getAll(String nameInput); */
}
