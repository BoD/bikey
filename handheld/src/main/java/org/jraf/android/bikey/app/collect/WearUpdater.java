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
package org.jraf.android.bikey.app.collect;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import org.jraf.android.bikey.backend.location.Speedometer;
import org.jraf.android.bikey.backend.log.LogListener;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.wear.WearCommHelper;

public class WearUpdater {
    private WearCommHelper mWearCommHelper = WearCommHelper.get();
    private Uri mActiveRideUri;
    private float mLastSpeed;
    private long mInitialDuration;
    private long mActivatedDate;

    public void startUpdates(Context context) {
        // Ride updates
        RideManager.get().addListener(mRideListener);

        // Log updates
        LogManager.get().addListener(mLogListener);

        // Speed updates
        mSpeedometer.startListening();

        // Propagate unit preferences at this point
        mWearCommHelper.updatePreferences();
    }

    public void stopUpdates() {
        // Ride updates
        RideManager.get().removeListener(mRideListener);

        // Log updates
        LogManager.get().removeListener(mLogListener);

        // Speed updates
        mSpeedometer.stopListening();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(Uri rideUri) {
            mActiveRideUri = rideUri;
            mInitialDuration = RideManager.get().getDuration(rideUri);
            mActivatedDate = RideManager.get().getActivatedDate(rideUri).getTime();
        }

        @Override
        public void onPaused(Uri rideUri) {
            mActiveRideUri = null;
            // TODO update notif maybe?
        }
    };

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(Uri rideUri) {
            float distance = LogManager.get().getTotalDistance(rideUri);
            long duration = mInitialDuration + (System.currentTimeMillis() - mActivatedDate);
            mWearCommHelper.updateRideValues(true, duration, mLastSpeed, distance, 0); // TODO heart rate
        }
    };

    private Speedometer mSpeedometer = new Speedometer() {
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            mLastSpeed = getSpeed();
        }
    };
}