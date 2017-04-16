/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * Copyright (C) 2017 Carmen Alvarez (c@rmen.ca)
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
package org.jraf.android.bikey.app.mediabutton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.collect.LogCollectorService;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.string.StringUtil;

public class MediaButtonHandler {

    private static final String TAG = MediaButtonHandler.class.getSimpleName();
    private MediaSessionCompat mMediaSessionCompat;

    private static final MediaButtonHandler INSTANCE = new MediaButtonHandler();

    private MediaButtonHandler() {
        // prevent instantiation
    }

    public static MediaButtonHandler get() {
        return INSTANCE;
    }

    @MainThread
    public void start(Context context) {
        Log.d();
        if (mMediaSessionCompat != null) {
            mMediaSessionCompat.setActive(false);
            mMediaSessionCompat.release();
        }
        mMediaSessionCompat = new MediaSessionCompat(context, TAG);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSessionCompat.setCallback(new BikeyMediaButtonCallback(context));
        mMediaSessionCompat.setActive(true);
    }

    @MainThread
    public void stop() {
        Log.d();
        if (mMediaSessionCompat != null) {
            mMediaSessionCompat.setActive(false);
            mMediaSessionCompat.release();
            mMediaSessionCompat = null;
        }
    }

    private static class BikeyMediaButtonCallback extends MediaSessionCompat.Callback {

        private final Context mContext;

        BikeyMediaButtonCallback(Context context) {
            mContext = context;
        }

        @Override
        public boolean onMediaButtonEvent(Intent intent) {
            Log.d("intent=" + StringUtil.toString(intent));
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            // Only care if the preference is checked
            if (!sharedPreferences.getBoolean(Constants.PREF_LISTEN_TO_HEADSET_BUTTON, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) return false;

            // Only care if there is a current ride
            Uri currentRideUri = RideManager.get().getCurrentRide();
            if (currentRideUri == null) return false;

            // Only care about key down
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null && keyEvent.getAction() != KeyEvent.ACTION_DOWN) return false;

            Single.fromCallable(() -> readRideState(currentRideUri))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rideState -> {
                        switch (rideState) {
                            case CREATED:
                            case PAUSED:
                                mContext.startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, currentRideUri, mContext, LogCollectorService.class));
                                TextToSpeechManager.get().speak(R.string.speak_activate_ride);
                                break;
                            case ACTIVE:
                                mContext.startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, currentRideUri, mContext, LogCollectorService.class));
                                TextToSpeechManager.get().speak(R.string.speak_pause_ride);
                                break;
                        }
                    });
            return true;
        }

        @WorkerThread
        private RideState readRideState(Uri currentRideUri) {
            // Check if the ride still exists (it may have been deleted)
            boolean rideExists = RideManager.get().isExistingRide(currentRideUri);
            Log.d("rideExists=" + rideExists);
            if (!rideExists) return RideState.DELETED;
            return RideManager.get().getState(currentRideUri);
        }
    }
}
