package com.abkv.choseone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;

    private static volatile DbHelper mInstance = null;

    private static final String NAME_DATABASE = "CHOSE_ONE.db";
    private static final String CREATE_TABLE = "create table " + VisitedPlaces.TABLE_NAME + "(" +
            VisitedPlaces.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            VisitedPlaces.TIMESTAMP + " INTEGER NOT NULL," +
            VisitedPlaces.NAME + " TEXT NOT NULL," +
            VisitedPlaces.ADDRESS + " TEXT NOT NULL," +
            VisitedPlaces.PLACE_ID + " TEXT NOT NULL," +
            VisitedPlaces.COMMENT + " TEXT" +
            ")";

    public static DbHelper getInstance(Context context)
    {
        if (null == mInstance)
        {
            synchronized (DbHelper.class)
            {
                if (null == mInstance)
                {
                    mInstance = new DbHelper(context, NAME_DATABASE, null, DATABASE_VERSION);
                }
            }
        }

        return mInstance;
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + VisitedPlaces.TABLE_NAME);
        onCreate(db);
    }
}
