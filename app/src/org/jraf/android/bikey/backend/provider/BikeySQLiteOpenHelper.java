/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.backend.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

public class BikeySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = BikeySQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "bikey_provider.db";
    private static final int DATABASE_VERSION = 1;

    // @formatter:off
    private static final String SQL_CREATE_TABLE_LOG = "CREATE TABLE IF NOT EXISTS "
            + LogColumns.TABLE_NAME + " ( "
            + LogColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LogColumns.RIDE_ID + " INTEGER NOT NULL, "
            + LogColumns.RECORDED_DATE + " INTEGER NOT NULL, "
            + LogColumns.LAT + " REAL NOT NULL, "
            + LogColumns.LON + " REAL NOT NULL, "
            + LogColumns.ELE + " REAL NOT NULL, "
            + LogColumns.DURATION + " INTEGER, "
            + LogColumns.DISTANCE + " REAL, "
            + LogColumns.SPEED + " REAL "
            + ", CONSTRAINT FK_RIDE FOREIGN KEY (RIDE_ID) REFERENCES RIDE (_ID) ON DELETE CASCADE"
            + " );";

    private static final String SQL_CREATE_TABLE_RIDE = "CREATE TABLE IF NOT EXISTS "
            + RideColumns.TABLE_NAME + " ( "
            + RideColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RideColumns.NAME + " TEXT, "
            + RideColumns.CREATED_DATE + " INTEGER NOT NULL, "
            + RideColumns.STATE + " INTEGER NOT NULL, "
            + RideColumns.ACTIVATED_DATE + " INTEGER, "
            + RideColumns.DURATION + " INTEGER NOT NULL, "
            + RideColumns.DISTANCE + " REAL NOT NULL "
            + " );";

    // @formatter:on

    public BikeySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BikeySQLiteOpenHelper(Context context, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_TABLE_LOG);
        db.execSQL(SQL_CREATE_TABLE_RIDE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
}
