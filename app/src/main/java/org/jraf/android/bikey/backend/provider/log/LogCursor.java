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

import java.util.Date;

import android.database.Cursor;

import org.jraf.android.bikey.backend.provider.base.AbstractCursor;
import org.jraf.android.bikey.backend.provider.ride.*;

/**
 * Cursor wrapper for the {@code log} table.
 */
public class LogCursor extends AbstractCursor {
    public LogCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Get the {@code ride_id} value.
     */
    public long getRideId() {
        return getLongOrNull(LogColumns.RIDE_ID);
    }

    /**
     * Get the {@code recorded_date} value.
     * Cannot be {@code null}.
     */
    public Date getRecordedDate() {
        return getDate(LogColumns.RECORDED_DATE);
    }

    /**
     * Get the {@code lat} value.
     */
    public double getLat() {
        return getDoubleOrNull(LogColumns.LAT);
    }

    /**
     * Get the {@code lon} value.
     */
    public double getLon() {
        return getDoubleOrNull(LogColumns.LON);
    }

    /**
     * Get the {@code ele} value.
     */
    public double getEle() {
        return getDoubleOrNull(LogColumns.ELE);
    }

    /**
     * Get the {@code log_duration} value.
     * Can be {@code null}.
     */
    public Long getLogDuration() {
        return getLongOrNull(LogColumns.LOG_DURATION);
    }

    /**
     * Get the {@code log_distance} value.
     * Can be {@code null}.
     */
    public Float getLogDistance() {
        return getFloatOrNull(LogColumns.LOG_DISTANCE);
    }

    /**
     * Get the {@code speed} value.
     * Can be {@code null}.
     */
    public Float getSpeed() {
        return getFloatOrNull(LogColumns.SPEED);
    }

    /**
     * Get the {@code cadence} value.
     * Can be {@code null}.
     */
    public Float getCadence() {
        return getFloatOrNull(LogColumns.CADENCE);
    }

    /**
     * Get the {@code heart_rate} value.
     * Can be {@code null}.
     */
    public Integer getHeartRate() {
        return getIntegerOrNull(LogColumns.HEART_RATE);
    }

    /**
     * Get the {@code name} value.
     * Can be {@code null}.
     */
    public String getName() {
        Integer index = getCachedColumnIndexOrThrow(RideColumns.NAME);
        return getString(index);
    }

    /**
     * Get the {@code created_date} value.
     * Cannot be {@code null}.
     */
    public Date getCreatedDate() {
        return getDate(RideColumns.CREATED_DATE);
    }

    /**
     * Get the {@code state} value.
     * Cannot be {@code null}.
     */
    public RideState getState() {
        Integer intValue = getIntegerOrNull(RideColumns.STATE);
        if (intValue == null) return null;
        return RideState.values()[intValue];
    }

    /**
     * Get the {@code first_activated_date} value.
     * Can be {@code null}.
     */
    public Date getFirstActivatedDate() {
        return getDate(RideColumns.FIRST_ACTIVATED_DATE);
    }

    /**
     * Get the {@code activated_date} value.
     * Can be {@code null}.
     */
    public Date getActivatedDate() {
        return getDate(RideColumns.ACTIVATED_DATE);
    }

    /**
     * Get the {@code duration} value.
     */
    public long getDuration() {
        return getLongOrNull(RideColumns.DURATION);
    }

    /**
     * Get the {@code distance} value.
     */
    public float getDistance() {
        return getFloatOrNull(RideColumns.DISTANCE);
    }
}
