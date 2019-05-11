package com.modestie.modestieapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FreeCompanyDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ModestieApp.db";

    private static final String SQL_CREATE_FREECOMPANIES_TABLE =
            "CREATE TABLE " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME + " (" +
                    FreeCompanyReaderContract.FreeCompanyEntry._ID + " INTEGER PRIMARY KEY," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_LODESTONEID + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_NAME + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_TAG + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_SLOGAN + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTBACKGROUND + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTFRAME + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTLOGO + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_FORMED + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RANK + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYNAME + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYRANK + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYPROGRESS + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_MONTHLYRANKING + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_WEEKLYRANKING + " INTEGER," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATENAME + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATEPLOT + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ACTIVE + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RECRUITMENT + " TEXT," +
                    FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED + " INTEGER)";

    private static final String SQL_CREATE_MEMBERS_TABLE =
            "CREATE TABLE " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME + " (" +
                    FreeCompanyReaderContract.MemberEntry._ID + " INTEGER PRIMARY KEY," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_LODESTONEID + " INTEGER," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_NAME + " TEXT," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_AVATAR + " TEXT," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANK + " TEXT," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANKICON + " TEXT," +
                    FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_FEASTMATCHES + " INTEGER)";

    private static final String SQL_CREATE_FOCUS_TABLE =
            "CREATE TABLE " + FreeCompanyReaderContract.FocusEntry.TABLE_NAME + " (" +
                    FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_NAME + " TEXT," +
                    FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_ICON + " TEXT," +
                    FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_STATUS + " INTEGER)";

    private static final String SQL_CREATE_SEEKEDROLES_TABLE =
            "CREATE TABLE " + FreeCompanyReaderContract.SeekedRoleEntry.TABLE_NAME + " (" +
                    FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_NAME + " TEXT," +
                    FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_ICON + " TEXT," +
                    FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_STATUS + " INTEGER)";

    private static final String SQL_DELETE_FREECOMPANIES_TABLE =
            "DROP TABLE IF EXISTS " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME;

    private static final String SQL_DELETE_MEMBERS_TABLE =
            "DROP TABLE IF EXISTS " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME;

    private static final String SQL_DELETE_FOCUS_TABLE =
            "DROP TABLE IF EXISTS " + FreeCompanyReaderContract.FocusEntry.TABLE_NAME;

    private static final String SQL_DELETE_SEEKEDROLES_TABLE =
            "DROP TABLE IF EXISTS " + FreeCompanyReaderContract.SeekedRoleEntry.TABLE_NAME;

    public FreeCompanyDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_FREECOMPANIES_TABLE);
        db.execSQL(SQL_CREATE_MEMBERS_TABLE);
        db.execSQL(SQL_CREATE_FOCUS_TABLE);
        db.execSQL(SQL_CREATE_SEEKEDROLES_TABLE);
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
        db.execSQL(SQL_DELETE_FREECOMPANIES_TABLE);
        db.execSQL(SQL_DELETE_MEMBERS_TABLE);
        db.execSQL(SQL_DELETE_FOCUS_TABLE);
        db.execSQL(SQL_DELETE_SEEKEDROLES_TABLE);
        onCreate(db);
    }
}
