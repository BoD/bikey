package org.jraf.android.bike.backend.export;

import java.io.IOException;
import java.io.PrintWriter;

import android.content.ContentUris;
import android.net.Uri;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.provider.LogColumns;
import org.jraf.android.bike.backend.provider.LogCursorWrapper;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;

public class GpxExporter extends Exporter {
    private static final long NEW_SEGMENT_DURATION = 2 * 60 * 1000;

    public GpxExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    @Background
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".gpx");
    }

    @Override
    public void export() throws IOException {
        PrintWriter out = new PrintWriter(getExportFile());
        // Header
        String appName = getString(R.string.app_name);
        String rideName = RideManager.get().getDisplayName(getRideUri());
        out.println(getString(R.string.export_gpx_begin, appName, rideName));

        long rideId = ContentUris.parseId(getRideUri());
        String selection = LogColumns.RIDE_ID + "=?";
        String[] selectionArgs = { String.valueOf(rideId) };
        Long previousRecordedDate = null;
        LogCursorWrapper c = new LogCursorWrapper(getContext().getContentResolver().query(LogColumns.CONTENT_URI, null, selection, selectionArgs, null));
        try {
            while (c.moveToNext()) {
                Long recordedDate = c.getRecordedDate();
                // Track segment
                if (previousRecordedDate == null) {
                    out.println(getString(R.string.export_gpx_trackSegment_begin));
                } else if (recordedDate - previousRecordedDate > NEW_SEGMENT_DURATION) {
                    out.println(getString(R.string.export_gpx_trackSegment_end));
                    out.println(getString(R.string.export_gpx_trackSegment_begin));
                }

                // Track point
                String lat = String.valueOf(c.getLat());
                String lon = String.valueOf(c.getLon());
                String ele = String.valueOf(c.getEle());
                String dateTime = DateTimeUtil.toIso8601(recordedDate, true);
                out.println(getString(R.string.export_gpx_trackPoint, lat, lon, ele, dateTime));

                previousRecordedDate = recordedDate;
            }
        } finally {
            c.close();
        }
        out.println(getString(R.string.export_gpx_trackSegment_end));
        out.println(getString(R.string.export_gpx_end));
        IoUtil.closeSilently(out);
    }
}
