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
package org.jraf.android.bikey.backend.export.gpx;

import java.io.IOException;
import java.io.PrintWriter;

import android.content.ContentUris;
import android.net.Uri;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.export.Exporter;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;

public class GpxExporter extends Exporter {
    private static final long NEW_SEGMENT_DURATION = 5 * 60 * 1000;

    public GpxExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".gpx");
    }

    @Override
    @Background
    public void export() throws IOException {
        PrintWriter out = new PrintWriter(getOutputStream());
        // Header
        String appName = getString(R.string.app_name);
        String rideName = RideManager.get().getDisplayName(getRideUri());
        out.println(getString(R.string.export_gpx_begin, appName, rideName));

        long rideId = ContentUris.parseId(getRideUri());
        String selection = LogColumns.RIDE_ID + "=?";
        String[] selectionArgs = { String.valueOf(rideId) };
        Long previousRecordedDate = null;
        LogCursor c = new LogCursor(getContext().getContentResolver().query(LogColumns.CONTENT_URI, null, selection, selectionArgs, null));
        try {
            while (c.moveToNext()) {
                long recordedDate = c.getRecordedDate().getTime();
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
