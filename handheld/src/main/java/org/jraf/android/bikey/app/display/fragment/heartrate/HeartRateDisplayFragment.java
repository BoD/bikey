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
package org.jraf.android.bikey.app.display.fragment.heartrate;

import org.jraf.android.bikey.app.display.fragment.SimpleDisplayFragment;
import org.jraf.android.bikey.backend.heartrate.HeartRateListener;
import org.jraf.android.bikey.backend.heartrate.HeartRateManager;
import org.jraf.android.bikey.common.UnitUtil;

public class HeartRateDisplayFragment extends SimpleDisplayFragment {
    public static HeartRateDisplayFragment newInstance() {
        return new HeartRateDisplayFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        // HeartRate
        HeartRateManager heartRateManager = HeartRateManager.get();
        heartRateManager.addListener(mHeartRateListener);
        if (heartRateManager.isConnecting()) {
            mHeartRateListener.onConnecting();
        } else if (heartRateManager.isConnected()) {
            mHeartRateListener.onConnected();
        } else {
            mHeartRateListener.onHeartRateChange(heartRateManager.getLastValue());
            mHeartRateListener.onDisconnected();
        }
    }

    @Override
    public void onStop() {
        // HeartRate
        HeartRateManager.get().removeListener(mHeartRateListener);
        super.onStop();
    }

    private HeartRateListener mHeartRateListener = new HeartRateListener() {
        @Override
        public void onConnecting() {}

        @Override
        public void onConnected() {
            setTextEnabled(true);
        }

        @Override
        public void onHeartRateChange(int bpm) {
            setText(UnitUtil.formatHeartRate(bpm));
        }

        @Override
        public void onDisconnected() {
            setText("    -    ");
            setTextEnabled(false);
        }

        @Override
        public void onError() {
            onDisconnected();
        }
    };
}
