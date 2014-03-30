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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.bikey.backend.provider.base.AbstractSelection;

/**
 * Selection for the {@code log} table.
 */
public class LogSelection extends AbstractSelection<LogSelection> {
    @Override
    public Uri uri() {
        return LogColumns.CONTENT_URI;
    }
    
    /**
     * Query the given content resolver using this selection.
     * 
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code LogCursor} object, which is positioned before the first entry, or null.
     */
    public LogCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new LogCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null}.
     */
    public LogCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null}.
     */
    public LogCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }
    
    
    public LogSelection id(long... value) {
        addEquals(LogColumns._ID, toObjectArray(value));
        return this;
    }

    public LogSelection rideId(long... value) {
        addEquals(LogColumns.RIDE_ID, toObjectArray(value));
        return this;
    }
    
    public LogSelection rideIdNot(long... value) {
        addNotEquals(LogColumns.RIDE_ID, toObjectArray(value));
        return this;
    }

    public LogSelection rideIdGt(long value) {
        addGreaterThan(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection rideIdGtEq(long value) {
        addGreaterThanOrEquals(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection rideIdLt(long value) {
        addLessThan(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection rideIdLtEq(long value) {
        addLessThanOrEquals(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection recordedDate(Date... value) {
        addEquals(LogColumns.RECORDED_DATE, value);
        return this;
    }
    
    public LogSelection recordedDateNot(Date... value) {
        addNotEquals(LogColumns.RECORDED_DATE, value);
        return this;
    }

    public LogSelection recordedDate(long... value) {
        addEquals(LogColumns.RECORDED_DATE, toObjectArray(value));
        return this;
    }

    public LogSelection recordedDateAfter(Date value) {
        addGreaterThan(LogColumns.RECORDED_DATE, value);
        return this;
    }

    public LogSelection recordedDateAfterEq(Date value) {
        addGreaterThanOrEquals(LogColumns.RECORDED_DATE, value);
        return this;
    }

    public LogSelection recordedDateBefore(Date value) {
        addLessThan(LogColumns.RECORDED_DATE, value);
        return this;
    }

    public LogSelection recordedDateBeforeEq(Date value) {
        addLessThanOrEquals(LogColumns.RECORDED_DATE, value);
        return this;
    }

    public LogSelection lat(double... value) {
        addEquals(LogColumns.LAT, toObjectArray(value));
        return this;
    }
    
    public LogSelection latNot(double... value) {
        addNotEquals(LogColumns.LAT, toObjectArray(value));
        return this;
    }

    public LogSelection latGt(double value) {
        addGreaterThan(LogColumns.LAT, value);
        return this;
    }

    public LogSelection latGtEq(double value) {
        addGreaterThanOrEquals(LogColumns.LAT, value);
        return this;
    }

    public LogSelection latLt(double value) {
        addLessThan(LogColumns.LAT, value);
        return this;
    }

    public LogSelection latLtEq(double value) {
        addLessThanOrEquals(LogColumns.LAT, value);
        return this;
    }

    public LogSelection lon(double... value) {
        addEquals(LogColumns.LON, toObjectArray(value));
        return this;
    }
    
    public LogSelection lonNot(double... value) {
        addNotEquals(LogColumns.LON, toObjectArray(value));
        return this;
    }

    public LogSelection lonGt(double value) {
        addGreaterThan(LogColumns.LON, value);
        return this;
    }

    public LogSelection lonGtEq(double value) {
        addGreaterThanOrEquals(LogColumns.LON, value);
        return this;
    }

    public LogSelection lonLt(double value) {
        addLessThan(LogColumns.LON, value);
        return this;
    }

    public LogSelection lonLtEq(double value) {
        addLessThanOrEquals(LogColumns.LON, value);
        return this;
    }

    public LogSelection ele(double... value) {
        addEquals(LogColumns.ELE, toObjectArray(value));
        return this;
    }
    
    public LogSelection eleNot(double... value) {
        addNotEquals(LogColumns.ELE, toObjectArray(value));
        return this;
    }

    public LogSelection eleGt(double value) {
        addGreaterThan(LogColumns.ELE, value);
        return this;
    }

    public LogSelection eleGtEq(double value) {
        addGreaterThanOrEquals(LogColumns.ELE, value);
        return this;
    }

    public LogSelection eleLt(double value) {
        addLessThan(LogColumns.ELE, value);
        return this;
    }

    public LogSelection eleLtEq(double value) {
        addLessThanOrEquals(LogColumns.ELE, value);
        return this;
    }

    public LogSelection duration(Long... value) {
        addEquals(LogColumns.DURATION, value);
        return this;
    }
    
    public LogSelection durationNot(Long... value) {
        addNotEquals(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection durationGt(long value) {
        addGreaterThan(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection durationGtEq(long value) {
        addGreaterThanOrEquals(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection durationLt(long value) {
        addLessThan(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection durationLtEq(long value) {
        addLessThanOrEquals(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection distance(Float... value) {
        addEquals(LogColumns.DISTANCE, value);
        return this;
    }
    
    public LogSelection distanceNot(Float... value) {
        addNotEquals(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection distanceGt(float value) {
        addGreaterThan(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection distanceGtEq(float value) {
        addGreaterThanOrEquals(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection distanceLt(float value) {
        addLessThan(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection distanceLtEq(float value) {
        addLessThanOrEquals(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection speed(Float... value) {
        addEquals(LogColumns.SPEED, value);
        return this;
    }
    
    public LogSelection speedNot(Float... value) {
        addNotEquals(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection speedGt(float value) {
        addGreaterThan(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection speedGtEq(float value) {
        addGreaterThanOrEquals(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection speedLt(float value) {
        addLessThan(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection speedLtEq(float value) {
        addLessThanOrEquals(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection cadence(Float... value) {
        addEquals(LogColumns.CADENCE, value);
        return this;
    }
    
    public LogSelection cadenceNot(Float... value) {
        addNotEquals(LogColumns.CADENCE, value);
        return this;
    }

    public LogSelection cadenceGt(float value) {
        addGreaterThan(LogColumns.CADENCE, value);
        return this;
    }

    public LogSelection cadenceGtEq(float value) {
        addGreaterThanOrEquals(LogColumns.CADENCE, value);
        return this;
    }

    public LogSelection cadenceLt(float value) {
        addLessThan(LogColumns.CADENCE, value);
        return this;
    }

    public LogSelection cadenceLtEq(float value) {
        addLessThanOrEquals(LogColumns.CADENCE, value);
        return this;
    }
}
