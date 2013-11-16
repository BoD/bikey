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
package org.jraf.android.bikey.backend.provider;

import java.util.Date;

/**
 * Content values wrapper for the {@code ride} table.
 */
public class RideContentValues extends AbstractContentValuesWrapper {

    public void putName(String value) {
        mContentValues.put(RideColumns.NAME, value);
    }

    public void putNameNull() {
        mContentValues.putNull(RideColumns.NAME);
    }


    public void putCreatedDate(Date value) {
        mContentValues.put(RideColumns.CREATED_DATE, value.getTime());
    }

    public void putCreatedDateNull() {
        mContentValues.putNull(RideColumns.CREATED_DATE);
    }

    public void putCreatedDate(Long value) {
        mContentValues.put(RideColumns.CREATED_DATE, value);
    }


    public void putState(Long value) {
        mContentValues.put(RideColumns.STATE, value);
    }

    public void putStateNull() {
        mContentValues.putNull(RideColumns.STATE);
    }


    public void putActivatedDate(Date value) {
        mContentValues.put(RideColumns.ACTIVATED_DATE, value.getTime());
    }

    public void putActivatedDateNull() {
        mContentValues.putNull(RideColumns.ACTIVATED_DATE);
    }

    public void putActivatedDate(Long value) {
        mContentValues.put(RideColumns.ACTIVATED_DATE, value);
    }


    public void putDuration(Long value) {
        mContentValues.put(RideColumns.DURATION, value);
    }

    public void putDurationNull() {
        mContentValues.putNull(RideColumns.DURATION);
    }


    public void putDistance(Double value) {
        mContentValues.put(RideColumns.DISTANCE, value);
    }

    public void putDistanceNull() {
        mContentValues.putNull(RideColumns.DISTANCE);
    }

}
