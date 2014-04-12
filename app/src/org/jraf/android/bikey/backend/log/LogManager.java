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
package org.jraf.android.bikey.backend.log;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.location.LocationPair;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogContentValues;
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;
import org.jraf.android.util.log.wrapper.Log;

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
    public Uri add(final Uri rideUri, Location location, Location previousLocation, Float cadence) {
        // Add a log
        LogContentValues values = new LogContentValues();
        long rideId = ContentUris.parseId(rideUri);
        values.putRideId(rideId);
        values.putRecordedDate(location.getTime());
        values.putLat(location.getLatitude());
        values.putLon(location.getLongitude());
        values.putEle(location.getAltitude());
        if (previousLocation != null) {
            LocationPair locationPair = new LocationPair(previousLocation, location);
            float speed = locationPair.getSpeed();
            if (speed < LocationManager.SPEED_MIN_THRESHOLD_M_S) {
                Log.d("Speed under threshold, not logging it");
            } else {
                values.putDuration(locationPair.getDuration());
                values.putDistance(locationPair.getDistance());
                values.putSpeed(speed);
            }
        }
        values.putCadence(cadence);

        Uri res = mContext.getContentResolver().insert(LogColumns.CONTENT_URI, values.values());

        // Update total distance for ride
        double totalDistance = getTotalDistance(rideUri);
        RideManager.get().updateTotalDistance(rideUri, totalDistance);

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
    public double getTotalDistance(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "sum(" + LogColumns.DISTANCE + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return 0;
            }
            return c.getDouble(0);
        } finally {
            c.close();
        }
    }

    public double getAverageMovingSpeed(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "sum(" + LogColumns.DISTANCE + ")/sum(" + LogColumns.DURATION + ")*1000" };
        LogSelection where = new LogSelection();
        where.rideId(rideId).and().speedGt(LocationManager.SPEED_MIN_THRESHOLD_M_S);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return 0;
            }
            return c.getDouble(0);
        } finally {
            c.close();
        }
    }

    public Double getTotalMovingDuration(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "sum(" + LogColumns.DURATION + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId).and().speedGt(LocationManager.SPEED_MIN_THRESHOLD_M_S);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return null;
            }
            if (c.isNull(0)) return null;
            return c.getDouble(0);
        } finally {
            c.close();
        }
    }

    public double getMaxSpeed(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "max(" + LogColumns.SPEED + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return 0;
            }
            return c.getDouble(0);
        } finally {
            c.close();
        }
    }

    public Long getFirstLogDate(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "min(" + LogColumns.RECORDED_DATE + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return null;
            }
            if (c.isNull(0)) return null;
            return c.getLong(0);
        } finally {
            c.close();
        }
    }

    public Long getLastLogDate(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "max(" + LogColumns.RECORDED_DATE + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return null;
            }
            if (c.isNull(0)) return null;
            return c.getLong(0);
        } finally {
            c.close();
        }
    }

    public Float getAverageCadence(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "avg(" + LogColumns.CADENCE + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return null;
            }
            if (c.isNull(0)) return null;
            return c.getFloat(0);
        } finally {
            c.close();
        }
    }

    public Float getMaxCadence(Uri rideUri) {
        long rideId = ContentUris.parseId(rideUri);
        String[] projection = { "max(" + LogColumns.CADENCE + ")" };
        LogSelection where = new LogSelection();
        where.rideId(rideId);
        Cursor c = mContext.getContentResolver().query(LogColumns.CONTENT_URI, projection, where.sel(), where.args(), null);
        try {
            if (!c.moveToNext()) {
                return null;
            }
            if (c.isNull(0)) return null;
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
