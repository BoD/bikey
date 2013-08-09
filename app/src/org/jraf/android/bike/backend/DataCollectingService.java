package org.jraf.android.bike.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.HudActivity;
import org.jraf.android.util.Log;
import org.jraf.android.util.string.StringUtil;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class DataCollectingService extends Service {
    private static final String PREFIX = DataCollectingService.class.getName() + ".";
    public static final String ACTION_START_COLLECTING = PREFIX + "ACTION_START_COLLECTING";
    public static final String ACTION_STOP_COLLECTING = PREFIX + "ACTION_STOP_COLLECTING";
    public static final String EXTRA_SESSION_ID = PREFIX + "EXTRA_SESSION_ID";

    private static final int NOTIFICATION_ID = 1;

    private static final int INTERVAL_LOC_REQUEST = 1000;

    private LocationClient mLocationClient;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("intent=" + StringUtil.toString(intent));
        String action = intent.getAction();
        long sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1);
        if (ACTION_START_COLLECTING.equals(action)) {
            startCollecting(sessionId);
        } else if (ACTION_STOP_COLLECTING.equals(action)) {
            stopCollecting(sessionId);
        }
        return Service.START_STICKY;
    }

    private void startCollecting(long sessionId) {
        Log.d("sessionId=" + sessionId);
        startLocationListener();
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopCollecting(long sessionId) {
        Log.d("sessionId=" + sessionId);
        stopLocationListener();
        dismissNotification();
        stopSelf();
    }


    /*
     * Notification.
     */

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.ic_stat_collecting);
        builder.setTicker(getString(R.string.service_notification_ticker));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.service_notification_text));
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HudActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        //TODO
        //        builder.addAction(R.drawable.ic_action_stop, getString(R.string.service_notification_action_stop),
        //                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DISABLE), PendingIntent.FLAG_CANCEL_CURRENT));
        //        builder.addAction(R.drawable.ic_action_logs, getString(R.string.service_notification_action_logs),
        //                PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        return notification;
    }

    private void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    /*
     * Track device location.
     */

    private void startLocationListener() {
        Log.d();
        // If already connected (or connecting) do not do anything
        if (mLocationClient != null && (mLocationClient.isConnected() || mLocationClient.isConnecting())) return;
        mLocationClient = new LocationClient(this, mLocationOnConnectionCallbacks, mLocationOnConnectionFailedListener);
        mLocationClient.connect();
    }

    private void stopLocationListener() {
        Log.d();
        if (mLocationClient == null) return;
        mLocationClient.removeLocationUpdates(mLocationListener);
        if (mLocationClient.isConnected()) mLocationClient.disconnect();
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

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("location=" + location);

            final int latE6 = (int) (location.getLatitude() * 1E6);
            final int lonE6 = (int) (location.getLongitude() * 1E6);

        }
    };
}
