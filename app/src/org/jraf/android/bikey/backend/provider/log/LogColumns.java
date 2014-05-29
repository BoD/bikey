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

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.bikey.backend.provider.BikeyProvider;

/**
 * Columns for the {@code log} table.
 */
public interface LogColumns extends BaseColumns {
    String TABLE_NAME = "log";
    Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    String _ID = BaseColumns._ID;
    String RIDE_ID = "ride_id";
    String RECORDED_DATE = "recorded_date";
    String LAT = "lat";
    String LON = "lon";
    String ELE = "ele";
    String DURATION = "duration";
    String DISTANCE = "distance";
    String SPEED = "speed";
    String CADENCE = "cadence";
    String HEART_RATE = "heart_rate";

    String DEFAULT_ORDER = _ID;

	// @formatter:off
    String[] FULL_PROJECTION = new String[] {
            _ID,
            RIDE_ID,
            RECORDED_DATE,
            LAT,
            LON,
            ELE,
            DURATION,
            DISTANCE,
            SPEED,
            CADENCE,
            HEART_RATE
    };
    // @formatter:on
}