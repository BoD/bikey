package org.jraf.android.bikey.wearable.app.notif;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.log.wrapper.Log;

public class NotificationService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 0;

    public NotificationService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("dataEvents.count=" + dataEvents.getCount());
        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();
            String path = dataItem.getUri().getPath();
            Log.d("path=" + path);
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap dataMap = dataMapItem.getDataMap();
            if (path.endsWith(CommConstants.PATH_PREFERENCES)) {
                // Unit preferences
                String units = dataMap.getString(CommConstants.EXTRA_UNITS);
                UnitUtil.setUnits(units);
            } else if (path.endsWith(CommConstants.PATH_RIDE_VALUES)) {
                // Current ride data
                long duration = dataMap.getLong(CommConstants.EXTRA_DURATION);
                float speed = dataMap.getFloat(CommConstants.EXTRA_SPEED);
                float distance = dataMap.getFloat(CommConstants.EXTRA_DISTANCE);
                int heartRate = dataMap.getInt(CommConstants.EXTRA_HEART_RATE);
                notificationShow(duration, speed, distance, heartRate);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(messageEvent.toString());
    }

    private void notificationShow(long duration, float speed, float distance, int heartRate) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotificationOngoingRide(duration, speed, distance, heartRate);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void notificationHide() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private Notification createNotificationOngoingRide(long duration, float speed, float distance, int heartRate) {
        Notification.Builder mainNotifBuilder = new Notification.Builder(this);
        mainNotifBuilder.setOngoing(true);
        mainNotifBuilder.setSmallIcon(R.drawable.ic_launcher);
//        mainNotifBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        CharSequence durationStr = DateTimeUtil.formatDuration(this, duration);
        CharSequence speedStr = UnitUtil.formatSpeed(speed, true, .85f);
        CharSequence distanceStr = UnitUtil.formatDistance(distance, true, .85f);
        CharSequence heartRateStr = UnitUtil.formatHeartRate(heartRate, true);

//        mainNotifBuilder.setContentTitle(durationStr+"\n"+speedStr+"\n"+distanceStr+"\n"+heartRateStr);
        CharSequence text = TextUtils.concat(durationStr, "\n", distanceStr, "\n", speedStr);
        mainNotifBuilder.setContentText(text);

//        mainNotifBuilder.setContent(new RemoteViews(getPackageName(), R.layout.test));

        //        mainNotifBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        //        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        //        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));


        Intent intent = new Intent();
        mainNotifBuilder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        //TODO
//        mainNotifBuilder.addAction(R.drawable.ic_launcher, getString(R.string.app_name), PendingIntent.getBroadcast(this, 0, new Intent("test"),
//                PendingIntent.FLAG_CANCEL_CURRENT));
        //        builder.addAction(R.drawable.ic_action_logs, getString(R.string.service_notification_action_logs),
        //                PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));


        // Android Wear
        Notification.WearableExtender wearableExtender = new Notification.WearableExtender();
//        wearableExtender.setHintHideIcon(true);
        wearableExtender.setContentIcon(R.drawable.ic_action_pause);
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
