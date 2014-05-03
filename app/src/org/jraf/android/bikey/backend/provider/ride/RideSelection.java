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
package org.jraf.android.bikey.backend.provider.ride;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.bikey.backend.provider.base.AbstractSelection;

/**
 * Selection for the {@code ride} table.
 */
public class RideSelection extends AbstractSelection<RideSelection> {
    @Override
    public Uri uri() {
        return RideColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code RideCursor} object, which is positioned before the first entry, or null.
     */
    public RideCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new RideCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null}.
     */
    public RideCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null}.
     */
    public RideCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public RideSelection id(long... value) {
        addEquals(RideColumns._ID, toObjectArray(value));
        return this;
    }


    public RideSelection name(String... value) {
        addEquals(RideColumns.NAME, value);
        return this;
    }

    public RideSelection nameNot(String... value) {
        addNotEquals(RideColumns.NAME, value);
        return this;
    }

    public RideSelection nameLike(String... value) {
        addLike(RideColumns.NAME, value);
        return this;
    }

    public RideSelection createdDate(Date... value) {
        addEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection createdDateNot(Date... value) {
        addNotEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection createdDate(long... value) {
        addEquals(RideColumns.CREATED_DATE, toObjectArray(value));
        return this;
    }

    public RideSelection createdDateAfter(Date value) {
        addGreaterThan(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection createdDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection createdDateBefore(Date value) {
        addLessThan(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection createdDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.CREATED_DATE, value);
        return this;
    }

    public RideSelection state(RideState... value) {
        addEquals(RideColumns.STATE, value);
        return this;
    }

    public RideSelection stateNot(RideState... value) {
        addNotEquals(RideColumns.STATE, value);
        return this;
    }


    public RideSelection firstActivatedDate(Date... value) {
        addEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDateNot(Date... value) {
        addNotEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDate(Long... value) {
        addEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDateAfter(Date value) {
        addGreaterThan(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDateBefore(Date value) {
        addLessThan(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection firstActivatedDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDate(Date... value) {
        addEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDateNot(Date... value) {
        addNotEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDate(Long... value) {
        addEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDateAfter(Date value) {
        addGreaterThan(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDateAfterEq(Date value) {
        addGreaterThanOrEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDateBefore(Date value) {
        addLessThan(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection activatedDateBeforeEq(Date value) {
        addLessThanOrEquals(RideColumns.ACTIVATED_DATE, value);
        return this;
    }

    public RideSelection duration(long... value) {
        addEquals(RideColumns.DURATION, toObjectArray(value));
        return this;
    }

    public RideSelection durationNot(long... value) {
        addNotEquals(RideColumns.DURATION, toObjectArray(value));
        return this;
    }

    public RideSelection durationGt(long value) {
        addGreaterThan(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection durationGtEq(long value) {
        addGreaterThanOrEquals(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection durationLt(long value) {
        addLessThan(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection durationLtEq(long value) {
        addLessThanOrEquals(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection distance(float... value) {
        addEquals(RideColumns.DISTANCE, toObjectArray(value));
        return this;
    }

    public RideSelection distanceNot(float... value) {
        addNotEquals(RideColumns.DISTANCE, toObjectArray(value));
        return this;
    }

    public RideSelection distanceGt(float value) {
        addGreaterThan(RideColumns.DISTANCE, value);
        return this;
    }

    public RideSelection distanceGtEq(float value) {
        addGreaterThanOrEquals(RideColumns.DISTANCE, value);
        return this;
    }

    public RideSelection distanceLt(float value) {
        addLessThan(RideColumns.DISTANCE, value);
        return this;
    }

    public RideSelection distanceLtEq(float value) {
        addLessThanOrEquals(RideColumns.DISTANCE, value);
        return this;
    }
}
