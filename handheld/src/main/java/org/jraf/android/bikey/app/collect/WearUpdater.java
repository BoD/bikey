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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.jraf.android.bikey.backend.location.Speedometer;
import org.jraf.android.bikey.backend.log.LogListener;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.util.log.wrapper.Log;

public class WearUpdater {
    private static final long SEND_VALUES_RATE_S = 2;
    private WearCommHelper mWearCommHelper = WearCommHelper.get();
    private Uri mActiveRideUri;
    private long mInitialDuration;
    private long mActivatedDate;
    private ScheduledExecutorService mScheduledExecutorService;
    private Speedometer mSpeedometer = new Speedometer();

    public void startUpdates(Context context) {
        Log.d();

        // Ride updates
        RideManager.get().addListener(mRideListener);

        // Log updates
        LogManager.get().addListener(mLogListener);

        // Speed updates
        mSpeedometer.startListening();

        // Propagate unit preferences at this point
        mWearCommHelper.updatePreferences();

        // Inform wearables that there is an ongoing ride
        mWearCommHelper.updateRideOngoing(true);

        // Start the scheduled task
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        mScheduledExecutorService.scheduleAtFixedRate(mSendValueRunnable, SEND_VALUES_RATE_S, SEND_VALUES_RATE_S, TimeUnit.SECONDS);
    }

    public void stopUpdates() {
        Log.d();

        // Ride updates
        RideManager.get().removeListener(mRideListener);

        // Log updates
        LogManager.get().removeListener(mLogListener);

        // Speed updates
        mSpeedometer.stopListening();

        // Inform wearables that there are no ongoing rides
        mWearCommHelper.updateRideOngoing(false);

        // Stop the scheduled task
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    private Runnable mSendValueRunnable = new Runnable() {
        @Override
        public void run() {
            if (mActiveRideUri == null) return;
            float totalDistance = LogManager.get().getTotalDistance(mActiveRideUri);
            long startDateOffset = mInitialDuration - mActivatedDate;
            float speed = mSpeedometer.getSpeed();
            mWearCommHelper.updateRideValues(startDateOffset, speed, totalDistance, 0); // TODO heart rate
        }
    };


    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(final Uri rideUri) {
            mActiveRideUri = rideUri;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mInitialDuration = RideManager.get().getDuration(rideUri);
                    mActivatedDate = RideManager.get().getActivatedDate(rideUri).getTime();
                    return null;
                }
            }.execute();
        }

        @Override
        public void onPaused(Uri rideUri) {
            mActiveRideUri = null;
        }
    };

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(Uri rideUri) {
        }
    };
}