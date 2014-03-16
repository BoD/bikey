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

import java.util.Date;

import org.jraf.android.bikey.backend.provider.base.AbstractContentValuesWrapper;

/**
 * Content values wrapper for the {@code log} table.
 */
public class LogContentValues extends AbstractContentValuesWrapper {

    public LogContentValues putRideId(long value) {
        mContentValues.put(LogColumns.RIDE_ID, value);
        return this;
    }



    public LogContentValues putRecordedDate(Date value) {
        if (value == null) throw new IllegalArgumentException("value for recordedDate must not be null");
        mContentValues.put(LogColumns.RECORDED_DATE, value.getTime());
        return this;
    }


    public LogContentValues putRecordedDate(long value) {
        mContentValues.put(LogColumns.RECORDED_DATE, value);
        return this;
    }


    public LogContentValues putLat(double value) {
        mContentValues.put(LogColumns.LAT, value);
        return this;
    }



    public LogContentValues putLon(double value) {
        mContentValues.put(LogColumns.LON, value);
        return this;
    }



    public LogContentValues putEle(double value) {
        mContentValues.put(LogColumns.ELE, value);
        return this;
    }



    public LogContentValues putDuration(Long value) {
        mContentValues.put(LogColumns.DURATION, value);
        return this;
    }

    public LogContentValues putDurationNull() {
        mContentValues.putNull(LogColumns.DURATION);
        return this;
    }


    public LogContentValues putDistance(Float value) {
        mContentValues.put(LogColumns.DISTANCE, value);
        return this;
    }

    public LogContentValues putDistanceNull() {
        mContentValues.putNull(LogColumns.DISTANCE);
        return this;
    }


    public LogContentValues putSpeed(Float value) {
        mContentValues.put(LogColumns.SPEED, value);
        return this;
    }

    public LogContentValues putSpeedNull() {
        mContentValues.putNull(LogColumns.SPEED);
        return this;
    }

}
