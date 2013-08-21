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

import java.io.File;
import java.io.IOException;

import android.net.Uri;

import org.jraf.android.bikey.backend.provider.BikeySQLiteOpenHelper;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.file.FileUtil;

public class DbExporter extends Exporter {
    public DbExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    @Background
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".db");
    }

    @Override
    public void export() throws IOException {
        File dbFile = getContext().getDatabasePath(BikeySQLiteOpenHelper.DATABASE_NAME);
        FileUtil.copy(dbFile, getExportFile());
    }
}
