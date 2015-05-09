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

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jraf.android.bikey.backend.provider.base.AbstractCursor;
import org.jraf.android.bikey.backend.provider.ride.*;

/**
 * Cursor wrapper for the {@code log} table.
 */
public class LogCursor extends AbstractCursor implements LogModel {
    public LogCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(LogColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code ride_id} value.
     */
    public long getRideId() {
        Long res = getLongOrNull(LogColumns.RIDE_ID);
        if (res == null)
            throw new NullPointerException("The value of 'ride_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code name} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getRideName() {
        String res = getStringOrNull(RideColumns.NAME);
        return res;
    }

    /**
     * Get the {@code created_date} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public Date getRideCreatedDate() {
        Date res = getDateOrNull(RideColumns.CREATED_DATE);
        if (res == null)
            throw new NullPointerException("The value of 'created_date' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code state} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public RideState getRideState() {
        Integer intValue = getIntegerOrNull(RideColumns.STATE);
        if (intValue == null)
            throw new NullPointerException("The value of 'state' in the database was null, which is not allowed according to the model definition");
        return RideState.values()[intValue];
    }

    /**
     * Get the {@code first_activated_date} value.
     * Can be {@code null}.
     */
    @Nullable
    public Date getRideFirstActivatedDate() {
        Date res = getDateOrNull(RideColumns.FIRST_ACTIVATED_DATE);
        return res;
    }

    /**
     * Get the {@code activated_date} value.
     * Can be {@code null}.
     */
    @Nullable
    public Date getRideActivatedDate() {
        Date res = getDateOrNull(RideColumns.ACTIVATED_DATE);
        return res;
    }

    /**
     * Get the {@code duration} value.
     */
    public long getRideDuration() {
        Long res = getLongOrNull(RideColumns.DURATION);
        if (res == null)
            throw new NullPointerException("The value of 'duration' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code distance} value.
     */
    public float getRideDistance() {
        Float res = getFloatOrNull(RideColumns.DISTANCE);
        if (res == null)
            throw new NullPointerException("The value of 'distance' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code recorded_date} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public Date getRecordedDate() {
        Date res = getDateOrNull(LogColumns.RECORDED_DATE);
        if (res == null)
            throw new NullPointerException("The value of 'recorded_date' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code lat} value.
     */
    public double getLat() {
        Double res = getDoubleOrNull(LogColumns.LAT);
        if (res == null)
            throw new NullPointerException("The value of 'lat' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code lon} value.
     */
    public double getLon() {
        Double res = getDoubleOrNull(LogColumns.LON);
        if (res == null)
            throw new NullPointerException("The value of 'lon' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code ele} value.
     */
    public double getEle() {
        Double res = getDoubleOrNull(LogColumns.ELE);
        if (res == null)
            throw new NullPointerException("The value of 'ele' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code log_duration} value.
     * Can be {@code null}.
     */
    @Nullable
    public Long getLogDuration() {
        Long res = getLongOrNull(LogColumns.LOG_DURATION);
        return res;
    }

    /**
     * Get the {@code log_distance} value.
     * Can be {@code null}.
     */
    @Nullable
    public Float getLogDistance() {
        Float res = getFloatOrNull(LogColumns.LOG_DISTANCE);
        return res;
    }

    /**
     * Get the {@code speed} value.
     * Can be {@code null}.
     */
    @Nullable
    public Float getSpeed() {
        Float res = getFloatOrNull(LogColumns.SPEED);
        return res;
    }

    /**
     * Get the {@code cadence} value.
     * Can be {@code null}.
     */
    @Nullable
    public Float getCadence() {
        Float res = getFloatOrNull(LogColumns.CADENCE);
        return res;
    }

    /**
     * Get the {@code heart_rate} value.
     * Can be {@code null}.
     */
    @Nullable
    public Integer getHeartRate() {
        Integer res = getIntegerOrNull(LogColumns.HEART_RATE);
        return res;
    }
}
