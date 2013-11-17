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

    public LogSelection rideIdGt(long value) {
        addGreaterThan(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection rideIdLt(long value) {
        addLessThan(LogColumns.RIDE_ID, value);
        return this;
    }

    public LogSelection recordedDate(Date... value) {
        addEquals(LogColumns.RECORDED_DATE, (Object[]) value);
        return this;
    }

    public LogSelection recordedDate(Long... value) {
        addEquals(LogColumns.RECORDED_DATE, (Object[]) value);
        return this;
    }

    public LogSelection lat(Double... value) {
        addEquals(LogColumns.LAT, (Object[]) value);
        return this;
    }

    public LogSelection latGt(double value) {
        addGreaterThan(LogColumns.LAT, value);
        return this;
    }

    public LogSelection latLt(double value) {
        addLessThan(LogColumns.LAT, value);
        return this;
    }

    public LogSelection lon(Double... value) {
        addEquals(LogColumns.LON, (Object[]) value);
        return this;
    }

    public LogSelection lonGt(double value) {
        addGreaterThan(LogColumns.LON, value);
        return this;
    }

    public LogSelection lonLt(double value) {
        addLessThan(LogColumns.LON, value);
        return this;
    }

    public LogSelection ele(Double... value) {
        addEquals(LogColumns.ELE, (Object[]) value);
        return this;
    }

    public LogSelection eleGt(double value) {
        addGreaterThan(LogColumns.ELE, value);
        return this;
    }

    public LogSelection eleLt(double value) {
        addLessThan(LogColumns.ELE, value);
        return this;
    }

    public LogSelection duration(Long... value) {
        addEquals(LogColumns.DURATION, (Object[]) value);
        return this;
    }

    public LogSelection durationGt(long value) {
        addGreaterThan(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection durationLt(long value) {
        addLessThan(LogColumns.DURATION, value);
        return this;
    }

    public LogSelection distance(Double... value) {
        addEquals(LogColumns.DISTANCE, (Object[]) value);
        return this;
    }

    public LogSelection distanceGt(double value) {
        addGreaterThan(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection distanceLt(double value) {
        addLessThan(LogColumns.DISTANCE, value);
        return this;
    }

    public LogSelection speed(Double... value) {
        addEquals(LogColumns.SPEED, (Object[]) value);
        return this;
    }

    public LogSelection speedGt(double value) {
        addGreaterThan(LogColumns.SPEED, value);
        return this;
    }

    public LogSelection speedLt(double value) {
        addLessThan(LogColumns.SPEED, value);
        return this;
    }
}
