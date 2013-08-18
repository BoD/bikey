package org.jraf.android.bikey.backend.log;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.bikey.backend.location.DistanceDuration;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.provider.LogColumns;
import org.jraf.android.util.Listeners;
import org.jraf.android.util.Listeners.Dispatcher;
import org.jraf.android.util.Log;
import org.jraf.android.util.annotation.Background;

public class LogManager {
    private static final LogManager INSTANCE = new LogManager();

    public static LogManager get() {
        return INSTANCE;
    }

    private final Context mContext;
    private Listeners<LogListener> mListeners = Listeners.newInstance();

    private LogManager() {
        mContext = Application.getApplication();
    }

    @Background
    public Uri add(final Uri rideUri, Location location, Location previousLocation) {
        ContentValues values = new ContentValues(7);
        long rideId = ContentUris.parseId(rideUri);
        values.put(LogColumns.RIDE_ID, rideId);
        values.put(LogColumns.RECORDED_DATE, location.getTime());
        values.put(LogColumns.LAT, location.getLatitude());
        values.put(LogColumns.LON, location.getLongitude());
        values.put(LogColumns.ELE, location.getAltitude());
        if (previousLocation != null) {
            DistanceDuration distanceDuration = new DistanceDuration(previousLocation, location);
            float speed = distanceDuration.getSpeed();
            if (speed < LocationManager.SPEED_MIN_THRESHOLD_M_S) {
                Log.d("Speed under threshold, not logging it");
            } else {
                values.put(LogColumns.DURATION, distanceDuration.duration);
                values.put(LogColumns.DISTANCE, distanceDuration.distance);
                values.put(LogColumns.SPEED, speed);
            }
        }
        Uri res = mContext.getContentResolver().insert(LogColumns.CONTENT_URI, values);

        // Dispatch to listeners
        mListeners.dispatch(new Dispatcher<LogListener>() {
            @Override
            public void dispatch(LogListener listener) {
                listener.onLogAdded(rideUri);
            }
        });
        return res;
    }

    @Background
    public float getTotalDistance(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "sum(" + LogColumns.DISTANCE + ")" };
        String selection = LogColumns.RIDE_ID + "=?";
        String[] selectionArgs = { String.valueOf(rideId) };
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        try {
            if (!c.moveToNext()) {
                return 0;
            }
            return c.getFloat(0);
        } finally {
            c.close();
        }
    }

    public float getAverageMovingSpeed(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "sum(" + LogColumns.DISTANCE + ")/sum(" + LogColumns.DURATION + ")*1000" };
        String selection = LogColumns.RIDE_ID + "=? and " + LogColumns.SPEED + ">?";
        String[] selectionArgs = { String.valueOf(rideId), String.valueOf(LocationManager.SPEED_MIN_THRESHOLD_M_S) };
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        try {
            if (!c.moveToNext()) {
                return 0;
            }
            return c.getFloat(0);
        } finally {
            c.close();
        }
    }


    /*
     * Listeners.
     */

    public void addListener(LogListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(LogListener listener) {
        mListeners.remove(listener);
    }

}
