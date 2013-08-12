package org.jraf.android.bike.backend.ride;

import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.jraf.android.bike.app.Application;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.provider.RideCursorWrapper;
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.util.Listeners;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.collection.CollectionUtil;

public class RideManager {
    private static final RideManager INSTANCE = new RideManager();

    public static RideManager get() {
        return INSTANCE;
    }

    private final Context mContext;
    private Listeners<RideListener> mListeners = Listeners.newInstance();

    private RideManager() {
        mContext = Application.getApplication();
    }

    @Background
    public Uri create(String name) {
        ContentValues values = new ContentValues(4);
        values.put(RideColumns.CREATED_DATE, System.currentTimeMillis());
        if (!name.isEmpty()) {
            values.put(RideColumns.NAME, name);
        }
        values.put(RideColumns.STATE, RideState.CREATED.getValue());
        values.put(RideColumns.DURATION, 0);
        return mContext.getContentResolver().insert(RideColumns.CONTENT_URI, values);
    }

    @Background
    public int delete(long[] ids) {
        List<Long> idList = CollectionUtil.asList(ids);
        String where = RideColumns._ID + " in (" + TextUtils.join(",", idList) + ")";
        return mContext.getContentResolver().delete(RideColumns.CONTENT_URI, where, null);
    }

    @Background
    public void activate(Uri rideUri) {
        // Update state
        ContentValues values = new ContentValues(3);
        values.put(RideColumns.STATE, RideState.ACTIVE.getValue());
        // Update activated date if first time
        values.put(RideColumns.ACTIVATED_DATE, System.currentTimeMillis());
        mContext.getContentResolver().update(rideUri, values, null, null);

        // Dispatch to listeners
        for (RideListener listener : mListeners) {
            listener.onActivated(rideUri);
        }
    }

    @Background
    public void pause(Uri rideUri) {
        // Get current activated date / duration
        String[] projection = { RideColumns.ACTIVATED_DATE, RideColumns.DURATION };
        RideCursorWrapper c = new RideCursorWrapper(mContext.getContentResolver().query(rideUri, projection, null, null, null));
        if (!c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not found");
        }
        long activatedDate = c.getActivatedDate();
        long duration = c.getDuration();
        c.close();

        // Update duration and state
        duration += System.currentTimeMillis() - activatedDate;

        ContentValues values = new ContentValues(2);
        values.put(RideColumns.STATE, RideState.PAUSED.getValue());
        values.put(RideColumns.DURATION, duration);
        mContext.getContentResolver().update(rideUri, values, null, null);

        // Dispatch to listeners
        for (RideListener listener : mListeners) {
            listener.onPaused(rideUri);
        }
    }

    @Background
    public Uri getActiveRide() {
        String[] projection = { RideColumns._ID };
        String selection = RideColumns.STATE + "=?";
        String[] selectionArgs = { RideState.ACTIVE.getValueAsString() };
        Cursor c = mContext.getContentResolver().query(RideColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        if (c == null || !c.moveToNext()) return null;
        long id = c.getLong(0);
        c.close();
        return ContentUris.withAppendedId(RideColumns.CONTENT_URI, id);
    }

    @Background
    public long getActivatedDate(Uri rideUri) {
        String[] projection = { RideColumns.ACTIVATED_DATE };
        Cursor c = mContext.getContentResolver().query(rideUri, projection, null, null, null);
        if (c == null || !c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not found");
        }
        long res = c.getLong(0);
        c.close();
        return res;
    }

    @Background
    public long getDuration(Uri rideUri) {
        String[] projection = { RideColumns.DURATION };
        Cursor c = mContext.getContentResolver().query(rideUri, projection, null, null, null);
        if (c == null || !c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not found");
        }
        long res = c.getLong(0);
        c.close();
        return res;
    }

    public void addListener(RideListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(RideListener listener) {
        mListeners.remove(listener);
    }
}
