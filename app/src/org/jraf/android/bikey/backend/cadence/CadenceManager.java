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

    private static final long BROADCAST_CURRENT_VALUE_RATE_S = 2;
    protected static final long LOG_SIZE_MS = 5 * 1000;

    private static class Entry {
        long timestamp;
        float[] values;

        Entry(float[] values) {
            this.values = values;
            timestamp = System.currentTimeMillis();
        }
    }

    public static CadenceManager get() {
        return INSTANCE;
    }

    private Context mContext;
    private ArrayDeque<Entry> mValues = new ArrayDeque<Entry>(200);
    private ScheduledExecutorService mScheduledExecutorService;
    protected Float mLastValue;
    private float[][] mLastRawData;

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
            // Keep only logs for a specific duration (discard old logs)
            while (mValues.size() >= 2 && mValues.peekLast().timestamp - mValues.peekFirst().timestamp >= LOG_SIZE_MS) {
                mValues.removeFirst();
            }
            float[] values = event.values.clone();
            synchronized (mValues) {
                mValues.add(new Entry(values));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private float[][] getValuesAsFloatArray() {
        float[][] res = new float[4][mValues.size()];
        int i = 0;

        for (Entry e : mValues) {
            res[0][i] = e.values[0];
            res[1][i] = e.values[1];
            res[2][i] = e.values[2];
            // Distance to 0, 0, 0
            res[3][i] = (float) Math.sqrt(e.values[0] * e.values[0] + e.values[1] * e.values[1] + e.values[2] * e.values[2]);
            i++;
        }
        return res;
    }

    /**
     * Get the current cadence.
     * 
     * @return The current cadence in revolutions per minute, or {@code null} if the information is not available.
     */
    private Float getCurrentCadence() {
        if (mListeners.size() == 0) throw new IllegalStateException("There must be at least one listener prior to calling getCurrentCadence");
        float[][] valuesAsFloats;
        long durationMs;
        int len;
        synchronized (mValues) {
            len = mValues.size();
            if (len < 2) return null;
            valuesAsFloats = getValuesAsFloatArray();
            durationMs = mValues.peekLast().timestamp - mValues.peekFirst().timestamp;
        }
        mLastRawData = valuesAsFloats;

        float[] distanceValues = valuesAsFloats[3];
        // Average
        float average = MathUtil.getAverage(distanceValues);
        float count = 0;
        for (int i = 1; i < len; i++) {

            if (distanceValues[i - 1] < average && distanceValues[i] >= average) {
                // Going up
                count++;
            } else if (distanceValues[i - 1] > average && distanceValues[i] <= average) {
                // Going down
                count++;
            }
        }

        // We counted up AND down so we must divide by two
        count /= 2f;
        float revPerMs = count / durationMs;
        float revPerMin = revPerMs * 60000f;

        // TODO: sanity checks

        Log.d("durationMs=" + durationMs + " count=" + count + " revPerMin=" + revPerMin);

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
                        listener.onCadenceChanged(value, mLastRawData);
                    }
                });
            }
        }
    };
}
