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
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.collection.CollectionUtil;

public class RideManager {
    private static final RideManager INSTANCE = new RideManager();

    public static RideManager get() {
        return INSTANCE;
    }

    private final Context mContext;

    private RideManager() {
        mContext = Application.getApplication();
    }

    @Background
    public Uri create(String name) {
        ContentValues values = new ContentValues(3);
        values.put(RideColumns.CREATED_DATE, System.currentTimeMillis());
        if (!name.isEmpty()) {
            values.put(RideColumns.NAME, name);
        }
        values.put(RideColumns.STATE, RideState.CREATED.getValue());
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
        // Get current state
        String[] projection = { RideColumns.STATE };
        Cursor c = mContext.getContentResolver().query(rideUri, projection, null, null, null);
        if (c == null || !c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not foundd");
        }
        RideState previousState = RideState.from(c.getInt(0));
        c.close();

        // Update state (and activated date if first time)
        ContentValues values = new ContentValues(2);
        values.put(RideColumns.STATE, RideState.ACTIVE.getValue());
        if (previousState == RideState.CREATED) {
            values.put(RideColumns.ACTIVATED_DATE, System.currentTimeMillis());
        }
        mContext.getContentResolver().update(rideUri, values, null, null);
    }

    @Background
    public void pause(Uri rideUri) {
        ContentValues values = new ContentValues(1);
        values.put(RideColumns.STATE, RideState.PAUSED.getValue());
        mContext.getContentResolver().update(rideUri, values, null, null);
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
}
