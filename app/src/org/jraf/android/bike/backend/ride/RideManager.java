package org.jraf.android.bike.backend.ride;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.jraf.android.bike.app.Application;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.provider.RideState;
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

    public Uri create(String name) {
        ContentValues values = new ContentValues(3);
        values.put(RideColumns.CREATED_DATE, System.currentTimeMillis());
        if (!name.isEmpty()) {
            values.put(RideColumns.NAME, name);
        }
        values.put(RideColumns.STATE, RideState.CREATED.getValue());
        return mContext.getContentResolver().insert(RideColumns.CONTENT_URI, values);
    }

    public int delete(long[] ids) {
        List<Long> idList = CollectionUtil.asList(ids);
        String where = RideColumns._ID + " in (" + TextUtils.join(",", idList) + ")";
        return mContext.getContentResolver().delete(RideColumns.CONTENT_URI, where, null);
    }

    public void activate(Uri rideUri) {
        ContentValues values = new ContentValues(1);
        values.put(RideColumns.STATE, RideState.ACTIVE.getValue());
        mContext.getContentResolver().update(rideUri, values, null, null);
    }

    public void pause(Uri rideUri) {
        ContentValues values = new ContentValues(1);
        values.put(RideColumns.STATE, RideState.PAUSED.getValue());
        mContext.getContentResolver().update(rideUri, values, null, null);
    }
}
