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

import org.jraf.android.bikey.backend.provider.base.AbstractContentValuesWrapper;

/**
 * Content values wrapper for the {@code log} table.
 */
public class LogContentValues extends AbstractContentValuesWrapper {

    public void putRideId(Long value) {
        mContentValues.put(LogColumns.RIDE_ID, value);
    }



    public void putRecordedDate(Date value) {
        mContentValues.put(LogColumns.RECORDED_DATE, value.getTime());
    }


    public void putRecordedDate(Long value) {
        mContentValues.put(LogColumns.RECORDED_DATE, value);
    }


    public void putLat(Double value) {
        mContentValues.put(LogColumns.LAT, value);
    }



    public void putLon(Double value) {
        mContentValues.put(LogColumns.LON, value);
    }



    public void putEle(Double value) {
        mContentValues.put(LogColumns.ELE, value);
    }



    public void putDuration(Long value) {
        mContentValues.put(LogColumns.DURATION, value);
    }

    public void putDurationNull() {
        mContentValues.putNull(LogColumns.DURATION);
    }


    public void putDistance(Float value) {
        mContentValues.put(LogColumns.DISTANCE, value);
    }

    public void putDistanceNull() {
        mContentValues.putNull(LogColumns.DISTANCE);
    }


    public void putSpeed(Float value) {
        mContentValues.put(LogColumns.SPEED, value);
    }

    public void putSpeedNull() {
        mContentValues.putNull(LogColumns.SPEED);
    }

}
