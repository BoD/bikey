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
package org.jraf.android.bikey.app.hud.fragment.compass;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.compass.CompassListener;
import org.jraf.android.bikey.backend.compass.CompassManager;
import org.jraf.android.util.log.wrapper.Log; 

public class CompassHudFragment extends Fragment {
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    public static CompassHudFragment newInstance() {
        return new CompassHudFragment();
    }

    private ImageView mImgCompass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.hud_compass, container, false);
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
            //            mImgCompass.setRotation(compass * 360f);
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
