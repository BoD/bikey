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

import java.util.Date;

import android.database.Cursor;

import org.jraf.android.bikey.backend.provider.base.AbstractCursorWrapper;

/**
 * Cursor wrapper for the {@code log} table.
 */
public class LogCursorWrapper extends AbstractCursorWrapper {
    public LogCursorWrapper(Cursor cursor) {
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
     * Get the {@code duration} value.
     * Can be {@code null}.
     */
    public Long getDuration() {
        return getLongOrNull(LogColumns.DURATION);
    }

    /**
     * Get the {@code distance} value.
     * Can be {@code null}.
     */
    public Float getDistance() {
        return getFloatOrNull(LogColumns.DISTANCE);
    }

    /**
     * Get the {@code speed} value.
     * Can be {@code null}.
     */
    public Float getSpeed() {
        return getFloatOrNull(LogColumns.SPEED);
    }
}
