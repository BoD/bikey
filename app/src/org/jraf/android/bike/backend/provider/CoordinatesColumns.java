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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Columns for the {@code coordinates} table.
 */
public class CoordinatesColumns implements BaseColumns {
    public static final String TABLE_NAME = "coordinates";
    public static final Uri CONTENT_URI = Uri.parse(BikeProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String RIDE_ID = "ride_id";
    public static final String RECORDED_DATE = "recorded_date";
    public static final String LAT_E6 = "lat_e6";
    public static final String LON_E6 = "lon_e6";

    public static final String DEFAULT_ORDER = _ID;
}