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
package org.jraf.android.bikey.backend.provider.ride;

import java.util.Date;

import android.database.Cursor;

import org.jraf.android.bikey.backend.provider.base.AbstractCursorWrapper;

/**
 * Cursor wrapper for the {@code ride} table.
 */
public class RideCursorWrapper extends AbstractCursorWrapper {
    public RideCursorWrapper(Cursor cursor) {
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
     */
    public int getState() {
        return getIntegerOrNull(RideColumns.STATE);
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
