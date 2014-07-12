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
import android.net.Uri;

import org.jraf.android.bikey.backend.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code ride} table.
 */
public class RideContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return RideColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, RideSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public RideContentValues putName(String value) {
        mContentValues.put(RideColumns.NAME, value);
        return this;
    }

    public RideContentValues putNameNull() {
        mContentValues.putNull(RideColumns.NAME);
        return this;
    }


    public RideContentValues putCreatedDate(Date value) {
        if (value == null) throw new IllegalArgumentException("value for createdDate must not be null");
        mContentValues.put(RideColumns.CREATED_DATE, value.getTime());
        return this;
    }


    public RideContentValues putCreatedDate(long value) {
        mContentValues.put(RideColumns.CREATED_DATE, value);
        return this;
    }


    public RideContentValues putState(RideState value) {
        if (value == null) throw new IllegalArgumentException("value for state must not be null");
        mContentValues.put(RideColumns.STATE, value.ordinal());
        return this;
    }



    public RideContentValues putFirstActivatedDate(Date value) {
        mContentValues.put(RideColumns.FIRST_ACTIVATED_DATE, value == null ? null : value.getTime());
        return this;
    }

    public RideContentValues putFirstActivatedDateNull() {
        mContentValues.putNull(RideColumns.FIRST_ACTIVATED_DATE);
        return this;
    }

    public RideContentValues putFirstActivatedDate(Long value) {
        mContentValues.put(RideColumns.FIRST_ACTIVATED_DATE, value);
        return this;
    }


    public RideContentValues putActivatedDate(Date value) {
        mContentValues.put(RideColumns.ACTIVATED_DATE, value == null ? null : value.getTime());
        return this;
    }

    public RideContentValues putActivatedDateNull() {
        mContentValues.putNull(RideColumns.ACTIVATED_DATE);
        return this;
    }

    public RideContentValues putActivatedDate(Long value) {
        mContentValues.put(RideColumns.ACTIVATED_DATE, value);
        return this;
    }


    public RideContentValues putDuration(long value) {
        mContentValues.put(RideColumns.DURATION, value);
        return this;
    }



    public RideContentValues putDistance(float value) {
        mContentValues.put(RideColumns.DISTANCE, value);
        return this;
    }


}
