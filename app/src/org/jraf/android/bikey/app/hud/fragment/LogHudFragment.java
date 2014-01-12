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
package org.jraf.android.bikey.app.hud.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.jraf.android.bikey.backend.log.LogListener;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;

public abstract class LogHudFragment extends SimpleHudFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Ride updates
        final RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        final Uri rideUri = getRideUri();

        new AsyncTask<Void, Void, Void>() {
            private boolean mIsActive;
            private CharSequence mValue;

            @Override
            protected Void doInBackground(Void... params) {
                mIsActive = rideManager.getState(rideUri) == RideState.ACTIVE;
                mValue = queryValue();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                setTextEnabled(mIsActive);
                setText(mValue);
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        // Ride updates
        RideManager.get().removeListener(mRideListener);

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Log updates
        final LogManager logManager = LogManager.get();
        logManager.addListener(mLogListener);
    }

    @Override
    public void onStop() {
        // Log updates
        LogManager.get().removeListener(mLogListener);

        super.onStop();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            setTextEnabled(true);
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            setTextEnabled(false);
        }
    };

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(final Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            if (!isAdded()) return;

            new AsyncTask<Void, Void, Void>() {
                private CharSequence mValue;

                @Override
                protected Void doInBackground(Void... params) {
                    mValue = queryValue();
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    setText(mValue);
                }
            }.execute();
        }
    };

    @Background
    protected abstract CharSequence queryValue();
}
