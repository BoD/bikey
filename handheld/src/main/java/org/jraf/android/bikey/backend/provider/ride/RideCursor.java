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
package org.jraf.android.bikey.backend.provider.ride;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jraf.android.bikey.backend.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code ride} table.
 */
public class RideCursor extends AbstractCursor implements RideModel {
    public RideCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(RideColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code uuid} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getUuid() {
        String res = getStringOrNull(RideColumns.UUID);
        if (res == null)
            throw new NullPointerException("The value of 'uuid' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code name} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getName() {
        String res = getStringOrNull(RideColumns.NAME);
        return res;
    }

    /**
     * Get the {@code created_date} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public Date getCreatedDate() {
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
    public RideState getState() {
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
    public Date getFirstActivatedDate() {
        Date res = getDateOrNull(RideColumns.FIRST_ACTIVATED_DATE);
        return res;
    }

    /**
     * Get the {@code activated_date} value.
     * Can be {@code null}.
     */
    @Nullable
    public Date getActivatedDate() {
        Date res = getDateOrNull(RideColumns.ACTIVATED_DATE);
        return res;
    }

    /**
     * Get the {@code duration} value.
     */
    public long getDuration() {
        Long res = getLongOrNull(RideColumns.DURATION);
        if (res == null)
            throw new NullPointerException("The value of 'duration' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code distance} value.
     */
    public float getDistance() {
        Float res = getFloatOrNull(RideColumns.DISTANCE);
        if (res == null)
            throw new NullPointerException("The value of 'distance' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
