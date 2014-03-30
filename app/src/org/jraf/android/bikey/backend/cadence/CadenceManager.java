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
package org.jraf.android.bikey.backend.cadence;

import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.math.MathUtil;

public class CadenceManager {
    private static final CadenceManager INSTANCE = new CadenceManager();

    private static final int LOG_SIZE = 1000;
    private static final long BROADCAST_CURRENT_VALUE_RATE_S = 2;

    private static class Entry {
        long timestamp;
        float value;

        Entry(float value) {
            this.value = value;
            timestamp = System.currentTimeMillis();
        }
    }

    public static CadenceManager get() {
        return INSTANCE;
    }

    private Context mContext;
    private ArrayDeque<Entry> mValues = new ArrayDeque<Entry>(LOG_SIZE);
    private ScheduledExecutorService mScheduledExecutorService;
    protected Float mLastValue;

    private Listeners<CadenceListener> mListeners = new Listeners<CadenceListener>() {
        @Override
        protected void onFirstListener() {
            startListening();
        }

        @Override
        protected void onNoMoreListeners() {
            stopListening();
        }
    };


    private CadenceManager() {
        mContext = Application.getApplication();
    }

    public void addListener(CadenceListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(CadenceListener listener) {
        mListeners.remove(listener);
    }

    protected void startListening() {
        Log.d();
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mRotationSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);

        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        mScheduledExecutorService.scheduleAtFixedRate(mBroadcastCurrentValueRunnable, BROADCAST_CURRENT_VALUE_RATE_S, BROADCAST_CURRENT_VALUE_RATE_S,
                TimeUnit.SECONDS);
    }

    protected void stopListening() {
        Log.d();
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mRotationSensorEventListener);
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    private SensorEventListener mRotationSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mValues.size() >= LOG_SIZE) {
                // Make room for the new value
                mValues.removeFirst();
            }
            float value = event.values[0]; // XXX: using this value and not the other ones is arbitrary
            synchronized (mValues) {
                mValues.add(new Entry(value));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private float[] getValuesAsFloatArray() {
        float[] res = new float[mValues.size()];
        int i = 0;

        for (Entry e : mValues) {
            res[i++] = e.value;
        }
        return res;
    }

    /**
     * Return the current cadence, in revolutions per minute.
     */
    public Float getCurrentCadence() {
        if (mListeners.size() == 0) throw new IllegalStateException("There must be at least one listener prior to calling getCurrentCadence");
        float[] valuesAsFloats;
        long durationMs;
        int len;
        synchronized (mValues) {
            len = mValues.size();
            if (len < 2) return null;
            valuesAsFloats = getValuesAsFloatArray();
            durationMs = mValues.peekLast().timestamp - mValues.peekFirst().timestamp;
        }

        float average = MathUtil.getAverage(valuesAsFloats);
        int count = 0;
        for (int i = 1; i < len; i++) {
            if (valuesAsFloats[i - 1] < average && valuesAsFloats[i] >= average) {
                count++;
            }
        }

        float revPerMs = count / (float) durationMs;
        float revPerMin = revPerMs * 60000f;

        // TODO: sanity checks

        return revPerMin;
    }

    private Runnable mBroadcastCurrentValueRunnable = new Runnable() {
        @Override
        public void run() {
            final Float value = getCurrentCadence();
            if (value != null && !value.equals(mLastValue)) {
                mLastValue = value;
                mListeners.dispatch(new Dispatcher<CadenceListener>() {
                    @Override
                    public void dispatch(CadenceListener listener) {
                        listener.onCadenceChanged(value);
                    }
                });
            }
        }
    };
}
