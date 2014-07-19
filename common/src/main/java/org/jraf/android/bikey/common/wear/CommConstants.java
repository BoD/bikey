/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.common.wear;

public class CommConstants {
    /*
     * Ride.
     */

    public static final String PATH_RIDE = "/ride";

    /**
     * Indicates whether a ride is currently ongoing ({@code boolean}).
     */
    public static final String PATH_RIDE_ONGOING = PATH_RIDE + "/ongoing";

    /**
     * Measurement values ({@code boolean}).
     */
    public static final String PATH_RIDE_VALUES = PATH_RIDE + "/values";


    /**
     * Start date offset ({@code long}). To get the current duration of the ride, add {@code System.currentTimeMillis()} to this value.
     */
    public static final String EXTRA_START_DATE_OFFSET = "EXTRA_START_DATE_OFFSET";

    /**
     * Current speed ({@code float}).
     */
    public static final String EXTRA_SPEED = "EXTRA_SPEED";

    /**
     * Total distance ({@code float}).
     */
    public static final String EXTRA_DISTANCE = "EXTRA_DISTANCE";

    /**
     * Current heart rate ({@code int}).
     */
    public static final String EXTRA_HEART_RATE = "EXTRA_HEART_RATE";

    /**
     * All-purpose value.
     */
    public static final String EXTRA_VALUE = "EXTRA_VALUE";


    /*
     * Preferences.
     */

    public static final String PATH_PREFERENCES = "/preferences";
    public static final String EXTRA_UNITS = "EXTRA_UNITS";
}
