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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.bikey.backend.provider.base.AbstractSelection;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.backend.provider.ride.RideState;

/**
 * Selection for the {@code log} table.
 */
public class LogSelection extends AbstractSelection<LogSelection> {
    @Override
    protected Uri baseUri() {
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
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public LogCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public LogCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code LogCursor} object, which is positioned before the first entry, or null.
     */
    public LogCursor query(Context context, String[] projection, String sortOrder) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new LogCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, projection, null)}.
     */
    public LogCursor query(Context context, String[] projection) {
        return query(context, projection, null);
    }

    /**
     * Equivalent of calling {@code query(context, projection, null, null)}.
     */
    public LogCursor query(Context context) {
        return query(context, null, null);
    }


    public LogSelection id(long... value) {
        addEquals("log." + LogColumns._ID, toObjectArray(value));
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

    public LogSelection rideUuid(String... value) {
        addEquals(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideUuidNot(String... value) {
        addNotEquals(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideUuidLike(String... value) {
        addLike(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideUuidContains(String... value) {
        addContains(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideUuidStartsWith(String... value) {
        addStartsWith(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideUuidEndsWith(String... value) {
        addEndsWith(RideColumns.UUID, value);
        return this;
    }

    public LogSelection rideName(String... value) {
        addEquals(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideNameNot(String... value) {
        addNotEquals(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideNameLike(String... value) {
        addLike(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideNameContains(String... value) {
        addContains(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideNameStartsWith(String... value) {
        addStartsWith(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideNameEndsWith(String... value) {
        addEndsWith(RideColumns.NAME, value);
        return this;
    }

    public LogSelection rideCreatedDate(Date... value) {
        addEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideCreatedDateNot(Date... value) {
        addNotEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideCreatedDate(long... value) {
        addEquals(RideColumns.CREATED_DATE, toObjectArray(value));
        return this;
    }

    public LogSelection rideCreatedDateAfter(Date value) {
        addGreaterThan(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideCreatedDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideCreatedDateBefore(Date value) {
        addLessThan(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideCreatedDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public LogSelection rideState(RideState... value) {
        addEquals(RideColumns.STATE, value);
        return this;
    }

    public LogSelection rideStateNot(RideState... value) {
        addNotEquals(RideColumns.STATE, value);
        return this;
    }


    public LogSelection rideFirstActivatedDate(Date... value) {
        addEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDateNot(Date... value) {
        addNotEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDate(Long... value) {
        addEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDateAfter(Date value) {
        addGreaterThan(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDateBefore(Date value) {
        addLessThan(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideFirstActivatedDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDate(Date... value) {
        addEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDateNot(Date... value) {
        addNotEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDate(Long... value) {
        addEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDateAfter(Date value) {
        addGreaterThan(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDateBefore(Date value) {
        addLessThan(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideActivatedDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public LogSelection rideDuration(long... value) {
        addEquals(RideColumns.DURATION, toObjectArray(value));
        return this;
    }

    public LogSelection rideDurationNot(long... value) {
        addNotEquals(RideColumns.DURATION, toObjectArray(value));
        return this;
    }

    public LogSelection rideDurationGt(long value) {
        addGreaterThan(RideColumns.DURATION, value);
        return this;
    }

    public LogSelection rideDurationGtEq(long value) {
        addGreaterThanOrEquals(RideColumns.DURATION, value);
        return this;
    }

    public LogSelection rideDurationLt(long value) {
        addLessThan(RideColumns.DURATION, value);
        return this;
    }

    public LogSelection rideDurationLtEq(long value) {
        addLessThanOrEquals(RideColumns.DURATION, value);
        return this;
    }

    public LogSelection rideDistance(float... value) {
        addEquals(RideColumns.DISTANCE, toObjectArray(value));
        return this;
    }

    public LogSelection rideDistanceNot(float... value) {
        addNotEquals(RideColumns.DISTANCE, toObjectArray(value));
        return this;
    }

    public LogSelection rideDistanceGt(float value) {
        addGreaterThan(RideColumns.DISTANCE, value);
        return this;
    }

    public LogSelection rideDistanceGtEq(float value) {
        addGreaterThanOrEquals(RideColumns.DISTANCE, value);
        return this;
    }

    public LogSelection rideDistanceLt(float value) {
        addLessThan(RideColumns.DISTANCE, value);
        return this;
    }

    public LogSelection rideDistanceLtEq(float value) {
        addLessThanOrEquals(RideColumns.DISTANCE, value);
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

    public LogSelection logDuration(Long... value) {
        addEquals(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDurationNot(Long... value) {
        addNotEquals(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDurationGt(long value) {
        addGreaterThan(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDurationGtEq(long value) {
        addGreaterThanOrEquals(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDurationLt(long value) {
        addLessThan(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDurationLtEq(long value) {
        addLessThanOrEquals(LogColumns.LOG_DURATION, value);
        return this;
    }

    public LogSelection logDistance(Float... value) {
        addEquals(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogSelection logDistanceNot(Float... value) {
        addNotEquals(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogSelection logDistanceGt(float value) {
        addGreaterThan(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogSelection logDistanceGtEq(float value) {
        addGreaterThanOrEquals(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogSelection logDistanceLt(float value) {
        addLessThan(LogColumns.LOG_DISTANCE, value);
        return this;
    }

    public LogSelection logDistanceLtEq(float value) {
        addLessThanOrEquals(LogColumns.LOG_DISTANCE, value);
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

    public LogSelection heartRate(Integer... value) {
        addEquals(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogSelection heartRateNot(Integer... value) {
        addNotEquals(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogSelection heartRateGt(int value) {
        addGreaterThan(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogSelection heartRateGtEq(int value) {
        addGreaterThanOrEquals(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogSelection heartRateLt(int value) {
        addLessThan(LogColumns.HEART_RATE, value);
        return this;
    }

    public LogSelection heartRateLtEq(int value) {
        addLessThanOrEquals(LogColumns.HEART_RATE, value);
        return this;
    }
}
