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
package org.jraf.android.bikey.app.hud.fragment.currenttime;

import java.text.DateFormat;
import java.util.Date;

import android.os.Handler;

import org.jraf.android.bikey.app.hud.fragment.SimpleHudFragment;

public class CurrentTimeHudFragment extends SimpleHudFragment {
    protected static final long REFRESH_RATE = 30 * 1000;
    private Handler mHandler = new Handler();
    private DateFormat mTimeFormat;

    public static CurrentTimeHudFragment newInstance() {
        return new CurrentTimeHudFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
        setTextEnabled(true);
        mHandler.post(mShowTimeRunnable);
    }

    @Override
    public void onStop() {
        mHandler.removeCallbacks(mShowTimeRunnable);
        super.onStop();
    }

    private Runnable mShowTimeRunnable = new Runnable() {
        @Override
        public void run() {
            setText(mTimeFormat.format(new Date()));
            mHandler.postDelayed(mShowTimeRunnable, REFRESH_RATE);
        }
    };
}
