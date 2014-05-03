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
package org.jraf.android.bikey.app.collect;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.bikey.backend.cadence.CadenceListener;
import org.jraf.android.bikey.backend.cadence.CadenceManager;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class LogCollectorService extends Service {
    private static final String PREFIX = LogCollectorService.class.getName() + ".";
    public static final String ACTION_START_COLLECTING = PREFIX + "ACTION_START_COLLECTING";
    public static final String ACTION_STOP_COLLECTING = PREFIX + "ACTION_STOP_COLLECTING";

    private static final int NOTIFICATION_ID = 1;
    private Uri mCollectingRideUri;
    protected Location mLastLocation;
    private Float mLastCadence;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("intent=" + StringUtil.toString(intent));
        if (intent == null) return Service.START_STICKY;
        final String action = intent.getAction();
        if (ACTION_START_COLLECTING.equals(action)) {
            startCollecting(intent.getData());
        } else if (ACTION_STOP_COLLECTING.equals(action)) {
            stopCollecting(intent.getData());
        }
        return Service.START_STICKY;
    }

    private void startCollecting(final Uri rideUri) {
        runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                // First, pause current ride if any
                if (mCollectingRideUri != null) {
                    RideManager.get().pause(mCollectingRideUri);
                }

                // Check if the ride still exists (it may have been deleted)
                boolean rideExists = RideManager.get().isExistingRide(rideUri);
                Log.d("rideExists=" + rideExists);
                if (!rideExists) {
                    stopSelf();
                    return;
                }

                // Save the ride as the current one
                RideManager.get().setCurrentRide(rideUri);

                // Now collect for the new current ride
                mCollectingRideUri = rideUri;
                RideManager.get().activate(mCollectingRideUri);
                Notification notification = createNotification();
                startForeground(NOTIFICATION_ID, notification);
                LocationManager.get().addLocationListener(mLocationListener);

                // Start monitoring cadence (if enabled in the prefs)
                if (PreferenceManager.getDefaultSharedPreferences(LogCollectorService.this).getBoolean(Constants.PREF_RECORD_CADENCE,
                        Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                    CadenceManager.get().addListener(mCadenceListener);
                }

                // Start listening to pref changes (to enable / disable cadence recording accordingly)
                PreferenceManager.getDefaultSharedPreferences(LogCollectorService.this).registerOnSharedPreferenceChangeListener(
                        mOnSharedPreferenceChangeListener);
            }
        });
    }

    private void stopCollecting(final Uri rideUri) {
        runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                RideManager.get().pause(rideUri);
            }
        });

        dismissNotification();
        LocationManager.get().removeLocationListener(mLocationListener);
        CadenceManager.get().removeListener(mCadenceListener);

        mCollectingRideUri = null;
        stopSelf();
    }

    private void runOnBackgroundThread(final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }
        }.execute();
    }


    /*
     * Location listener.
     */

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            runOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    Float cadence = mLastCadence;
                    LogManager.get().add(mCollectingRideUri, location, mLastLocation, cadence);
                    mLastLocation = location;
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
     * Cadence listener.
     */

    private CadenceListener mCadenceListener = new CadenceListener() {
        @Override
        public void onCadenceChanged(Float cadence, float[][] rawData) {
            mLastCadence = cadence;
        }
    };


    /*
     * Pref listener.
     */

    protected OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PreferenceManager.getDefaultSharedPreferences(LogCollectorService.this).getBoolean(Constants.PREF_RECORD_CADENCE,
                    Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                // Start monitoring cadence
                CadenceManager.get().addListener(mCadenceListener);
            } else {
                // Stop monitoring cadence
                CadenceManager.get().removeListener(mCadenceListener);
            }
        }
    };


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

        //        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        //        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(DisplayActivity.class);
        Log.d("mCollectingRideUri=" + mCollectingRideUri);
        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        taskStackBuilder.addNextIntent(intent);
        builder.setContentIntent(taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

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


    @Override
    public void onDestroy() {
        // Unregister pref listener
        PreferenceManager.getDefaultSharedPreferences(LogCollectorService.this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        super.onDestroy();
    }
}
