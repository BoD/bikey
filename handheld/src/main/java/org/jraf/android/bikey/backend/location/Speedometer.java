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

import java.util.ArrayDeque;
import java.util.Deque;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.jraf.android.util.log.wrapper.Log;

/**
 * Keeps a log of mDistance/mDuration. The size of the log depends on the last measured speed.
 */
public class Speedometer implements LocationListener {
    /**
     * Number of entries to keep when going slow.
     */
    private static final int LOG_SIZE_SLOW = 3;

    /**
     * Number of entries to keep when going at medium speed.
     */
    private static final int LOG_SIZE_MEDIUM = 4;

    /**
     * Number of entries to keep when going fast.
     */
    private static final int LOG_SIZE_MAX = 5;

    /**
     * Below this speed, we only keep LOG_SIZE_SLOW log entries.
     */
    private static final float SPEED_MEDIUM_M_S = 10f / 3.6f;

    /**
     * Below this speed, we only keep LOG_SIZE_MEDIUM log entries.
     */
    private static final float SPEED_FAST_M_S = 20f / 3.6f;

    public static class DebugInfo {
        public LocationPair lastLocationPair;
    }

    private Location mLastLocation = null;
    private Deque<LocationPair> mLog = new ArrayDeque<LocationPair>(LOG_SIZE_MAX);
    private int mLogSize = LOG_SIZE_SLOW;
    public DebugInfo mDebugInfo = new DebugInfo();

    public void startListening() {
        LocationManager.get().addLocationListener(this);
    }

    public void stopListening() {
        LocationManager.get().removeLocationListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation != null) {
            LocationPair locationPair = new LocationPair(mLastLocation, location);
            float lastSpeed = locationPair.getSpeed();

            if (mLog.size() >= mLogSize) {
                // Make room for the new value
                mLog.removeLast();

                if (mLog.size() >= mLogSize) {
                    // Remove one item to account for the log size which depends on the last measured speed
                    mLog.removeLast();
                }
            }

            mDebugInfo.lastLocationPair = locationPair;

            if (lastSpeed < LocationManager.SPEED_MIN_THRESHOLD_M_S) {
                Log.d("Speed under threshold: rounding to 0");
                locationPair.setDistance(0);
            }
            Log.d("Adding speed:" + lastSpeed * 3.6f);
            mLog.addFirst(locationPair);

            if (lastSpeed < SPEED_MEDIUM_M_S) {
                Log.d("Slow speed: keep " + LOG_SIZE_SLOW + " values");
                mLogSize = LOG_SIZE_SLOW;
            } else if (lastSpeed < SPEED_FAST_M_S) {
                Log.d("Medium speed: keep " + LOG_SIZE_MEDIUM + " values");
                mLogSize = LOG_SIZE_MEDIUM;
            } else {
                Log.d("Fast speed: keep  " + LOG_SIZE_MAX + " values");
                mLogSize = LOG_SIZE_MAX;
            }
        }

        mLastLocation = location;
    }

    public float getSpeed() {
        Log.d("mLog=" + mLog);
        int count = 0;
        float avgSpeed = 0;
        float maxSpeed = 0;
        for (LocationPair locationPair : mLog) {
            float speed = locationPair.getSpeed();
            avgSpeed += speed;
            count++;
            if (speed > maxSpeed) maxSpeed = speed;
        }

        if (count == 0) return 0f;

        // If we have at least 3 values, remove the max (to smooth the result)
        if (count >= 3) {
            avgSpeed -= maxSpeed;
            count--;
        }

        avgSpeed /= count;
        Log.d("res=" + avgSpeed);
        if (avgSpeed < LocationManager.SPEED_MIN_THRESHOLD_M_S) {
            Log.d("Speed under threshold: return 0");
            return 0f;
        }
        return avgSpeed;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
