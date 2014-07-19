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
package org.jraf.android.bikey.wearable.app.notif;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.common.wear.CommConstants;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.log.wrapper.Log;

public class NotificationService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 0;
    private static final long ONGOING_NOTIFICATION_UPDATE_FREQUENCY_LIMIT = 1500; // ms
    private float mRideDistance;
    private float mRideSpeed;
    private long mRideStartDateOffset;
    private long mLastOngoingNotificationUpdate;
    private int mHeartRate;

    public NotificationService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(messageEvent.toString());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("count=" + dataEvents.getCount());

        // First read unit preferences if necessary
        if (UnitUtil.getUnits() == null) {
            UnitUtil.setUnits(WearCommHelper.get().retrievePreferences(CommConstants.EXTRA_UNITS));
        }

        boolean updateOngoingNotification = false;
        boolean showPausedRideNotification = false;
        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();
            Uri uri = dataItem.getUri();
            Log.d("uri=" + uri);
            String path = uri.getPath();
            Log.d("path=" + path);
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap dataMap = dataMapItem.getDataMap();

            switch (path) {
                case CommConstants.PATH_PREFERENCES:
                    // Preferences
                    String units = dataMap.getString(CommConstants.EXTRA_UNITS);
                    UnitUtil.setUnits(units);
                    break;

                case CommConstants.PATH_RIDE_ONGOING:
                    boolean ongoing = dataMap.getBoolean(CommConstants.EXTRA_VALUE);
                    if (!ongoing) {
                        // The ride is no longer ongoing: show a paused notification
                        showPausedRideNotification = true;
                    }
                    break;

                case CommConstants.PATH_RIDE_VALUES:
                    // Values update
                    mRideDistance = dataMap.getFloat(CommConstants.EXTRA_DISTANCE);
                    mRideSpeed = dataMap.getFloat(CommConstants.EXTRA_SPEED);
                    mRideStartDateOffset = dataMap.getLong(CommConstants.EXTRA_START_DATE_OFFSET);
                    mHeartRate = dataMap.getInt(CommConstants.EXTRA_HEART_RATE);
                    updateOngoingNotification = true;
                    break;
            }
        }

        if (showPausedRideNotification) {
            showPausedRideNotification();
        } else if (updateOngoingNotification) {
            updateOngoingNotificationIfNecessary();
        }
    }

    private void updateOngoingNotificationIfNecessary() {
        Log.d();
        if (System.currentTimeMillis() - mLastOngoingNotificationUpdate < ONGOING_NOTIFICATION_UPDATE_FREQUENCY_LIMIT) return;
        mLastOngoingNotificationUpdate = System.currentTimeMillis();
        showOngoingNotification();
    }


    private void showOngoingNotification() {
        Log.d();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification(true);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void showPausedRideNotification() {
        Log.d();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification(false);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        Log.d();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private Notification createNotification(boolean ongoing) {
        Notification.Builder mainNotifBuilder = new Notification.Builder(this);
        mainNotifBuilder.setOngoing(ongoing);
        mainNotifBuilder.setSmallIcon(R.drawable.ic_launcher);
//        mainNotifBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

        long duration = System.currentTimeMillis() + mRideStartDateOffset;

        CharSequence durationStr = DateTimeUtil.formatDurationShort(this, duration);
        CharSequence speedStr = UnitUtil.formatSpeed(mRideSpeed, true, .85f, true);
        CharSequence distanceStr = UnitUtil.formatDistance(mRideDistance, true, .85f, true);
        CharSequence heartRateStr = UnitUtil.formatHeartRate(mHeartRate, true);

        if (ongoing) {
            CharSequence text = TextUtils.concat(distanceStr, "\n", speedStr, "\n", durationStr);
            mainNotifBuilder.setContentText(text);
        } else {
            mainNotifBuilder.setContentTitle(getString(R.string.notification_title_paused));

            CharSequence text = TextUtils.concat(distanceStr, "\n", durationStr);
            mainNotifBuilder.setContentText(text);
        }

//        mainNotifBuilder.setContent(new RemoteViews(getPackageName(), R.layout.test));

        //        mainNotifBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        //        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        //        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));


        //TODO
//        mainNotifBuilder.addAction(R.drawable.ic_launcher, getString(R.string.app_name), PendingIntent.getBroadcast(this, 0, new Intent("test"),
//                PendingIntent.FLAG_CANCEL_CURRENT));
        //        builder.addAction(R.drawable.ic_action_logs, getString(R.string.service_notification_action_logs),
        //                PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));


        // Wear specifics
        Notification.WearableExtender wearableExtender = new Notification.WearableExtender();
//        wearableExtender.setHintHideIcon(true);
        if (ongoing) {
            wearableExtender.setContentIcon(R.drawable.ic_action_pause);
        } else {
            wearableExtender.setContentIcon(R.drawable.ic_action_play);
        }
        wearableExtender.setContentIconGravity(Gravity.START);
//        wearableExtender.setCustomSizePreset(Notification.WearableExtender.SIZE_LARGE);

        // Speed page
        NotificationCompat.Builder speedPageNotifBuilder = new NotificationCompat.Builder(this);
        speedPageNotifBuilder.setContentText(new Date().toString());
        wearableExtender.addPage(speedPageNotifBuilder.build());

        Notification.Builder wearableNotifBuilder = wearableExtender.extend(mainNotifBuilder);
        Notification res = wearableNotifBuilder.build();
        return res;
    }
}
