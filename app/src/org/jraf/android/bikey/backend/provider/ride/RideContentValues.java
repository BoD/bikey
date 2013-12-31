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

import org.jraf.android.bikey.backend.provider.base.AbstractContentValuesWrapper;

/**
 * Content values wrapper for the {@code ride} table.
 */
public class RideContentValues extends AbstractContentValuesWrapper {

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


    public RideContentValues putState(int value) {
        mContentValues.put(RideColumns.STATE, value);
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



    public RideContentValues putDistance(double value) {
        mContentValues.put(RideColumns.DISTANCE, value);
        return this;
    }


}
