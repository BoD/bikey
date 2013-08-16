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
package org.jraf.android.bike.backend.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jraf.android.bike.Config;
import org.jraf.android.bike.Constants;

public class BikeSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.TAG + BikeSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "bike_provider.db";
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

    public BikeSQLiteOpenHelper(Context context) {
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

    // TODO REMOVE
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
