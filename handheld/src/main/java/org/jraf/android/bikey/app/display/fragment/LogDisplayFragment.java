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
package org.jraf.android.bikey.app.display.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.bikey.backend.log.LogListener;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class LogDisplayFragment extends SimpleDisplayFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Ride updates
        final RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        final Uri rideUri = getRideUri();
        if (rideUri == null) return;

        Single.fromCallable(() -> readLogDisplayInfo(rideUri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(logDisplayInfo -> {
                    setTextEnabled(logDisplayInfo.isActive);
                    setText(logDisplayInfo.value);
                });

    }

    private static class LogDisplayInfo {
        boolean isActive;
        CharSequence value;
    }

    @WorkerThread
    private LogDisplayInfo readLogDisplayInfo(Uri rideUri) {
        LogDisplayInfo result = new LogDisplayInfo();
        result.isActive = RideManager.get().getState(rideUri) == RideState.ACTIVE;
        result.value = queryValue();
        return result;
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

    private LogListener mLogListener = rideUri -> {
        if (!rideUri.equals(getRideUri())) return;
        if (!isAdded()) return;

        Maybe.fromCallable(this::queryValue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setText);
    };

    @WorkerThread
    protected abstract @Nullable CharSequence queryValue();
}
