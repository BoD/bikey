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
