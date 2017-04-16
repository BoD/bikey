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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Mock the database. This allows us to test on a test database,
 * without touching the real user database.
 */
public class TestBikeySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = TestBikeySQLiteOpenHelper.class.getSimpleName();

    public TestBikeySQLiteOpenHelper(Context context) {
        super(context, "test_" + BikeyProviderSQLiteOpenHelper.DATABASE_FILE_NAME, null, 1);
    }

    /**
     * This implementation could be replaced by an implementation
     * which reads a text file with sql table creation and insertion
     * statements. This would be useful if we want to test with a set
     * of specific rides already loaded in the database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BikeyProviderSQLiteOpenHelper.SQL_CREATE_TABLE_LOG);
        db.execSQL(BikeyProviderSQLiteOpenHelper.SQL_CREATE_TABLE_RIDE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
