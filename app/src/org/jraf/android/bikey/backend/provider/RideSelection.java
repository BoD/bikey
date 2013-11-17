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
 * Selection for the {@code ride} table.
 */
public class RideSelection extends AbstractSelection<RideSelection> {
    public RideSelection id(Long... value) {
        addEquals(RideColumns._ID, (Object[]) value);
        return this;
    }


    public RideSelection name(String... value) {
        addEquals(RideColumns.NAME, (Object[]) value);
        return this;
    }


    public RideSelection createdDate(Date... value) {
        addEquals(RideColumns.CREATED_DATE, (Object[]) value);
        return this;
    }

    public RideSelection createdDate(Long... value) {
        addEquals(RideColumns.CREATED_DATE, (Object[]) value);
        return this;
    }

    public RideSelection state(Long... value) {
        addEquals(RideColumns.STATE, (Object[]) value);
        return this;
    }

    public RideSelection stateGt(long value) {
        addGreaterThan(RideColumns.STATE, value);
        return this;
    }

    public RideSelection stateLt(long value) {
        addLessThan(RideColumns.STATE, value);
        return this;
    }

    public RideSelection activatedDate(Date... value) {
        addEquals(RideColumns.ACTIVATED_DATE, (Object[]) value);
        return this;
    }

    public RideSelection activatedDate(Long... value) {
        addEquals(RideColumns.ACTIVATED_DATE, (Object[]) value);
        return this;
    }

    public RideSelection duration(Long... value) {
        addEquals(RideColumns.DURATION, (Object[]) value);
        return this;
    }

    public RideSelection durationGt(long value) {
        addGreaterThan(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection durationLt(long value) {
        addLessThan(RideColumns.DURATION, value);
        return this;
    }

    public RideSelection distance(Double... value) {
        addEquals(RideColumns.DISTANCE, (Object[]) value);
        return this;
    }

    public RideSelection distanceGt(double value) {
        addGreaterThan(RideColumns.DISTANCE, value);
        return this;
    }

    public RideSelection distanceLt(double value) {
        addLessThan(RideColumns.DISTANCE, value);
        return this;
    }
}
