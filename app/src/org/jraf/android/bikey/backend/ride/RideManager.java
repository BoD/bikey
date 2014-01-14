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

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.Application;
import org.jraf.android.bikey.app.logcollectservice.LogCollectorService;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogContentValues;
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.backend.provider.ride.RideContentValues;
import org.jraf.android.bikey.backend.provider.ride.RideCursorWrapper;
import org.jraf.android.bikey.backend.provider.ride.RideSelection;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;
import org.jraf.android.util.log.wrapper.Log;

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
        RideContentValues values = new RideContentValues();
        values.putCreatedDate(new Date());
        if (!TextUtils.isEmpty(name)) {
            values.putName(name);
        }
        values.putState(RideState.CREATED.getValue());
        values.putDuration(0l);
        values.putDistance(0d);
        return mContext.getContentResolver().insert(RideColumns.CONTENT_URI, values.getContentValues());
    }

    @Background
    public int delete(long[] ids) {
        // First pause any active rides in the list
        pauseRides(ids);

        // Delete rides
        RideSelection rideWhere = new RideSelection();
        rideWhere.id(ids);
        int res = mContext.getContentResolver().delete(RideColumns.CONTENT_URI, rideWhere.sel(), rideWhere.args());

        // Delete logs
        LogSelection logWhere = new LogSelection();
        logWhere.rideId(ids);
        mContext.getContentResolver().delete(LogColumns.CONTENT_URI, logWhere.sel(), logWhere.args());

        // If we just deleted the current ride, select another ride to be the current ride (if any).
        Uri currentRideUri = getCurrentRide();
        long currentRideId = Long.valueOf(currentRideUri.getLastPathSegment());
        for (long id : ids) {
            if (currentRideId == id) {
                Uri nextRideUri = getNextRide();
                setCurrentRide(nextRideUri);
            }
        }
        return res;
    }

    @Background
    public void merge(long[] ids) {
        // First pause any active rides in the list
        pauseRides(ids);

        // Choose the master ride (the one with the earliest creation date)
        String[] projection = { RideColumns._ID };
        RideSelection rideWhere = new RideSelection();
        rideWhere.id(ids);
        String order = RideColumns.CREATED_DATE;
        Cursor c = mContext.getContentResolver().query(RideColumns.CONTENT_URI, projection, rideWhere.sel(), rideWhere.args(), order);
        long masterRideId = 0;
        try {
            c.moveToNext();
            masterRideId = c.getLong(0);
        } finally {
            c.close();
        }

        // Calculate the total duration
        projection = new String[] { "sum(" + RideColumns.DURATION + ")" };
        c = mContext.getContentResolver().query(RideColumns.CONTENT_URI, projection, rideWhere.sel(), rideWhere.args(), null);
        long totalDuration = 0;
        try {
            if (c.moveToNext()) {
                totalDuration = c.getLong(0);
            }
        } finally {
            c.close();
        }

        // Merge
        for (long mergedRideId : ids) {
            if (mergedRideId == masterRideId) continue;

            // Update logs
            LogSelection logWhere = new LogSelection();
            logWhere.rideId(mergedRideId);
            LogContentValues values = new LogContentValues();
            values.putRideId(masterRideId);
            mContext.getContentResolver().update(LogColumns.CONTENT_URI, values.getContentValues(), logWhere.sel(), logWhere.args());

            // Delete merged ride
            rideWhere = new RideSelection();
            rideWhere.id(mergedRideId);
            Uri contentUri = BikeyProvider.notify(RideColumns.CONTENT_URI, false);
            mContext.getContentResolver().delete(contentUri, rideWhere.sel(), rideWhere.args());
        }

        // Rename master ride
        Uri masterRideUri = ContentUris.withAppendedId(RideColumns.CONTENT_URI, masterRideId);
        String name = getName(masterRideUri);
        if (name == null) {
            name = mContext.getString(R.string.ride_list_mergedRide);
        } else {
            name = mContext.getString(R.string.ride_list_mergedRide_append, name);
        }
        RideContentValues values = new RideContentValues();
        values.putName(name);
        mContext.getContentResolver().update(masterRideUri, values.getContentValues(), null, null);

        // Update master ride total distance
        double distance = LogManager.get().getTotalDistance(masterRideUri);
        updateTotalDistance(masterRideUri, distance);

        // Update master ride total duration
        updateDuration(masterRideUri, totalDuration);
    }

    private void pauseRides(long[] ids) {
        for (long rideId : ids) {
            Uri rideUri = ContentUris.withAppendedId(RideColumns.CONTENT_URI, rideId);
            RideState state = getState(rideUri);
            if (state == RideState.ACTIVE) {
                mContext.startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, rideUri, mContext, LogCollectorService.class));
                break;
            }
        }
    }

    @Background
    public void activate(final Uri rideUri) {
        // Update state 
        RideContentValues values = new RideContentValues();
        values.putState(RideState.ACTIVE.getValue());
        // Update activated date
        values.putActivatedDate(new Date());
        mContext.getContentResolver().update(rideUri, values.getContentValues(), null, null);

        // Dispatch to listeners
        mListeners.dispatch(new Dispatcher<RideListener>() {
            @Override
            public void dispatch(RideListener listener) {
                listener.onActivated(rideUri);
            }
        });
    }

    @Background
    public void updateTotalDistance(Uri rideUri, double distance) {
        RideContentValues values = new RideContentValues();
        values.putDistance(distance);
        mContext.getContentResolver().update(rideUri, values.getContentValues(), null, null);
    }

    @Background
    private void updateDuration(Uri rideUri, long duration) {
        RideContentValues values = new RideContentValues();
        values.putDuration(duration);
        mContext.getContentResolver().update(rideUri, values.getContentValues(), null, null);
    }

    @Background
    public void updateName(Uri rideUri, String name) {
        RideContentValues values = new RideContentValues();
        if (TextUtils.isEmpty(name)) {
            values.putNameNull();
        } else {
            values.putName(name);
        }
        mContext.getContentResolver().update(rideUri, values.getContentValues(), null, null);
    }

    @Background
    public void pause(final Uri rideUri) {
        // Get current activated date / duration
        String[] projection = { RideColumns.ACTIVATED_DATE, RideColumns.DURATION };
        RideCursorWrapper c = new RideCursorWrapper(mContext.getContentResolver().query(rideUri, projection, null, null, null));
        try {
            if (!c.moveToNext()) {
                Log.w("Could not pause ride, uri " + rideUri + " not found");
                return;
            }
            long activatedDate = c.getActivatedDate().getTime();
            long duration = c.getDuration();

            // Update duration, state, and reset activated date
            duration += System.currentTimeMillis() - activatedDate;

            RideContentValues values = new RideContentValues();
            values.putState(RideState.PAUSED.getValue());
            values.putDuration(duration);
            values.putActivatedDate(0l);
            mContext.getContentResolver().update(rideUri, values.getContentValues(), null, null);

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
        if (rideUri == null) throw new IllegalArgumentException("null rideUri");
        Cursor c = mContext.getContentResolver().query(rideUri, null, null, null, null);
        if (!c.moveToNext()) {
            throw new IllegalArgumentException(rideUri + " not found");
        }
        return new RideCursorWrapper(c);
    }

    @Background
    public Uri getCurrentRide() {
        String currentRideUriStr = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_CURRENT_RIDE_URI, null);
        if (!TextUtils.isEmpty(currentRideUriStr)) {
            Uri currentRideUri = Uri.parse(currentRideUriStr);
            return currentRideUri;
        }
        return null;
    }

    @Background
    public void setCurrentRide(Uri rideUri) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(Constants.PREF_CURRENT_RIDE_URI, rideUri.toString()).commit();
    }

    @Background
    private Uri getNextRide() {
        String[] projection = { RideColumns._ID };
        // Return a ride, prioritizing ACTIVE ones first, then sorting by creation date.
        Cursor c = mContext.getContentResolver().query(RideColumns.CONTENT_URI, projection, null, null,
                RideColumns.STATE + ", " + RideColumns.CREATED_DATE + " DESC");
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

    @Background
    public boolean isExistingRide(Uri rideUri) {
        Cursor c = mContext.getContentResolver().query(rideUri, null, null, null, null);
        try {
            return c.moveToNext();
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
