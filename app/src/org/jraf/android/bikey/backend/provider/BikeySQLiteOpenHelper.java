/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

public class BikeySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = BikeySQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "bikey_provider.db";
    private static final int DATABASE_VERSION = 3;

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
            + LogColumns.SPEED + " REAL, "
            + LogColumns.CADENCE + " REAL "
            + ", CONSTRAINT FK_RIDE FOREIGN KEY (RIDE_ID) REFERENCES RIDE (_ID) ON DELETE CASCADE"
            + " );";

    private static final String SQL_CREATE_TABLE_RIDE = "CREATE TABLE IF NOT EXISTS "
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

    public static BikeySQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */

    private static BikeySQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new BikeySQLiteOpenHelper(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
    }

    private BikeySQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    /*
     * Post Honeycomb.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static BikeySQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new BikeySQLiteOpenHelper(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private BikeySQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
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
        new BikeySQLiteUpgradeHelper().onUpgrade(db, oldVersion, newVersion);
    }
}
