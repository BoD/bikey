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
package org.jraf.android.bikey.backend.location;

import android.location.Location;
import android.util.Pair;

public class LocationPair extends Pair<Location, Location> {
    private Float mDistance;
    private Long mDuration;
    private Double mAltitudeDiff;
    private Float mSpeed;
    private Float mSlope;

    public LocationPair(Location previousLocation, Location newLocation) {
        super(previousLocation, newLocation);
    }


    public float getDistance() {
        if (mDistance == null) {
            float[] distanceResults = new float[1];
            Location.distanceBetween(first.getLatitude(), first.getLongitude(), second.getLatitude(), second.getLongitude(), distanceResults);
            mDistance = distanceResults[0];
        }
        return mDistance;
    }

    public void setDistance(float distance) {
        mDistance = distance;
    }

    public long getDuration() {
        if (mDuration == null) {
            mDuration = second.getTime() - first.getTime();
        }
        return mDuration;
    }

    /**
     * In meters. Positive means going up, negative means going down.
     */
    public double getAltitudeDiff() {
        if (mAltitudeDiff == null) {
            mAltitudeDiff = second.getAltitude() - first.getAltitude();
        }
        return mAltitudeDiff;
    }

    /**
     * In meters/second.
     */
    public float getSpeed() {
        if (mSpeed == null) {
            if (getDuration() == 0) {
                mSpeed = 0f;
            } else {
                mSpeed = getDistance() / (getDuration() / 1000f);
            }
        }
        return mSpeed;
    }

    /**
     * In fraction of 1 (positive means going up, negative means going down).
     */
    public float getSlope() {
        if (mSlope == null) {
            if (getDistance() == 0) {
                mSlope = 0f;
            } else {
                mSlope = (float) (getAltitudeDiff() / getDistance());
            }
        }
        return mSlope;
    }

    @Override
    public String toString() {
        return "LocationPair [getDuration()=" + getDuration() + ", getDistance()=" + getDistance() + ", getSpeed()=" + getSpeed() + ", getAltitudeDiff()="
                + getAltitudeDiff() + ", getSlope()=" + getSlope() + "]";
    }
}
