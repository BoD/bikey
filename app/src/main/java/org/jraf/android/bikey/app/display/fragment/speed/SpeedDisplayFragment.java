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
package org.jraf.android.bikey.app.display.fragment.speed;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import org.jraf.android.bikey.app.display.fragment.SimpleDisplayFragment;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.location.LocationManager.StatusListener;
import org.jraf.android.bikey.backend.location.Speedometer;
import org.jraf.android.bikey.common.UnitUtil;

public class SpeedDisplayFragment extends SimpleDisplayFragment {
    public static SpeedDisplayFragment newInstance() {
        return new SpeedDisplayFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Speed updates
        mSpeedometer.startListening();
    }

    @Override
    public void onStop() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Speed updates
        mSpeedometer.stopListening();
        super.onStop();
    }

    private Speedometer mSpeedometer = new Speedometer() {
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            setText(UnitUtil.formatSpeed(getSpeed()));
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            setTextEnabled(active);
        }
    };
}
