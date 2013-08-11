package org.jraf.android.bike.backend.location;

import java.util.HashSet;
import java.util.Set;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import org.jraf.android.bike.app.Application;
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
    private static final int ALLOWED_LOC_MISSES = 4;

    public static LocationManager get() {
        return INSTANCE;
    }

    private final Context mContext;
    private Set<LocationListener> mLocationListeners = new HashSet<LocationListener>(3);
    private Set<StatusListener> mStatusListeners = new HashSet<StatusListener>(3);
    private Set<ActivityRecognitionListener> mActivityRecognitionListeners = new HashSet<ActivityRecognitionListener>(3);
    private LocationClient mLocationClient;
    private ActivityRecognitionClient mActivityRecognitionClient;
    protected long mLastFixDate;
    private Handler mHandler;
    private boolean mActive = false;
    private int mCurrentActivityType;
    private int mCurrentActivityConfidence;

    private LocationManager() {
        mContext = Application.getApplication();
    }

    public void addLocationListener(LocationListener listener) {
        int prevSize = mLocationListeners.size();
        mLocationListeners.add(listener);
        locationListenersCountChanged(prevSize, mLocationListeners.size());
    }

    public void removeLocationListener(LocationListener listener) {
        int prevSize = mLocationListeners.size();
        mLocationListeners.remove(listener);
        locationListenersCountChanged(prevSize, mLocationListeners.size());
    }

    private void locationListenersCountChanged(int prevSize, int newSize) {
        if (newSize == 1 && prevSize == 0) {
            Log.d("First location listener, start location listener");
            startLocationListener();
        } else if (newSize == 0 && prevSize == 1) {
            Log.d("No more location listeners, stop location listener");
            stopLocationListener();
        }
    }

    private void startLocationListener() {
        Log.d();
        // Location
        if (DEBUG_USE_DEVICE_GPS) {
            android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(mGpsLocationListener);
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, INTERVAL_LOC_REQUEST, 0, mGpsLocationListener);
        } else {
            // If already connected (or connecting) do nothing
            if (mLocationClient != null) {
                Log.d("Already connected: do nothing");
                return;
            }
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

    private PendingIntent getActivityRecognitionPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private OnConnectionFailedListener mLocationOnConnectionFailedListener = new OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.w("Could not connect to LocationClient, errorCode=" + connectionResult.getErrorCode());
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

            mLastFixDate = System.currentTimeMillis();

            int latE6 = (int) (location.getLatitude() * 1E6);
            int lonE6 = (int) (location.getLongitude() * 1E6);
            Log.d("location.hasSpeed=" + location.hasSpeed());

            // Dispatch to listeners
            for (LocationListener listener : mLocationListeners) {
                listener.onLocationChanged(location);
            }

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

    public void addStatusListener(StatusListener listener) {
        mStatusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        mStatusListeners.remove(listener);
    }

    protected void setActive(boolean active) {
        if (mActive != active) {
            // Dispatch the change
            for (StatusListener statusListener : mStatusListeners) {
                statusListener.onStatusChanged(active);
            }
        }
        mActive = active;
    }

    public void addActivityRecognitionListener(ActivityRecognitionListener listener) {
        int prevSize = mActivityRecognitionListeners.size();
        mActivityRecognitionListeners.add(listener);
        activityRecognitionListenersCountChanged(prevSize, mActivityRecognitionListeners.size());
    }

    public void removeActivityRecognitionListener(ActivityRecognitionListener listener) {
        int prevSize = mActivityRecognitionListeners.size();
        mActivityRecognitionListeners.remove(listener);
        activityRecognitionListenersCountChanged(prevSize, mActivityRecognitionListeners.size());
    }

    private void activityRecognitionListenersCountChanged(int prevSize, int newSize) {
        if (newSize == 1 && prevSize == 0) {
            Log.d("First activity listener, start activity listener");
            startActivityRecognitionListener();
        } else if (newSize == 0 && prevSize == 1) {
            Log.d("No more activity listeners, stop activity listener");
            stopActivityRecognitionListener();
        }
    }

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

    /* package */void onActivityRecognized(int activityType, int confidence) {
        if (mCurrentActivityType != activityType && mCurrentActivityConfidence != confidence) {
            // Dispatch the change
            for (ActivityRecognitionListener activityRecognitionListener : mActivityRecognitionListeners) {
                activityRecognitionListener.onActivityRecognized(activityType, confidence);
            }
        }
    }
}
