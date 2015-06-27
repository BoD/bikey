/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.backend.export.bikey;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.export.Exporter;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogCursor;
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;

public class BikeyExporter extends Exporter {
    private static final long NEW_SEGMENT_DURATION = 5 * 60 * 1000;

    public BikeyExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".ride");
    }

    @Override
    @Background
    public void export() throws IOException {
        PrintWriter out = new PrintWriter(new BufferedOutputStream(getOutputStream()));
        // Header
        String appVersion = null;
        try {
            appVersion = String.valueOf(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // Can never happen
        }
        String creationDate = DateTimeUtil.toIso8601(System.currentTimeMillis(), true);

        // Query
        long rideId = ContentUris.parseId(getRideUri());
        LogSelection logSelection = new LogSelection();
        logSelection.rideId(rideId);
        LogCursor logCursor = logSelection.query(getContext(), LogColumns.ALL_COLUMNS);

        int logCount = logCursor.getCount();
        out.println(getString(R.string.export_bikey_begin, appVersion, creationDate, logCount));

        RideCursor rideCursor = RideManager.get().query(getRideUri());
        exportCursorRow(rideCursor, out);
        rideCursor.close();

        // Logs
        out.println(getString(R.string.export_bikey_logs_begin));

        while (logCursor.moveToNext()) {
            out.println(getString(R.string.export_bikey_log_begin));
            exportCursorRow(logCursor, out);
            out.println(getString(R.string.export_bikey_log_end));
        }
        logCursor.close();

        // End
        out.println(getString(R.string.export_bikey_logs_end));
        out.println(getString(R.string.export_bikey_end));
        out.flush();
        IoUtil.closeSilently(out);

    }

    private static void exportCursorRow(Cursor cursor, PrintWriter out) {
        int rideCursorColumnCount = cursor.getColumnCount();
        for (int i = 0; i < rideCursorColumnCount; i++) {
            String rideColumnName = cursor.getColumnName(i);
            int rideColumnType = cursor.getType(i);
            out.print("<" + rideColumnName + " type=\"" + rideColumnType + "\">");
            String value = null;
            switch (rideColumnType) {
                case Cursor.FIELD_TYPE_NULL:
                    value = "null";
                    break;

                case Cursor.FIELD_TYPE_STRING:
                    value = cursor.getString(i);
                    break;

                case Cursor.FIELD_TYPE_FLOAT:
                    value = String.valueOf(cursor.getDouble(i));
                    break;

                case Cursor.FIELD_TYPE_INTEGER:
                    value = String.valueOf(cursor.getLong(i));
                    break;
            }
            out.print(value);
            out.println("</" + rideColumnName + ">");
        }
    }
}
