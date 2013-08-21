package org.jraf.android.bikey.backend.provider;

import android.database.Cursor;

/**
 * Cursor wrapper for the {@code ride} table.
 */
public class RideCursorWrapper extends AbstractCursorWrapper {
    public RideCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        return getLongOrNull(RideColumns._ID);
    }

    public String getName() {
        Integer index = getCachedColumnIndexOrThrow(RideColumns.NAME);
        return getString(index);
    }

    public Long getCreatedDate() {
        return getLongOrNull(RideColumns.CREATED_DATE);
    }

    public Long getState() {
        return getLongOrNull(RideColumns.STATE);
    }

    public Long getActivatedDate() {
        return getLongOrNull(RideColumns.ACTIVATED_DATE);
    }

    public Long getDuration() {
        return getLongOrNull(RideColumns.DURATION);
    }

    public Double getDistance() {
        return getDoubleOrNull(RideColumns.DISTANCE);
    }
}
