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
package org.jraf.android.bikey.app.mediabutton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.app.logcollectservice.LogCollectorService;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Only care if the preference is checked
        if (!sharedPreferences.getBoolean(Constants.PREF_LISTEN_TO_HEADSET_BUTTON, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) return;

        // Only care if there is a current ride
        String currentRideUriStr = sharedPreferences.getString(Constants.PREF_CURRENT_RIDE_URI, null);
        if (currentRideUriStr == null) return;

        // Only care about key down
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent != null && keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

        final Uri currentRideUri = Uri.parse(currentRideUriStr);
        new AsyncTask<Void, Void, Integer>() {
            private static final int RESULT_RIDE_DOES_NOT_EXIST = 0;
            private static final int RESULT_RIDE_ACTIVATED = 1;
            private static final int RESULT_RIDE_PAUSED = 2;


            @Override
            protected Integer doInBackground(Void... params) {
                // Check if the ride still exists (it may have been deleted)
                boolean rideExists = RideManager.get().isExistingRide(currentRideUri);
                Log.d("rideExists=" + rideExists);
                if (!rideExists) {
                    return RESULT_RIDE_DOES_NOT_EXIST;
                }

                RideState state = RideManager.get().getState(currentRideUri);
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60);
                switch (state) {
                    case CREATED:
                    case PAUSED:
                        context.startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, currentRideUri, context, LogCollectorService.class));
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                        return RESULT_RIDE_ACTIVATED;

                    case ACTIVE:
                        context.startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, currentRideUri, context, LogCollectorService.class));
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2);
                        return RESULT_RIDE_PAUSED;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                switch (result) {
                    case RESULT_RIDE_DOES_NOT_EXIST:
                        // Do not do anything
                        break;

                    case RESULT_RIDE_ACTIVATED:
                        // TODO: talk
                        break;

                    case RESULT_RIDE_PAUSED:
                        // TODO: talk
                        break;
                }
            }

        }.execute();
    }
}
