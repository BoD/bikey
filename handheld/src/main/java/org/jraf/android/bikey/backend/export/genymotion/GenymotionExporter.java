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
package org.jraf.android.bikey.backend.export.genymotion;

import java.io.IOException;
import java.io.PrintWriter;

import android.content.ContentUris;
import android.net.Uri;

import org.jraf.android.bikey.backend.export.Exporter;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.log.LogCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;

public class GenymotionExporter extends Exporter {
    public GenymotionExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".gm");
    }

    @Override
    @Background
    public void export() throws IOException {
        PrintWriter out = new PrintWriter(getExportFile());
        long rideId = ContentUris.parseId(getRideUri());
        String selection = LogColumns.RIDE_ID + "=?";
        String[] selectionArgs = { String.valueOf(rideId) };
        LogCursor c = new LogCursor(getContext().getContentResolver().query(LogColumns.CONTENT_URI, null, selection, selectionArgs, null));
        try {
            while (c.moveToNext()) {
                String lat = String.valueOf(c.getLat());
                String lon = String.valueOf(c.getLon());
                String ele = String.valueOf(c.getEle());
                out.println("gps setlatitude " + lat);
                out.println("gps setlongitude " + lon);
                out.println("gps setaltitude " + ele);
                out.println("pause 1");
            }
        } finally {
            c.close();
        }
        IoUtil.closeSilently(out);
    }
}
