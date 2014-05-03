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
package org.jraf.android.bikey.backend.export.kml;

import android.content.Context;
import android.net.Uri;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.util.datetime.DateTimeUtil;

/**
 * The ExtendedData element, which allows the user to tap on
 * a PlaceMark to bring up a popup with info about the ride.
 */
class RideExtendedData {

    private final String displayName;
    private final float totalDistance;
    private final long duration;
    private final float avgMovingSpeed;
    private final Context context;

    RideExtendedData(Context context, Uri rideUri) {
        this.context = context;
        displayName = RideManager.get().getDisplayName(rideUri);
        totalDistance = LogManager.get().getTotalDistance(rideUri);
        duration = RideManager.get().getDuration(rideUri);
        avgMovingSpeed = LogManager.get().getAverageMovingSpeed(rideUri);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(context.getString(R.string.export_kml_extended_data_begin)).append("\n");

        // The ride name
        result.append(context.getString(R.string.export_kml_extended_data_value, context.getString(R.string.export_kml_ride), displayName)).append("\n");

        // The distance
        result.append(
                context.getString(R.string.export_kml_extended_data_value, context.getString(R.string.display_title_distance),
                        UnitUtil.formatDistance(totalDistance, true))).append("\n");
        // The duration
        result.append(
                context.getString(R.string.export_kml_extended_data_value, context.getString(R.string.display_title_duration),
                        DateTimeUtil.formatDuration(context, duration))).append("\n");
        // Average moving speed.
        result.append(
                context.getString(R.string.export_kml_extended_data_value, context.getString(R.string.display_title_averageMovingSpeed),
                        UnitUtil.formatSpeed(avgMovingSpeed))).append("\n");
        result.append(context.getString(R.string.export_kml_extended_data_end)).append("\n");
        return result.toString();
    }
}