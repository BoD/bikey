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
package org.jraf.android.bikey.backend.provider.log;

import org.jraf.android.bikey.backend.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Data model for the {@code log} table.
 */
public interface LogModel extends BaseModel {

    /**
     * Get the {@code ride_id} value.
     */
    long getRideId();

    /**
     * Get the {@code recorded_date} value.
     * Cannot be {@code null}.
     */
    @NonNull
    Date getRecordedDate();

    /**
     * Get the {@code lat} value.
     */
    double getLat();

    /**
     * Get the {@code lon} value.
     */
    double getLon();

    /**
     * Get the {@code ele} value.
     */
    double getEle();

    /**
     * Get the {@code log_duration} value.
     * Can be {@code null}.
     */
    @Nullable
    Long getLogDuration();

    /**
     * Get the {@code log_distance} value.
     * Can be {@code null}.
     */
    @Nullable
    Float getLogDistance();

    /**
     * Get the {@code speed} value.
     * Can be {@code null}.
     */
    @Nullable
    Float getSpeed();

    /**
     * Get the {@code cadence} value.
     * Can be {@code null}.
     */
    @Nullable
    Float getCadence();

    /**
     * Get the {@code heart_rate} value.
     * Can be {@code null}.
     */
    @Nullable
    Integer getHeartRate();
}
