package com.example.examplerxjava.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.examplerxjava.model.Place;

@Database(entities = {Place.class}, version = 1) //{Modellerimizi veriyoruz}, database versiyon numarası
public abstract class PlaceDatabase extends RoomDatabase {

    public abstract PlaceDao placeDao();
}
