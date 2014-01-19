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

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.log.wrapper.Log;

public class TextToSpeachManager {
    private static final TextToSpeachManager INSTANCE = new TextToSpeachManager();

    public static TextToSpeachManager get() {
        return INSTANCE;
    }

    private static final String[] SUPPORTED_LANGUAGES = new String[] { "en" };

    private TextToSpeech mTextToSpeech;
    private final Context mContext;
    private int mStatus = TextToSpeech.ERROR;

    private TextToSpeachManager() {
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
