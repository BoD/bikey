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
package org.jraf.android.bikey.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.app.mediabutton.MediaButtonHandler;
import org.jraf.android.bikey.app.mediabutton.TextToSpeechManager;

public class MediaButtonUtil {
    public static void registerMediaButtonEventReceiverAccordingToPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Only care if the preference is checked
        if (sharedPreferences.getBoolean(Constants.PREF_LISTEN_TO_HEADSET_BUTTON, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
            registerMediaButtonEventReceiver(context);
        } else {
            unregisterMediaButtonEventReceiver();
        }
    }

    public static void registerMediaButtonEventReceiver(Context context) {
        MediaButtonHandler.get().start(context);
        TextToSpeechManager.get().start();
    }

    public static void unregisterMediaButtonEventReceiver() {
        MediaButtonHandler.get().stop();
        TextToSpeechManager.get().stop();
    }
}
