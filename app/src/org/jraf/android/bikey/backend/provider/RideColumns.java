package org.jraf.android.bikey.backend.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Columns for the {@code ride} table.
 */
public class RideColumns implements BaseColumns {
    public static final String TABLE_NAME = "ride";
    public static final Uri CONTENT_URI = Uri.parse(BikeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String NAME = "name";
    public static final String CREATED_DATE = "created_date";
    public static final String STATE = "state";
    public static final String ACTIVATED_DATE = "activated_date";
    public static final String DURATION = "duration";
    public static final String DISTANCE = "distance";

    public static final String DEFAULT_ORDER = _ID;
}
