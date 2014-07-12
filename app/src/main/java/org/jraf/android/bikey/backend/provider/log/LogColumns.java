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
package org.jraf.android.bikey.backend.provider.log;

import java.util.HashSet;
import java.util.Set;

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.bikey.backend.provider.BikeyProvider;

/**
 * Columns for the {@code log} table.
 */
public class LogColumns implements BaseColumns {
    public static final String TABLE_NAME = "log";
    public static final Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;
    public static final String RIDE_ID = "ride_id";
    public static final String RECORDED_DATE = "recorded_date";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String ELE = "ele";
    public static final String LOG_DURATION = "log_duration";
    public static final String LOG_DISTANCE = "log_distance";
    public static final String SPEED = "speed";
    public static final String CADENCE = "cadence";
    public static final String HEART_RATE = "heart_rate";

    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] FULL_PROJECTION = new String[] {
            TABLE_NAME + "." + _ID + " AS " + BaseColumns._ID,
            TABLE_NAME + "." + RIDE_ID,
            TABLE_NAME + "." + RECORDED_DATE,
            TABLE_NAME + "." + LAT,
            TABLE_NAME + "." + LON,
            TABLE_NAME + "." + ELE,
            TABLE_NAME + "." + LOG_DURATION,
            TABLE_NAME + "." + LOG_DISTANCE,
            TABLE_NAME + "." + SPEED,
            TABLE_NAME + "." + CADENCE,
            TABLE_NAME + "." + HEART_RATE
    };
    // @formatter:on

    private static final Set<String> ALL_COLUMNS = new HashSet<String>();
    static {
        ALL_COLUMNS.add(_ID);
        ALL_COLUMNS.add(RIDE_ID);
        ALL_COLUMNS.add(RECORDED_DATE);
        ALL_COLUMNS.add(LAT);
        ALL_COLUMNS.add(LON);
        ALL_COLUMNS.add(ELE);
        ALL_COLUMNS.add(LOG_DURATION);
        ALL_COLUMNS.add(LOG_DISTANCE);
        ALL_COLUMNS.add(SPEED);
        ALL_COLUMNS.add(CADENCE);
        ALL_COLUMNS.add(HEART_RATE);
    }

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (ALL_COLUMNS.contains(c)) return true;
        }
        return false;
    }
}
