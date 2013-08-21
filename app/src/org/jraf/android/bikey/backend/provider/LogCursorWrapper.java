package org.jraf.android.bikey.backend.provider;

import android.database.Cursor;

/**
 * Cursor wrapper for the {@code log} table.
 */
public class LogCursorWrapper extends AbstractCursorWrapper {
    public LogCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        return getLongOrNull(LogColumns._ID);
    }

    public Long getRideId() {
        return getLongOrNull(LogColumns.RIDE_ID);
    }

    public Long getRecordedDate() {
        return getLongOrNull(LogColumns.RECORDED_DATE);
    }

    public Double getLat() {
        return getDoubleOrNull(LogColumns.LAT);
    }

    public Double getLon() {
        return getDoubleOrNull(LogColumns.LON);
    }

    public Double getEle() {
        return getDoubleOrNull(LogColumns.ELE);
    }

    public Long getDuration() {
        return getLongOrNull(LogColumns.DURATION);
    }

    public Double getDistance() {
        return getDoubleOrNull(LogColumns.DISTANCE);
    }

    public Double getSpeed() {
        return getDoubleOrNull(LogColumns.SPEED);
    }
}
