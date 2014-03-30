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

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.backend.provider.log.LogColumns;

public class BikeySQLiteUpgradeHelper {
    private static final String TAG = BikeySQLiteUpgradeHelper.class.getSimpleName();

    // @formatter:off
    private static final String SQL_UPGRADE_TABLE_LOG_2 = "ALTER TABLE "
            + LogColumns.TABLE_NAME
            + " ADD COLUMN "
            + LogColumns.CADENCE + " REAL "
            + " ;";

    // @formatter:on

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        int curVersion = oldVersion;
        while (curVersion < newVersion) {
            switch (curVersion) {
                case 1:
                    // 1 -> 2
                    db.execSQL(SQL_UPGRADE_TABLE_LOG_2);
                    curVersion = 2;
                    break;
            }
        }
    }
}
