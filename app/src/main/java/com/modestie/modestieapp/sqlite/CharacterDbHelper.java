package com.modestie.modestieapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CharacterDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ModestieApp.db";

    private static final String SQL_CREATE_CHARACTER_UPDATES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + CharacterReaderContract.CharacterUpdateEntry.TABLE_NAME + " (" +
                    CharacterReaderContract.CharacterUpdateEntry._ID + " INTEGER PRIMARY KEY," +
                    CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_CHARACTER_ID + " INTEGER," +
                    CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_LAST_UPDATE + " INTEGER)";

    private static final String SQL_DELETE_CHARACTER_UPDATES_TABLE =
            "DROP TABLE IF EXISTS " + CharacterReaderContract.CharacterUpdateEntry.TABLE_NAME;

    public CharacterDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_CHARACTER_UPDATES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        resetDatabase(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void resetDatabase(SQLiteDatabase db)
    {
        db.execSQL(SQL_DELETE_CHARACTER_UPDATES_TABLE);
        onCreate(db);
    }
}
