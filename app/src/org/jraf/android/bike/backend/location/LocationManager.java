package org.jraf.android.bike.backend.location;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import org.jraf.android.bike.app.Application;
import org.jraf.android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationManager {
    private static final LocationManager INSTANCE = new LocationManager();

    private static final boolean DEBUG_USE_DEVICE_GPS = true;
    private static final int INTERVAL_LOC_REQUEST = 1000;

    public static LocationManager get() {
        return INSTANCE;
    }

    private Context mContext;
    private Set<LocationListener> mListeners = new HashSet<LocationListener>(3);
    private LocationClient mLocationClient;

    private LocationManager() {
        mContext = Application.getApplication();
    }

    public void addLocationListener(LocationListener listener) {
        mListeners.add(listener);
        onListenersUpdated();
    }

    public void removeLocationListener(LocationListener listener) {
        mListeners.remove(listener);
        onListenersUpdated();
    }

    private void onListenersUpdated() {
        if (mListeners.size() == 0) {
            Log.d("No more interested listeners, stop location listener");
            stopLocationListener();
        } else {
            startLocationListener();
        }
    }

    private void startLocationListener() {
        Log.d();
        // If already connected (or connecting) do not do anything
        if (mLocationClient != null && (mLocationClient.isConnected() || mLocationClient.isConnecting())) return;
        if (DEBUG_USE_DEVICE_GPS) {
            android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, INTERVAL_LOC_REQUEST, 0, mGpsLocationListener);
        } else {
            mLocationClient = new LocationClient(mContext, mLocationOnConnectionCallbacks, mLocationOnConnectionFailedListener);
            mLocationClient.connect();
        }
    }

    private void stopLocationListener() {
        Log.d();
        if (mLocationClient == null) return;
        mLocationClient.removeLocationUpdates(mLocationListener);
        if (mLocationClient.isConnected()) mLocationClient.disconnect();
        mLocationClient = null;
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
            Log.d("location=" + location);
            mLocationListener.onLocationChanged(location);
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("location=" + location);

            int latE6 = (int) (location.getLatitude() * 1E6);
            int lonE6 = (int) (location.getLongitude() * 1E6);
            Log.d("location.hasSpeed=" + location.hasSpeed());

            // Dispatch to listeners
            for (LocationListener listener : mListeners) {
                listener.onLocationChanged(location);
            }
        }
    };
}
