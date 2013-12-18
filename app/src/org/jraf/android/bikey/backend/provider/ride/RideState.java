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

public enum RideState {

    /**
     * Initial state: the ride has been created but has not started yet.
     */
    CREATED(0),

    /**
     * The ride is currently active and being recorded.<br>
     * Only one ride can be active at any time.
     */
    ACTIVE(1),

    /**
     * The ride has been started but recording is currently paused.
     */
    PAUSED(2);


    private int mValue;

    RideState(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public String getValueAsString() {
        return String.valueOf(mValue);
    }

    public static RideState from(int value) {
        for (RideState rideState : values()) {
            if (rideState.mValue == value) return rideState;
        }
        return null;
    }
}
