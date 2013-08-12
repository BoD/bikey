package org.jraf.android.bike.backend.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import org.jraf.android.bike.app.Application;
import org.jraf.android.util.Listeners;
import org.jraf.android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationManager {
    public static interface StatusListener {
        void onStatusChanged(boolean active);
    }

    public static interface ActivityRecognitionListener {
        void onActivityRecognized(int activityType, int confidence);
    }

    private static final LocationManager INSTANCE = new LocationManager();

    private static final boolean DEBUG_USE_DEVICE_GPS = false;
    private static final int INTERVAL_LOC_REQUEST = 1000;
    private static final int ALLOWED_LOC_MISSES = 8;

    protected static final float ACCURACY_THRESHOLD_M = 20;

    private static final int IGNORE_LOCATION_COUNT = 7;

    public static LocationManager get() {
        return INSTANCE;
    }

    private final Context mContext;
    private LocationClient mLocationClient;
    private ActivityRecognitionClient mActivityRecognitionClient;
    protected long mLastFixDate;
    private Handler mHandler;
    private boolean mActive = false;
    private int mCurrentActivityType;
    private int mCurrentActivityConfidence;
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
        if (DEBUG_USE_DEVICE_GPS) {
            mIgnoreLocationCount = IGNORE_LOCATION_COUNT;
            android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(mGpsLocationListener);
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, INTERVAL_LOC_REQUEST, 0, mGpsLocationListener);
        } else {
            // If already connected (or connecting) do nothing
            if (mLocationClient != null) {
                Log.d("Already connected: do nothing");
                return;
            }
            mIgnoreLocationCount = IGNORE_LOCATION_COUNT;
            mLocationClient = new LocationClient(mContext, mLocationOnConnectionCallbacks, mLocationOnConnectionFailedListener);
            mLocationClient.connect();
        }
    }

    private void stopLocationListener() {
        Log.d();
        if (DEBUG_USE_DEVICE_GPS) {
            android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(mGpsLocationListener);
        } else {
            if (mLocationClient == null) return;
            mLocationClient.removeLocationUpdates(mLocationListener);
            if (mLocationClient.isConnected()) mLocationClient.disconnect();
            mLocationClient = null;
        }
    }

    private ConnectionCallbacks mLocationOnConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle params) {
            Log.d();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(INTERVAL_LOC_REQUEST);
            locationRequest.setFastestInterval(INTERVAL_LOC_REQUEST);
            mLocationClient.requestLocationUpdates(locationRequest, mLocationListener);
        }

        @Override
        public void onDisconnected() {
            Log.d();
        }
    };

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
        public void onLocationChanged(Location location) {
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
            for (LocationListener listener : mLocationListeners) {
                listener.onLocationChanged(location);
            }
        }
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
            Log.d();
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

    protected void setActive(boolean active) {
        if (mActive != active) {
            // Dispatch the change
            for (StatusListener listener : mStatusListeners) {
                listener.onStatusChanged(active);
            }
        }
        mActive = active;
    }


    /*
     * Activity.
     */

    public void addActivityRecognitionListener(ActivityRecognitionListener listener) {
        mActivityRecognitionListeners.add(listener);
    }

    public void removeActivityRecognitionListener(ActivityRecognitionListener listener) {
        mActivityRecognitionListeners.remove(listener);
    }

    private Listeners<ActivityRecognitionListener> mActivityRecognitionListeners = new Listeners<ActivityRecognitionListener>() {
        @Override
        protected void onFirstListener() {
            Log.d("First activity listener, start activity listener");
            startActivityRecognitionListener();
        }

        @Override
        protected void onNoMoreListeners() {
            Log.d("No more activity listeners, stop activity listener");
            stopActivityRecognitionListener();
        }
    };

    private void startActivityRecognitionListener() {
        Log.d();
        // If already connected (or connecting) do nothing
        if (mActivityRecognitionClient != null) {
            Log.d("Already connected: do nothing");
            return;
        }
        mCurrentActivityType = -1;
        mCurrentActivityConfidence = -1;
        mActivityRecognitionClient = new ActivityRecognitionClient(mContext, mActivityOnConnectionCallbacks, mLocationOnConnectionFailedListener);
        mActivityRecognitionClient.connect();
    }

    private void stopActivityRecognitionListener() {
        Log.d();
        if (mActivityRecognitionClient == null) return;
        mActivityRecognitionClient.removeActivityUpdates(getActivityRecognitionPendingIntent());
        if (mActivityRecognitionClient.isConnected()) mActivityRecognitionClient.disconnect();
        mActivityRecognitionClient = null;
    }

    private ConnectionCallbacks mActivityOnConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle params) {
            Log.d();
            mActivityRecognitionClient.requestActivityUpdates(INTERVAL_LOC_REQUEST, getActivityRecognitionPendingIntent());
        }

        @Override
        public void onDisconnected() {
            Log.d();
        }
    };

    private OnConnectionFailedListener mLocationOnConnectionFailedListener = new OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.w("Could not connect to LocationClient, errorCode=" + connectionResult.getErrorCode());
        }
    };

    private PendingIntent getActivityRecognitionPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /* package */void onActivityRecognized(int activityType, int confidence) {
        if (mCurrentActivityType != activityType && mCurrentActivityConfidence != confidence) {
            // Dispatch the change
            for (ActivityRecognitionListener listener : mActivityRecognitionListeners) {
                listener.onActivityRecognized(activityType, confidence);
            }
        }
    }
}
