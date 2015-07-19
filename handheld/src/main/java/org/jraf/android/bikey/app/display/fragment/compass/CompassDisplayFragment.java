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
package org.jraf.android.bikey.app.display.fragment.compass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.bikey.backend.compass.CompassListener;
import org.jraf.android.bikey.backend.compass.CompassManager;
import org.jraf.android.util.app.base.BaseFragment;
import org.jraf.android.util.log.wrapper.Log;

public class CompassDisplayFragment extends BaseFragment<DisplayActivity> {
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    public static CompassDisplayFragment newInstance() {
        return new CompassDisplayFragment();
    }

    private ImageView mImgCompass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.display_compass, container, false);
        mImgCompass = (ImageView) res.findViewById(R.id.imgCompass);
        return res;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isHidden()) CompassManager.get().addListener(mCompassListener);
    }

    @Override
    public void onStop() {
        CompassManager.get().removeListener(mCompassListener);
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("hidden=" + hidden);
        if (hidden) {
            CompassManager.get().removeListener(mCompassListener);
        } else {
            CompassManager.get().addListener(mCompassListener);
        }
    }

    private CompassListener mCompassListener = new CompassListener() {
        @Override
        public void onCompassChange(float compass) {
            float currentRotation = mImgCompass.getRotation();
            float newRotation = compass * 360f;
            if (newRotation - currentRotation < 180) {
                mImgCompass.animate().setDuration(CompassManager.RATE).setInterpolator(LINEAR_INTERPOLATOR).rotation(newRotation);
            } else {
                mImgCompass.animate().setDuration(CompassManager.RATE).setInterpolator(LINEAR_INTERPOLATOR).rotation(newRotation - 360);
            }
        }
    };
}
