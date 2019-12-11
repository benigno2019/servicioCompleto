package com.example.serviciofrontend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDatos extends SQLiteOpenHelper {

    public BaseDatos(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Actividades(nombre varchar(200), prioridad integer, nomImgSon varchar(50))");
        db.execSQL("INSERT INTO Actividades VALUES('Agua',0,'agua')");
        db.execSQL("INSERT INTO Actividades VALUES('Baño',0,'bano')");
        db.execSQL("INSERT INTO Actividades VALUES('Me Ahogo',0,'me_ahogo')");
        db.execSQL("INSERT INTO Actividades VALUES('Doctor',0,'Doctor')");
        db.execSQL("INSERT INTO Actividades VALUES('Si',0,'Si')");
        db.execSQL("INSERT INTO Actividades VALUES('No',0,'No')");
        db.execSQL("INSERT INTO Actividades VALUES('Platano',0,'Platano')");
        db.execSQL("INSERT INTO Actividades VALUES('Mango',0,'Mango')");
        db.execSQL("INSERT INTO Actividades VALUES('Zapato',0,'Zapato')");
        db.execSQL("INSERT INTO Actividades VALUES('Rascar',0,'Rascar')");
        db.execSQL("INSERT INTO Actividades VALUES('Leche',0,'Leche')");
        db.execSQL("INSERT INTO Actividades VALUES('Papaya',0,'Papaya')");
        db.execSQL("INSERT INTO Actividades VALUES('Fresas',0,'Fresas')");
        db.execSQL("INSERT INTO Actividades VALUES('Silla',0,'Silla')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}