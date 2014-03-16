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
