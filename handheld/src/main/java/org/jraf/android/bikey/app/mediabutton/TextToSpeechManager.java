/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Carmen Alvarez
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
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.log.wrapper.Log;

import java.util.Locale;

public class TextToSpeechManager {
    private static final TextToSpeechManager INSTANCE = new TextToSpeechManager();

    public static TextToSpeechManager get() {
        return INSTANCE;
    }

    private static final String[] SUPPORTED_LANGUAGES = new String[] { "en" };

    private TextToSpeech mTextToSpeech;
    private final Context mContext;
    private int mStatus = TextToSpeech.ERROR;

    private TextToSpeechManager() {
        mContext = Application.getApplication();
    }

    public void start() {
        Log.d();
        if (mTextToSpeech == null || mStatus == TextToSpeech.ERROR) {
            mTextToSpeech = new TextToSpeech(mContext, mOnInitListener);
        }
    }

    private OnInitListener mOnInitListener = new OnInitListener() {
        @Override
        public void onInit(int status) {
            Log.d("status=" + status);
            mStatus = status;
            mTextToSpeech.addEarcon(mContext.getString(R.string.speak_activate_ride), mContext.getPackageName(), R.raw.start);
            mTextToSpeech.addEarcon(mContext.getString(R.string.speak_pause_ride), mContext.getPackageName(), R.raw.stop);
        }
    };

    public void stop() {
        Log.d();
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    public boolean speak(int stringId) {
        start();
        String string = mContext.getString(stringId);
        Log.d("string=" + string);
        if (mStatus == TextToSpeech.SUCCESS) {
            int result;
            if (isDeviceLanguageSupported()) {
                result = mTextToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("spoke, result=" + result);
            } else {
                result = mTextToSpeech.playEarcon(string, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("played earcon, result=" + result);
            }
            return result == TextToSpeech.SUCCESS;
        }
        return false;
    }

    private boolean isDeviceLanguageSupported() {
        Locale userLocale = mContext.getResources().getConfiguration().locale;
        String userLanguage = userLocale.getLanguage();
        for (String supportedLanguage : SUPPORTED_LANGUAGES) {
            if (supportedLanguage.equals(userLanguage)) return true;
        }
        return false;
    }
}
