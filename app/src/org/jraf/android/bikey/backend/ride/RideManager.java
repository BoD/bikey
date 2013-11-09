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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.backend.ride;

import java.util.Date;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.bikey.backend.provider.LogColumns;
import org.jraf.android.bikey.backend.provider.RideColumns;
import org.jraf.android.bikey.backend.provider.RideCursorWrapper;
import org.jraf.android.bikey.backend.provider.RideState;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;

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
        if (!TextUtils.isEmpty(name)) {
            values.put(RideColumns.NAME, name);
        }
        values.put(RideColumns.STATE, RideState.CREATED.getValue());
        values.put(RideColumns.DURATION, 0);
        values.put(RideColumns.DISTANCE, 0);
        return mContext.getContentResolver().insert(RideColumns.CONTENT_URI, values);
    }

    @Background
    public int delete(long[] ids) {
        List<Long> idList = CollectionUtil.asList(ids);
        // Delete rides
        String where = RideColumns._ID + " in (" + TextUtils.join(",", idList) + ")";
        int res = mContext.getContentResolver().delete(RideColumns.CONTENT_URI, where, null);

        // Delete logs
        where = LogColumns.RIDE_ID + " in (" + TextUtils.join(",", idList) + ")";
        mContext.getContentResolver().delete(LogColumns.CONTENT_URI, where, null);
        return res;
    }

    @Background
    public void activate(final Uri rideUri) {
        // Update state 
        ContentValues values = new ContentValues(3);
        values.put(RideColumns.STATE, RideState.ACTIVE.getValue());
        // Update activated date
        values.put(RideColumns.ACTIVATED_DATE, System.currentTimeMillis());
        mContext.getContentResolver().update(rideUri, values, null, null);

        // Dispatch to listeners
        mListeners.dispatch(new Dispatcher<RideListener>() {
            @Override
            public void dispatch(RideListener listener) {
                listener.onActivated(rideUri);
            }
        });
    }

    @Background
    public void updateTotalDistance(Uri rideUri, float distance) {
        ContentValues values = new ContentValues(1);
        values.put(RideColumns.DISTANCE, distance);
        mContext.getContentResolver().update(rideUri, values, null, null);
    }

    @Background
    public void updateName(Uri rideUri, String name) {
        ContentValues values = new ContentValues(1);
        if (TextUtils.isEmpty(name)) {
            values.put(RideColumns.NAME, (String) null);
        } else {
            values.put(RideColumns.NAME, name);
        }
        mContext.getContentResolver().update(rideUri, values, null, null);
    }

    @Background
    public void pause(final Uri rideUri) {
        // Get current activated date / duration
        String[] projection = { RideColumns.ACTIVATED_DATE, RideColumns.DURATION };
        RideCursorWrapper c = new RideCursorWrapper(mContext.getContentResolver().query(rideUri, projection, null, null, null));
        try {
            if (!c.moveToNext()) {
                throw new IllegalArgumentException(rideUri + " not found");
            }
            long activatedDate = c.getActivatedDate().getTime();
            long duration = c.getDuration();

            // Update duration, state, and reset activated date
            duration += System.currentTimeMillis() - activatedDate;

            ContentValues values = new ContentValues(3);
            values.put(RideColumns.STATE, RideState.PAUSED.getValue());
            values.put(RideColumns.DURATION, duration);
            values.put(RideColumns.ACTIVATED_DATE, 0);
            mContext.getContentResolver().update(rideUri, values, null, null);

            // Dispatch to listeners
            mListeners.dispatch(new Dispatcher<RideListener>() {
                @Override
                public void dispatch(RideListener listener) {
                    listener.onPaused(rideUri);
                }
            });
        } finally {
            c.close();
        }
    }

    /**
     * Queries all the columns for the given ride.
     * Do not forget to call {@link Cursor#close()} on the returned Cursor.
     */
    private RideCursorWrapper query(Uri rideUri) {
        Cursor c = mContext.getContentResolver().query(rideUri, null, null, null, null);
        if (!c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not found");
        }
        return new RideCursorWrapper(c);
    }

    @Background
    public Uri getActiveRide() {
        String[] projection = { RideColumns._ID };
        String selection = RideColumns.STATE + "=?";
        String[] selectionArgs = { RideState.ACTIVE.getValueAsString() };
        Cursor c = mContext.getContentResolver().query(RideColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        try {
            if (!c.moveToNext()) return null;
            long id = c.getLong(0);
            return ContentUris.withAppendedId(RideColumns.CONTENT_URI, id);
        } finally {
            c.close();
        }
    }

    @Background
    public Date getActivatedDate(Uri rideUri) {
        RideCursorWrapper c = query(rideUri);
        try {
            return c.getActivatedDate();
        } finally {
            c.close();
        }
    }

    @Background
    public long getDuration(Uri rideUri) {
        RideCursorWrapper c = query(rideUri);
        try {
            return c.getDuration();
        } finally {
            c.close();
        }
    }

    @Background
    public RideState getState(Uri rideUri) {
        RideCursorWrapper c = query(rideUri);
        try {
            return RideState.from(c.getState());
        } finally {
            c.close();
        }
    }

    @Background
    public String getDisplayName(Uri rideUri) {
        RideCursorWrapper c = query(rideUri);
        try {
            String name = c.getName();
            long createdDateLong = c.getCreatedDate().getTime();
            String createdDateTimeStr = DateUtils.formatDateTime(mContext, createdDateLong, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
            if (name == null) {
                return createdDateTimeStr;
            }
            return name + " (" + createdDateTimeStr + ")";
        } finally {
            c.close();
        }
    }

    @Background
    public String getName(Uri rideUri) {
        RideCursorWrapper c = query(rideUri);
        try {
            return c.getName();
        } finally {
            c.close();
        }
    }



    /*
     * Listeners.
     */

    public void addListener(RideListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(RideListener listener) {
        mListeners.remove(listener);
    }
}
