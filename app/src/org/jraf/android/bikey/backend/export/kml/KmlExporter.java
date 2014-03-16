/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Carmen Alvarez (c@rmen.ca)
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
package org.jraf.android.bikey.backend.export.kml;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.content.ContentUris;
import android.net.Uri;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.export.Exporter;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogCursorWrapper;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;

public class KmlExporter extends Exporter {

    public KmlExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    @Background
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".kml");
    }

    @Override
    public void export() throws IOException {
        PrintWriter out = new PrintWriter(getExportFile());
        Uri rideUri = getRideUri();
        // Header
        out.println(getString(R.string.export_kml_document_begin));
        String appName = getString(R.string.app_name);
        String rideName = RideManager.get().getDisplayName(rideUri);
        out.println(getString(R.string.export_kml_name, appName + ": " + rideName));

        String timestampNow = new Date().toString();
        out.println(getString(R.string.export_kml_timestamp, timestampNow));
        long rideId = ContentUris.parseId(rideUri);
        String selection = LogColumns.RIDE_ID + "=?";
        String[] selectionArgs = { String.valueOf(rideId) };
        LogCursorWrapper c = new LogCursorWrapper(getContext().getContentResolver().query(LogColumns.CONTENT_URI, null, selection, selectionArgs, null));
        try {
            c.moveToFirst();
            // Write the LookAt element, which contains the start and end timestamps, and the first coordinate.
            long rideBeginDate = c.getRecordedDate().getTime();
            long rideEndDate = rideBeginDate + RideManager.get().getDuration(rideUri);
            String timestampBegin = DateTimeUtil.toIso8601(rideBeginDate, false);
            String timestampEnd = DateTimeUtil.toIso8601(rideEndDate, false);
            double firstLatitude = c.getLat();
            double firstLongitude = c.getLon();
            double range = 500;
            out.println(getString(R.string.export_kml_look_at, timestampBegin, timestampEnd, firstLongitude, firstLatitude, range));

            // Write the KML elements leading up to the list of track points.
            out.println(getString(R.string.export_kml_style));
            out.println(getString(R.string.export_kml_folder_begin));
            out.println(getString(R.string.export_kml_placemark_begin));
            out.println(getString(R.string.export_kml_name, timestampBegin));
            out.println(getString(R.string.export_kml_style_url));
            out.println(getString(R.string.export_kml_track_begin));

            // Write the timestamps for each track point
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                long recordedDate = c.getRecordedDate().getTime();
                String dateTime = DateTimeUtil.toIso8601(recordedDate, true);
                out.println(getString(R.string.export_kml_when, dateTime));
            }

            // Write the coordinates for each track point
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                double latitude = c.getLat();
                double longitude = c.getLon();
                double elevation = c.getEle();
                out.println(getString(R.string.export_kml_coord, longitude, latitude, elevation));
            }

            // Write the KML elements to close the document.
            out.println(getString(R.string.export_kml_track_end));
            out.println(getString(R.string.export_kml_placemark_end));
            out.println(getString(R.string.export_kml_folder_end));
            out.println(getString(R.string.export_kml_document_end));
        } finally {
            c.close();
        }
        IoUtil.closeSilently(out);
    }
}
