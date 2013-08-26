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
package org.jraf.android.bikey.backend.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.Listeners;
import org.jraf.android.util.Listeners.Dispatcher;
import org.jraf.android.util.Log;

public class LocationManager {
    public static interface StatusListener {
        void onStatusChanged(boolean active);
    }

    public static interface ActivityRecognitionListener {
        void onActivityRecognized(int activityType, int confidence);
    }

    private static final LocationManager INSTANCE = new LocationManager();

    private static final int INTERVAL_LOC_REQUEST = 1000;
    private static final int ALLOWED_LOC_MISSES = 8;

    protected static final float ACCURACY_THRESHOLD_M = 20;

    private static final int IGNORE_LOCATION_COUNT = 7;

    /**
     * Speeds below this value will be reported as 0 (because of GPS low precision).
     */
    public static final float SPEED_MIN_THRESHOLD_M_S = 2.2f / 3.6f;


    public static LocationManager get() {
        return INSTANCE;
    }

    private final Context mContext;
    protected long mLastFixDate;
    private Handler mHandler;
    private boolean mActive = false;
    private int mIgnoreLocationCount = IGNORE_LOCATION_COUNT;

    private LocationManager() {
        mContext = Application.getApplication();
    }


    /*
     * Location.
     */

    public void addLocationListener(LocationListener listener) {
        mLocationListeners.add(listener);
    }

    public void removeLocationListener(LocationListener listener) {
        mLocationListeners.remove(listener);
    }

    private Listeners<LocationListener> mLocationListeners = new Listeners<LocationListener>() {
        @Override
        protected void onFirstListener() {
            Log.d("First location listener, start location listener");
            startLocationListener();
        }

        @Override
        protected void onNoMoreListeners() {
            Log.d("No more location listeners, stop location listener");
            stopLocationListener();
        }
    };

    private void startLocationListener() {
        Log.d();
        mIgnoreLocationCount = IGNORE_LOCATION_COUNT;
        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(mGpsLocationListener);
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, INTERVAL_LOC_REQUEST, 0, mGpsLocationListener);
    }

    private void stopLocationListener() {
        Log.d();
        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(mGpsLocationListener);
    }

    private android.location.LocationListener mGpsLocationListener = new android.location.LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("status=" + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("provider=" + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("provider=" + provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocationListener.onLocationChanged(location);
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("location=" + location);
            if (location.hasAccuracy() && location.getAccuracy() > ACCURACY_THRESHOLD_M) {
                Log.d("Accuracy above threshold: ignore location");
                return;
            }

            mIgnoreLocationCount--;
            if (mIgnoreLocationCount >= 0) {
                Log.d("Ignore first few locations");
                return;
            }

            // Dispatch to listeners
            mLocationListeners.dispatch(new Dispatcher<LocationListener>() {
                @Override
                public void dispatch(LocationListener listener) {
                    listener.onLocationChanged(location);
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };


    /*
     * Gps status.
     */

    public void addStatusListener(StatusListener listener) {
        mStatusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        mStatusListeners.remove(listener);
    }

    private Listeners<StatusListener> mStatusListeners = new Listeners<StatusListener>() {
        @Override
        protected void onFirstListener() {
            Log.d("First status listener, start gps location listener");
            startGpsLocationListener();
        }

        @Override
        protected void onNoMoreListeners() {
            Log.d("No more status listeners, stop gps location listener");
            stopGpsLocationListener();
        }
    };

    private void startGpsLocationListener() {
        Log.d();
        setActive(false);
        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(mGpsStatusLocationListener);
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, INTERVAL_LOC_REQUEST, 0, mGpsStatusLocationListener);
    }

    private void stopGpsLocationListener() {
        Log.d();
        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(mGpsStatusLocationListener);
    }

    private android.location.LocationListener mGpsStatusLocationListener = new android.location.LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("status=" + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("provider=" + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("provider=" + provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastFixDate = System.currentTimeMillis();
            // We just received a fix so we're active
            setActive(true);

            // Schedule to check if we're still active
            getHandler().removeCallbacks(mCheckForActiveRunnable);
            getHandler().postDelayed(mCheckForActiveRunnable, INTERVAL_LOC_REQUEST * ALLOWED_LOC_MISSES);
        }
    };

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    protected Runnable mCheckForActiveRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - mLastFixDate >= INTERVAL_LOC_REQUEST * ALLOWED_LOC_MISSES) {
                setActive(false);
            }
        }
    };

    protected void setActive(final boolean active) {
        if (mActive != active) {
            // Dispatch to listeners
            mStatusListeners.dispatch(new Dispatcher<StatusListener>() {
                @Override
                public void dispatch(StatusListener listener) {
                    listener.onStatusChanged(active);
                }
            });
        }
        mActive = active;
    }
}
