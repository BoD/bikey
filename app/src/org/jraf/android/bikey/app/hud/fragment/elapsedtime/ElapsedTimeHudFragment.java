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
package org.jraf.android.bikey.app.hud.fragment.elapsedtime;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.hud.fragment.SimpleHudFragment;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;

public class ElapsedTimeHudFragment extends SimpleHudFragment {
    private Chronometer mChronometer;

    public static ElapsedTimeHudFragment newInstance() {
        return new ElapsedTimeHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_elapsed_time;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChronometer = (Chronometer) view.findViewById(R.id.chronometer);
    }

    private void setChronometerDuration(long duration) {
        mChronometer.setBase(SystemClock.elapsedRealtime() - duration);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Ride updates
        final RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        final Uri rideUri = getRideUri();

        new AsyncTask<Void, Void, Void>() {
            private long mDuration;
            private boolean mIsActive;
            private long mActivatedDate;

            @Override
            protected Void doInBackground(Void... params) {
                mDuration = rideManager.getDuration(rideUri);
                mIsActive = rideManager.getState(rideUri) == RideState.ACTIVE;
                if (mIsActive) {
                    mActivatedDate = rideManager.getActivatedDate(rideUri).getTime();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mIsActive) {
                    long additionalDuration = System.currentTimeMillis() - mActivatedDate;
                    setChronometerDuration(mDuration + additionalDuration);
                    mChronometer.setEnabled(true);
                    mChronometer.start();
                } else {
                    setChronometerDuration(mDuration);
                }
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        // Ride updates
        RideManager.get().removeListener(mRideListener);
        super.onDestroy();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(final Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;

            new AsyncTask<Void, Void, Void>() {
                private long mDuration;

                @Override
                protected Void doInBackground(Void... params) {
                    mDuration = RideManager.get().getDuration(rideUri);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    setChronometerDuration(mDuration);
                    mChronometer.start();
                    mChronometer.setEnabled(true);
                }
            }.execute();
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            mChronometer.stop();
            mChronometer.setEnabled(false);
        }
    };
}
