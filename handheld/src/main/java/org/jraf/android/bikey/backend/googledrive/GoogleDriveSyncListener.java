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

public interface GoogleDriveSyncListener {
    void onSyncStart();

    void onDeleteRemoteItemsStart();

    void onDeleteRemoteItemsFinish();

    void onDeleteLocalItemsStart();

    void onDeleteLocalItemsFinish();

    void onUploadNewLocalItemsStart();

    void onUploadNewLocalItemsProgress(int progress, int total);

    void onUploadNewLocalItemsFinish();

    void onDownloadNewRemoteItemsStart();

    void onDownloadNewRemoteItemsOverallProgress(int progress, int total);

    void onDownloadNewRemoteItemsDownloadProgress(long progress, long total);

    void onDownloadNewRemoteItemsFinish();

    void onSyncFinish(boolean success);
}
