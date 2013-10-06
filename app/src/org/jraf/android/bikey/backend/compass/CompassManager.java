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
package org.jraf.android.bikey.backend.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.Listeners;
import org.jraf.android.util.Listeners.Dispatcher;
import org.jraf.android.util.Log;

public class CompassManager {
    private static final CompassManager INSTANCE = new CompassManager();
    public static final int RATE = 400;

    public static CompassManager get() {
        return INSTANCE;
    }

    private Context mContext;
    private Listeners<CompassListener> mListeners = new Listeners<CompassListener>() {
        @Override
        protected void onFirstListener() {
            startListening();
        }

        @Override
        protected void onNoMoreListeners() {
            stopListening();
        }
    };

    protected float[] mLastAccelerometerValues;

    private CompassManager() {
        mContext = Application.getApplication();
    }

    public void addListener(CompassListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(CompassListener listener) {
        mListeners.remove(listener);
    }

    protected void startListening() {
        Log.d();
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mAccelerometerSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(mMagneticFieldSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }

    protected void stopListening() {
        Log.d();
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mAccelerometerSensorEventListener);
        sensorManager.unregisterListener(mMagneticFieldSensorEventListener);
    }

    private SensorEventListener mAccelerometerSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastAccelerometerValues = event.values.clone();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private SensorEventListener mMagneticFieldSensorEventListener = new SensorEventListener() {
        private float[] mInR = new float[16];
        private float[] mOutR = new float[16];
        private long mLastDate;

        @Override
        public void onSensorChanged(SensorEvent event) {
            //            Log.d();
            //            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
            if (System.currentTimeMillis() - mLastDate < RATE) return;
            float[] values = event.values.clone();
            mLastDate = System.currentTimeMillis();

            if (mLastAccelerometerValues == null) return;
            boolean ok = SensorManager.getRotationMatrix(mInR, null, mLastAccelerometerValues, values);
            if (ok) {
                SensorManager.remapCoordinateSystem(mInR, SensorManager.AXIS_X, SensorManager.AXIS_Z, mOutR);
                float[] deviceOrientation = new float[3];
                SensorManager.getOrientation(mOutR, deviceOrientation);
                float azimuth = deviceOrientation[0];
                final float value = (float) (azimuth / (2 * Math.PI)) + .5f;

                mListeners.dispatch(new Dispatcher<CompassListener>() {
                    @Override
                    public void dispatch(CompassListener listener) {
                        listener.onCompassChange(value);
                    }
                });
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}
