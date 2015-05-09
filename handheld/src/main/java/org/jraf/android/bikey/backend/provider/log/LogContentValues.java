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

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jraf.android.bikey.backend.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code log} table.
 */
public class LogContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return LogColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable LogSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable LogSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public LogContentValues putRideId(long value) {
        mContentValues.put(LogColumns.RIDE_ID, value);
        return this;
    }


    public LogContentValues putRecordedDate(@NonNull Date value) {
        if (value == null) throw new IllegalArgumentException("recordedDate must not be null");
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


    public LogContentValues putLogDuration(@Nullable Long value) {
        mContentValues.put(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogContentValues putLogDurationNull() {
        mContentValues.putNull(LogColumns.LOG_DURATION);
        return this;
    }

    public LogContentValues putLogDistance(@Nullable Float value) {
        mContentValues.put(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogContentValues putLogDistanceNull() {
        mContentValues.putNull(LogColumns.LOG_DISTANCE);
        return this;
    }

    public LogContentValues putSpeed(@Nullable Float value) {
        mContentValues.put(LogColumns.SPEED, value);
        return this;
    }

    public LogContentValues putSpeedNull() {
        mContentValues.putNull(LogColumns.SPEED);
        return this;
    }

    public LogContentValues putCadence(@Nullable Float value) {
        mContentValues.put(LogColumns.CADENCE, value);
        return this;
    }

    public LogContentValues putCadenceNull() {
        mContentValues.putNull(LogColumns.CADENCE);
        return this;
    }

    public LogContentValues putHeartRate(@Nullable Integer value) {
        mContentValues.put(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogContentValues putHeartRateNull() {
        mContentValues.putNull(LogColumns.HEART_RATE);
        return this;
    }
}
