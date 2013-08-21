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
package org.jraf.android.bikey.backend.export;

import java.io.IOException;
import java.io.PrintWriter;

import android.content.ContentUris;
import android.net.Uri;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.LogColumns;
import org.jraf.android.bikey.backend.provider.LogCursorWrapper;
import org.jraf.android.bikey.backend.ride.RideManager;
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
