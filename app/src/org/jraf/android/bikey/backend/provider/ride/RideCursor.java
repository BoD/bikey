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

import android.database.Cursor;

import org.jraf.android.bikey.backend.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code ride} table.
 */
public class RideCursor extends AbstractCursor {
    public RideCursor(Cursor cursor) {
        super(cursor);
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
    public double getDistance() {
        return getDoubleOrNull(RideColumns.DISTANCE);
    }
}
