package org.jraf.android.bikey.backend.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Columns for the {@code log} table.
 */
public class LogColumns implements BaseColumns {
    public static final String TABLE_NAME = "log";
    public static final Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String RIDE_ID = "ride_id";
    public static final String RECORDED_DATE = "recorded_date";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String ELE = "ele";
    public static final String DURATION = "duration";
    public static final String DISTANCE = "distance";
    public static final String SPEED = "speed";

    public static final String DEFAULT_ORDER = _ID;
}
