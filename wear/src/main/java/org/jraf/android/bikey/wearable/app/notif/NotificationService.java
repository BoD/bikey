package org.jraf.android.bikey.wearable.app.notif;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.jraf.android.bikey.common.Path;
import org.jraf.android.bikey.R;
import org.jraf.android.util.log.wrapper.Log;

public class NotificationService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 0;

    public NotificationService() {}

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(messageEvent.toString());
        String path = messageEvent.getPath();
        if (Path.Notif.SHOW.matches(path)) {
            notificationShow();
        } else if (Path.Notif.HIDE.matches(path)) {
            notificationHide();
        }
    }

    private void notificationShow() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void notificationHide() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private Notification createNotification() {
        Notification.Builder mainNotifBuilder = new Notification.Builder(this);
        mainNotifBuilder.setOngoing(true);
        mainNotifBuilder.setSmallIcon(R.drawable.ic_launcher);
        mainNotifBuilder.setTicker(getString(R.string.app_name));
        mainNotifBuilder.setContentTitle(getString(R.string.app_name));
        mainNotifBuilder.setContentText(getString(R.string.app_name));

        //        mainNotifBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        //        Intent intent = new Intent(this, DisplayActivity.class).setData(mCollectingRideUri);
        //        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));


        Intent intent = new Intent();
        mainNotifBuilder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        //TODO
        //        builder.addAction(R.drawable.ic_action_stop, getString(R.string.service_notification_action_stop),
        //                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DISABLE), PendingIntent.FLAG_CANCEL_CURRENT));
        //        builder.addAction(R.drawable.ic_action_logs, getString(R.string.service_notification_action_logs),
        //                PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));


        // Android Wear

        // Speed page
        NotificationCompat.Builder speedNotifBuilder = new NotificationCompat.Builder(this);
        speedNotifBuilder.setContentText(new Date().toString());

        Notification.Builder pageNotificationBuilder = new Notification.WearableExtender().addPage(speedNotifBuilder.build()).extend(mainNotifBuilder);

        Notification res = pageNotificationBuilder.build();


        return res;
    }
}
