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
package org.jraf.android.bikey.backend.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;
import org.jraf.android.util.log.Log;

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
                final float value = 1f - (float) (azimuth / (2 * Math.PI));

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
