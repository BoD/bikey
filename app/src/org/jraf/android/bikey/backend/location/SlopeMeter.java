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
import android.location.LocationListener;
import android.os.Bundle;

public class SlopeMeter implements LocationListener {
    private static final float MIN_DISTANCE = 100f;

    public static class DebugInfo {
        public LocationPair lastLocationPair;
    }

    private Location mLastLocation = null;
    public DebugInfo mDebugInfo = new DebugInfo();
    private LocationPair mLocationPair;

    public void startListening() {
        LocationManager.get().addLocationListener(this);
    }

    public void stopListening() {
        LocationManager.get().removeLocationListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation == null) {
            mLastLocation = location;
        } else {
            LocationPair locationPair = new LocationPair(mLastLocation, location);
            if (locationPair.getDistance() >= MIN_DISTANCE) {
                mLocationPair = locationPair;
                mDebugInfo.lastLocationPair = mLocationPair;
                mLastLocation = location;
            }
        }

    }

    public float getSlope() {
        if (mLocationPair == null) return 0;
        return mLocationPair.getSlope();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
