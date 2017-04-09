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
package org.jraf.android.bikey.app.display.fragment.elapsedtime;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.widget.Chronometer;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.fragment.SimpleDisplayFragment;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ElapsedTimeDisplayFragment extends SimpleDisplayFragment {
    private Chronometer mChronometer;

    public static ElapsedTimeDisplayFragment newInstance() {
        return new ElapsedTimeDisplayFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.display_elapsed_time;
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
        if (rideUri == null) return;


        Single.fromCallable(() -> readRideDurationInfo(rideUri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rideDurationInfo -> {
                    if (rideDurationInfo.isActive) {
                        long additionalDuration = System.currentTimeMillis() - rideDurationInfo.activatedDate;
                        setChronometerDuration(rideDurationInfo.duration + additionalDuration);
                        mChronometer.setEnabled(true);
                        mChronometer.start();
                    } else {
                        setChronometerDuration(rideDurationInfo.duration);
                    }
                });
    }

    private static class RideDurationInfo {
        long duration;
        boolean isActive;
        long activatedDate;
    }

    @WorkerThread
    private RideDurationInfo readRideDurationInfo(Uri rideUri) {
        RideDurationInfo result = new RideDurationInfo();
        final RideManager rideManager = RideManager.get();
        result.duration = rideManager.getDuration(rideUri);
        result.isActive = rideManager.getState(rideUri) == RideState.ACTIVE;
        result.activatedDate = result.isActive ? rideManager.getActivatedDate(rideUri).getTime() : 0;
        return result;
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

            Single.fromCallable(() -> RideManager.get().getDuration(rideUri))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(duration -> {
                        setChronometerDuration(duration);
                        mChronometer.start();
                        mChronometer.setEnabled(true);
                    });
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            mChronometer.stop();
            mChronometer.setEnabled(false);
        }
    };
}
