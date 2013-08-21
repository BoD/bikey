package org.jraf.android.bikey.backend.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jraf.android.bikey.Config;
import org.jraf.android.bikey.Constants;

public class BikeySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.TAG + BikeySQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "bikey_provider.db";
    private static final int DATABASE_VERSION = 1;

    // @formatter:off
    private static final String SQL_CREATE_TABLE_RIDE = "CREATE TABLE IF NOT EXISTS "
            + RideColumns.TABLE_NAME + " ( "
            + RideColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RideColumns.NAME + " TEXT, "
            + RideColumns.CREATED_DATE + " INTEGER, "
            + RideColumns.STATE + " INTEGER, "
            + RideColumns.ACTIVATED_DATE + " INTEGER, "
            + RideColumns.DURATION + " INTEGER, "
            + RideColumns.DISTANCE + " FLOAT "
            + " );";

    private static final String SQL_CREATE_TABLE_LOG = "CREATE TABLE IF NOT EXISTS "
            + LogColumns.TABLE_NAME + " ( "
            + LogColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LogColumns.RIDE_ID + " INTEGER, "
            + LogColumns.RECORDED_DATE + " INTEGER, "
            + LogColumns.LAT + " FLOAT, "
            + LogColumns.LON + " FLOAT, "
            + LogColumns.ELE + " FLOAT, "
            + LogColumns.DURATION + " INTEGER, "
            + LogColumns.DISTANCE + " FLOAT, "
            + LogColumns.SPEED + " FLOAT "
            + " );";

    // @formatter:on

    public BikeySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (Config.LOGD_PROVIDER) Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_TABLE_RIDE);
        db.execSQL(SQL_CREATE_TABLE_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Config.LOGD_PROVIDER) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
}
