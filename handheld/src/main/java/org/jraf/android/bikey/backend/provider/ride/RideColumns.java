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
package org.jraf.android.bikey.backend.provider.ride;

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

/**
 * Columns for the {@code ride} table.
 */
public class RideColumns implements BaseColumns {
    public static final String TABLE_NAME = "ride";
    public static final Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String NAME = "name";

    public static final String CREATED_DATE = "created_date";

    public static final String STATE = "state";

    public static final String FIRST_ACTIVATED_DATE = "first_activated_date";

    public static final String ACTIVATED_DATE = "activated_date";

    public static final String DURATION = "duration";

    public static final String DISTANCE = "distance";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            NAME,
            CREATED_DATE,
            STATE,
            FIRST_ACTIVATED_DATE,
            ACTIVATED_DATE,
            DURATION,
            DISTANCE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(CREATED_DATE) || c.contains("." + CREATED_DATE)) return true;
            if (c.equals(STATE) || c.contains("." + STATE)) return true;
            if (c.equals(FIRST_ACTIVATED_DATE) || c.contains("." + FIRST_ACTIVATED_DATE)) return true;
            if (c.equals(ACTIVATED_DATE) || c.contains("." + ACTIVATED_DATE)) return true;
            if (c.equals(DURATION) || c.contains("." + DURATION)) return true;
            if (c.equals(DISTANCE) || c.contains("." + DISTANCE)) return true;
        }
        return false;
    }

}
