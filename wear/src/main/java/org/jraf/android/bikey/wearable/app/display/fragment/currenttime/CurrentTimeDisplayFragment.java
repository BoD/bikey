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
package org.jraf.android.bikey.wearable.app.display.fragment.currenttime;

import java.text.DateFormat;
import java.util.Date;

import android.os.Handler;

import org.jraf.android.bikey.wearable.app.display.fragment.SimpleDisplayFragment;


public class CurrentTimeDisplayFragment extends SimpleDisplayFragment {
    protected static final long REFRESH_RATE = 30 * 1000;
    private Handler mHandler = new Handler();
    private DateFormat mTimeFormat;

    public static CurrentTimeDisplayFragment newInstance() {
        return new CurrentTimeDisplayFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
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
