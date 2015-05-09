/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013-2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.backend.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

public class BikeySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = BikeySQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "bikey_provider.db";
    private static final int DATABASE_VERSION = 5;
    private static BikeySQLiteOpenHelper sInstance;
    private final Context mContext;
    private final BikeySQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_LOG = "CREATE TABLE IF NOT EXISTS "
            + LogColumns.TABLE_NAME + " ( "
            + LogColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LogColumns.RIDE_ID + " INTEGER NOT NULL, "
            + LogColumns.RECORDED_DATE + " INTEGER NOT NULL, "
            + LogColumns.LAT + " REAL NOT NULL, "
            + LogColumns.LON + " REAL NOT NULL, "
            + LogColumns.ELE + " REAL NOT NULL, "
            + LogColumns.LOG_DURATION + " INTEGER, "
            + LogColumns.LOG_DISTANCE + " REAL, "
            + LogColumns.SPEED + " REAL, "
            + LogColumns.CADENCE + " REAL, "
            + LogColumns.HEART_RATE + " INTEGER "
            + ", CONSTRAINT fk_ride_id FOREIGN KEY (" + LogColumns.RIDE_ID + ") REFERENCES ride (_id) ON DELETE CASCADE"
            + " );";

    public static final String SQL_CREATE_TABLE_RIDE = "CREATE TABLE IF NOT EXISTS "
            + RideColumns.TABLE_NAME + " ( "
            + RideColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RideColumns.NAME + " TEXT, "
            + RideColumns.CREATED_DATE + " INTEGER NOT NULL, "
            + RideColumns.STATE + " INTEGER NOT NULL, "
            + RideColumns.FIRST_ACTIVATED_DATE + " INTEGER, "
            + RideColumns.ACTIVATED_DATE + " INTEGER, "
            + RideColumns.DURATION + " INTEGER NOT NULL, "
            + RideColumns.DISTANCE + " REAL NOT NULL "
            + " );";

    // @formatter:on

    public static BikeySQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static BikeySQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static BikeySQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new BikeySQLiteOpenHelper(context);
    }

    private BikeySQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new BikeySQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static BikeySQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new BikeySQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private BikeySQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new BikeySQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_LOG);
        db.execSQL(SQL_CREATE_TABLE_RIDE);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
