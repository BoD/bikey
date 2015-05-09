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

import org.jraf.android.bikey.backend.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Data model for the {@code ride} table.
 */
public interface RideModel extends BaseModel {

    /**
     * Get the {@code name} value.
     * Can be {@code null}.
     */
    @Nullable
    String getName();

    /**
     * Get the {@code created_date} value.
     * Cannot be {@code null}.
     */
    @NonNull
    Date getCreatedDate();

    /**
     * Get the {@code state} value.
     * Cannot be {@code null}.
     */
    @NonNull
    RideState getState();

    /**
     * Get the {@code first_activated_date} value.
     * Can be {@code null}.
     */
    @Nullable
    Date getFirstActivatedDate();

    /**
     * Get the {@code activated_date} value.
     * Can be {@code null}.
     */
    @Nullable
    Date getActivatedDate();

    /**
     * Get the {@code duration} value.
     */
    long getDuration();

    /**
     * Get the {@code distance} value.
     */
    float getDistance();
}
