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
package org.jraf.android.bikey.backend.provider.log;

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

/**
 * Columns for the {@code log} table.
 */
public class LogColumns implements BaseColumns {
    public static final String TABLE_NAME = "log";
    public static final Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
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
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            RIDE_ID,
            RECORDED_DATE,
            LAT,
            LON,
            ELE,
            LOG_DURATION,
            LOG_DISTANCE,
            SPEED,
            CADENCE,
            HEART_RATE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(RIDE_ID) || c.contains("." + RIDE_ID)) return true;
            if (c.equals(RECORDED_DATE) || c.contains("." + RECORDED_DATE)) return true;
            if (c.equals(LAT) || c.contains("." + LAT)) return true;
            if (c.equals(LON) || c.contains("." + LON)) return true;
            if (c.equals(ELE) || c.contains("." + ELE)) return true;
            if (c.equals(LOG_DURATION) || c.contains("." + LOG_DURATION)) return true;
            if (c.equals(LOG_DISTANCE) || c.contains("." + LOG_DISTANCE)) return true;
            if (c.equals(SPEED) || c.contains("." + SPEED)) return true;
            if (c.equals(CADENCE) || c.contains("." + CADENCE)) return true;
            if (c.equals(HEART_RATE) || c.contains("." + HEART_RATE)) return true;
        }
        return false;
    }

    public static final String PREFIX_RIDE = TABLE_NAME + "__" + RideColumns.TABLE_NAME;
}
