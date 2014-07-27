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
package org.jraf.android.bikey.wearable.app.display.fragment.elapsedtime;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.wearable.app.display.fragment.SimpleDisplayFragment;

public class ElapsedTimeDisplayFragment extends SimpleDisplayFragment {
    private Chronometer mChronometer;
    private long mRideStartDateOffset;

    public static ElapsedTimeDisplayFragment newInstance() {
        return new ElapsedTimeDisplayFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.display_elapsed_time;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChronometer = (Chronometer) view.findViewById(R.id.chronometer);
    }

    public void setStartDateOffset(long rideStartDateOffset) {
        if (mRideStartDateOffset != rideStartDateOffset) {
            mRideStartDateOffset = rideStartDateOffset;

            mChronometer.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() + mRideStartDateOffset));
            mChronometer.start();
        }
    }
}
