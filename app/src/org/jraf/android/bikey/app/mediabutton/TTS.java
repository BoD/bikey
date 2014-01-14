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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.log.wrapper.Log;

public class TTS {
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "en", "fr" };
    private static TTS INSTANCE;
    private TextToSpeech mTts;
    private final Context mContext;
    private int mTtsStatus = TextToSpeech.ERROR;

    public synchronized static TTS get() {
        if (INSTANCE == null) INSTANCE = new TTS();
        return INSTANCE;
    }

    private TTS() {
        mContext = Application.getApplication();
    }

    public void start() {
        Log.d("start");
        if (mTts == null || mTtsStatus == TextToSpeech.ERROR) mTts = new TextToSpeech(mContext, mOnInitListener);
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    public void stop() {
        Log.d("stop");
        if (mTts != null) {
            mTts.shutdown();
            mTts = null;
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public void speak(int stringId) {
        String string = mContext.getString(stringId);
        Log.d("speak " + string);
        if (mTtsStatus == TextToSpeech.SUCCESS) {
            int result = mTts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
            Log.d("spoke, result=" + result);
        }
    }

    /**
     * Sets the language of TTS to the device language, if we support that language. Otherwise sets the TTS language to English.
     */
    private void setLanguage() {
        Log.d("setLanguage");
        Locale ttsLocale = Locale.US;
        Locale userLocale = mContext.getResources().getConfiguration().locale;
        String userLanguage = userLocale.getLanguage();
        for (String supportedLanguage : SUPPORTED_LANGUAGES) {
            if (supportedLanguage.equals(userLanguage)) {
                ttsLocale = userLocale;
                break;
            }
        }
        Log.d("setLanguage to locale " + ttsLocale);
        mTts.setLanguage(ttsLocale);

    }

    private OnInitListener mOnInitListener = new OnInitListener() {
        @Override
        public void onInit(int status) {
            Log.d("onInit: status = " + status);
            mTtsStatus = status;
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("onReceive: intent = " + intent);
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        setLanguage();
                        return null;
                    }
                }.execute();
            }

        }
    };
}
