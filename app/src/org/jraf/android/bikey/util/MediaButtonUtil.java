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
package org.jraf.android.bikey.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.app.mediabutton.MediaButtonReceiver;
import org.jraf.android.bikey.app.mediabutton.TextToSpeechManager;

public class MediaButtonUtil {
    public static void registerMediaButtonEventReceiverAccordingToPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Only care if the preference is checked
        if (sharedPreferences.getBoolean(Constants.PREF_LISTEN_TO_HEADSET_BUTTON, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
            registerMediaButtonEventReceiver(context);
        } else {
            unregisterMediaButtonEventReceiver(context);
        }
    }


    public static void registerMediaButtonEventReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ComponentName mediaButtonReceiverComponentName = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(mediaButtonReceiverComponentName);
        TextToSpeechManager.get().start();
    }

    public static void unregisterMediaButtonEventReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ComponentName mediaButtonReceiverComponentName = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(mediaButtonReceiverComponentName);
        TextToSpeechManager.get().stop();
    }
}
