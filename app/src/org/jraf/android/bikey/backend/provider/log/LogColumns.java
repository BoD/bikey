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
            SPEED
    };
    // @formatter:on
}