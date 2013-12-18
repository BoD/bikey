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

import org.jraf.android.bikey.backend.provider.base.AbstractSelection;

/**
 * Selection for the {@code log} table.
 */
public class LogSelection extends AbstractSelection<LogSelection> {
    public LogSelection id(Long... value) {
        addEquals(LogColumns._ID, (Object[]) value);
        return this;
    }

    public LogSelection rideId(Long... value) {
        addEquals(LogColumns.RIDE_ID, (Object[]) value);
        return this;
    }
    
    public LogSelection rideIdNot(Long... value) {
        addNotEquals(LogColumns.RIDE_ID, (Object[]) value);
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
        addEquals(LogColumns.RECORDED_DATE, (Object[]) value);
        return this;
    }
    
    public LogSelection recordedDateNot(Date... value) {
        addNotEquals(LogColumns.RECORDED_DATE, (Object[]) value);
        return this;
    }

    public LogSelection recordedDate(Long... value) {
        addEquals(LogColumns.RECORDED_DATE, (Object[]) value);
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

    public LogSelection lat(Double... value) {
        addEquals(LogColumns.LAT, (Object[]) value);
        return this;
    }
    
    public LogSelection latNot(Double... value) {
        addNotEquals(LogColumns.LAT, (Object[]) value);
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

    public LogSelection lon(Double... value) {
        addEquals(LogColumns.LON, (Object[]) value);
        return this;
    }
    
    public LogSelection lonNot(Double... value) {
        addNotEquals(LogColumns.LON, (Object[]) value);
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

    public LogSelection ele(Double... value) {
        addEquals(LogColumns.ELE, (Object[]) value);
        return this;
    }
    
    public LogSelection eleNot(Double... value) {
        addNotEquals(LogColumns.ELE, (Object[]) value);
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
        addEquals(LogColumns.DURATION, (Object[]) value);
        return this;
    }
    
    public LogSelection durationNot(Long... value) {
        addNotEquals(LogColumns.DURATION, (Object[]) value);
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
        addEquals(LogColumns.DISTANCE, (Object[]) value);
        return this;
    }
    
    public LogSelection distanceNot(Float... value) {
        addNotEquals(LogColumns.DISTANCE, (Object[]) value);
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
        addEquals(LogColumns.SPEED, (Object[]) value);
        return this;
    }
    
    public LogSelection speedNot(Float... value) {
        addNotEquals(LogColumns.SPEED, (Object[]) value);
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
}
