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
package org.jraf.android.bikey.backend.googledrive;

import java.util.Map;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.metadata.CustomPropertyKey;

public class ServerItem {
    /**
     * Local ride uuid.
     */
    public String uuid;

    /**
     * Google drive id.
     */
    public DriveId driveId;

    /**
     * The item is marked as deleted on the server.
     */
    boolean deleted;

    public ServerItem(Metadata metadata) {
        uuid = metadata.getTitle().split("\\.")[0]; // Get only the file name, not the extension
        driveId = metadata.getDriveId();

        CustomPropertyKey key = new CustomPropertyKey(GoogleDriveSyncManager.PROPERTY_TRASHED, CustomPropertyKey.PRIVATE);
        Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
        deleted = customProperties.containsKey(key) && GoogleDriveSyncManager.PROPERTY_TRASHED_TRUE.equals(customProperties.get(key));
    }

    @Override
    public String toString() {
        return "ServerItem{" + "uuid='" + uuid + '\'' + ", driveId=" + driveId + ", deleted=" + deleted + '}';
    }
}
