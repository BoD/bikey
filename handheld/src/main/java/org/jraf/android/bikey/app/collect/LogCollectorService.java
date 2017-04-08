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
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.bikey.app.smartwatchsender.AndroidWearSender;
import org.jraf.android.bikey.app.smartwatchsender.PebbleSender;
import org.jraf.android.bikey.backend.cadence.CadenceListener;
import org.jraf.android.bikey.backend.cadence.CadenceManager;
import org.jraf.android.bikey.backend.heartrate.HeartRateListener;
import org.jraf.android.bikey.backend.heartrate.HeartRateManager;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.string.StringUtil;

import com.getpebble.android.kit.PebbleKit;

import io.reactivex.schedulers.Schedulers;

public class LogCollectorService extends Service {
    private static final String PREFIX = LogCollectorService.class.getName() + ".";
    public static final String ACTION_START_COLLECTING = PREFIX + "ACTION_START_COLLECTING";
    public static final String ACTION_STOP_COLLECTING = PREFIX + "ACTION_STOP_COLLECTING";

    private static final int NOTIFICATION_ID = 1;
    private Uri mCollectingRideUri;
    protected Location mLastLocation;
    private Float mLastCadence;
    private Integer mLastHeartRate;
    private SharedPreferences mPreferences;
    private AndroidWearSender mAndroidWearSender = null;
    private PebbleSender mPebbleSender = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("intent=" + StringUtil.toString(intent));
        if (intent == null) return Service.START_STICKY;
        String action = intent.getAction();
        if (ACTION_START_COLLECTING.equals(action)) {
            startCollecting(intent.getData());
        } else if (ACTION_STOP_COLLECTING.equals(action)) {
            stopCollecting(intent.getData());
        }
        return Service.START_STICKY;
    }

    private void startCollecting(final Uri rideUri) {
        final Context context = getApplicationContext();
        Schedulers.io().scheduleDirect(() -> {
            // Smartwatches support (if enabled in prefs)
            if (mPreferences.getBoolean(Constants.PREF_ANDROID_WEAR, Constants.PREF_ANDROID_WEAR_DEFAULT)) {
                mAndroidWearSender = new AndroidWearSender();
                mAndroidWearSender.startSending(context);
            }
            if (mPreferences.getBoolean(Constants.PREF_PEBBLE, Constants.PREF_PEBBLE_DEFAULT)
                    && PebbleKit.isWatchConnected(context)) {
                mPebbleSender = new PebbleSender();
                mPebbleSender.startSending(LogCollectorService.this);
            }

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

            // Show notification
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);

            // Start recording location
            LocationManager.get().addLocationListener(mLocationListener);

            // Start monitoring cadence (if enabled in the prefs)
            if (mPreferences.getBoolean(Constants.PREF_RECORD_CADENCE, Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                CadenceManager.get().addListener(mCadenceListener);
            }

            // Start listening to pref changes (to enable / disable cadence recording accordingly)
            mPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

            // Start recording heart rate
            HeartRateManager.get().addListener(mHeartRateListener);
        });
    }

    private void stopCollecting(final Uri rideUri) {
        Schedulers.io().scheduleDirect(() -> RideManager.get().pause(rideUri));

        // Dismiss notification
        dismissNotification();

        LocationManager.get().removeLocationListener(mLocationListener);
        CadenceManager.get().removeListener(mCadenceListener);
        HeartRateManager.get().removeListener(mHeartRateListener);

        mCollectingRideUri = null;
        stopSelf();
    }

    /*
     * Location listener.
     */

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Schedulers.io().scheduleDirect(() -> {
                LogManager.get().add(mCollectingRideUri, location, mLastLocation, mLastCadence, mLastHeartRate);
                mLastLocation = location;
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
            if (mPreferences.getBoolean(Constants.PREF_RECORD_CADENCE, Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                // Start monitoring cadence
                CadenceManager.get().addListener(mCadenceListener);
            } else {
                // Stop monitoring cadence
                CadenceManager.get().removeListener(mCadenceListener);
            }

            // TODO check for smartwatch preferences and start / stop sending values
        }
    };


    /*
     * Heart rate listener.
     */

    private HeartRateListener mHeartRateListener = new HeartRateListener() {
        @Override
        public void onConnecting() {}

        @Override
        public void onHeartRateChange(int bpm) {
            mLastHeartRate = bpm;
        }

        @Override
        public void onDisconnected() {
            mLastHeartRate = null;
        }

        @Override
        public void onConnected() {}

        @Override
        public void onError() {
            onDisconnected();
        }
    };


    /*
     * Notification.
     */

    private Notification createNotification() {
        NotificationCompat.Builder mainNotifBuilder = new NotificationCompat.Builder(this);
        mainNotifBuilder.setOngoing(true);
        mainNotifBuilder.setSmallIcon(R.drawable.ic_stat_collecting);
        mainNotifBuilder.setTicker(getString(R.string.service_notification_ticker));
        mainNotifBuilder.setContentTitle(getString(R.string.app_name));
        mainNotifBuilder.setContentText(getString(R.string.service_notification_text));
        mainNotifBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        //        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        //        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(DisplayActivity.class);
        Log.d("mCollectingRideUri=" + mCollectingRideUri);
        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        taskStackBuilder.addNextIntent(intent);
        mainNotifBuilder.setContentIntent(taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

        //TODO
        //        builder.addAction(R.drawable.ic_action_stop, getString(R.string.service_notification_action_stop),
        //                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DISABLE), PendingIntent.FLAG_CANCEL_CURRENT));
        //        builder.addAction(R.drawable.ic_action_logs, getString(R.string.service_notification_action_logs),
        //                PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        Notification notification = mainNotifBuilder.build();
        return notification;
    }

    private void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    @Override
    public void onDestroy() {
        // Disconnect smartwatch senders
        if (mAndroidWearSender != null) mAndroidWearSender.stopSending();
        if (mPebbleSender != null) mPebbleSender.stopSending();

        // Unregister pref listener
        PreferenceManager.getDefaultSharedPreferences(LogCollectorService.this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        super.onDestroy();
    }
}
