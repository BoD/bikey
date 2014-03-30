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
package org.jraf.android.bikey.backend.export.db;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

import org.jraf.android.bikey.backend.export.Exporter;
import org.jraf.android.bikey.backend.provider.BikeySQLiteOpenHelper;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.file.FileUtil;

public class DbExporter extends Exporter {
    public DbExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".db");
    }

    @Override
    @Background
    public void export() throws IOException {
        File dbFile = getContext().getDatabasePath(BikeySQLiteOpenHelper.DATABASE_FILE_NAME);
        FileUtil.copy(dbFile, getExportFile());
    }
}
