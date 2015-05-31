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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.bikey.backend.dbimport.BikeyRideImporter;
import org.jraf.android.bikey.backend.export.bikey.BikeyExporter;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.provider.ride.RideSelection;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Query;

public class GoogleDriveSyncManager {
    private static final GoogleDriveSyncManager INSTANCE = new GoogleDriveSyncManager();

    private static final long AWAIT_DELAY_SHORT = 10;
    private static final TimeUnit AWAIT_UNIT_SHORT = TimeUnit.SECONDS;

    private static final long AWAIT_DELAY_LONG = 4;
    private static final TimeUnit AWAIT_UNIT_LONG = TimeUnit.MINUTES;

    private static final String EXTENSION = ".ride";
    private static final String MIME_TYPE = "application/vnd.jraf.bikey.ride";

    private Context mContext;

    public static GoogleDriveSyncManager get(Context context) {
        INSTANCE.mContext = context.getApplicationContext();
        return INSTANCE;
    }

    private GoogleDriveSyncManager() {}

    @WorkerThread
    public boolean sync(GoogleApiClient googleApiClient) {
        Log.d("Get server list");
        ArrayList<ServerItem> serverItems = getServerItems(googleApiClient);
        Log.d("serverItems=" + serverItems);
        if (serverItems == null) {
            Log.d("Got null serverItems: abort");
            return false;
        }

        Log.d("Get locally deleted items");
        ArrayList<String> locallyDeletedItems = getLocallyDeletedItems();
        Log.d("locallyDeletedItems=" + locallyDeletedItems);

        if (!locallyDeletedItems.isEmpty()) {
            Log.d("Delete locally deleted items from the server");
            boolean ok = serverDeleteItems(googleApiClient, locallyDeletedItems, serverItems);
            Log.d("ok=" + ok);
            if (ok) {
                Log.d("Purge locally deleted items");
                purgeLocallyDeletedItems();
            }
        }

        Log.d("Delete local items that are marked as deleted on the server");
        locallyDeleteRemotelyDeletedItems(serverItems);

        Log.d("Get new local items");
        ArrayList<String> newLocalItems = getNewLocalItems(serverItems);
        Log.d("newLocalItems=" + newLocalItems);

        Log.d("Send new local items to the server");
        boolean ok = sendNewLocalItems(googleApiClient, newLocalItems);
        Log.d("ok=" + ok);

        Log.d("Get new server items");
        ArrayList<String> allLocalItems = getAllLocalItems();
        //        ArrayList<String> allLocalItems = newLocalItems; // Uncomment to test
        ArrayList<ServerItem> newServerItems = getNewServerItems(serverItems, allLocalItems);
        Log.d("newServerItems=" + newServerItems);

        if (!newServerItems.isEmpty()) {
            Log.d("Download new server items");
            ok = ok && downloadNewServerItems(googleApiClient, newServerItems);
            Log.d("ok=" + ok);
        }

        Log.d("Sync finished");
        return ok;
    }

    @WorkerThread
    @Nullable
    public ArrayList<ServerItem> getServerItems(GoogleApiClient googleApiClient) {
        Log.d();
        Query query = new Query.Builder().build();
        DriveApi.MetadataBufferResult metadataBufferResult =
                Drive.DriveApi.getAppFolder(googleApiClient).queryChildren(googleApiClient, query).await(AWAIT_DELAY_SHORT, AWAIT_UNIT_SHORT);
        Status status = metadataBufferResult.getStatus();
        Log.d("status=" + status);
        if (!status.isSuccess()) {
            Log.w("Could not query app folder");
            metadataBufferResult.release();
            return null;
        }
        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();
        int count = metadataBuffer.getCount();
        Log.d("count=" + count);
        ArrayList<ServerItem> res = new ArrayList<>(count);
        for (Metadata metadata : metadataBuffer) {
            res.add(new ServerItem(metadata));
        }
        metadataBufferResult.release();
        Log.d(res.toString());
        return res;
    }

    @WorkerThread
    private ArrayList<String> getLocallyDeletedItems() {
        Log.d();
        RideSelection rideSelection = new RideSelection();
        rideSelection.state(RideState.DELETED);
        RideCursor c = rideSelection.query(mContext);
        ArrayList<String> res = new ArrayList<>();
        while (c.moveToNext()) {
            res.add(c.getUuid());
        }
        c.close();
        return res;
    }

    @WorkerThread
    private boolean serverDeleteItems(GoogleApiClient googleApiClient, ArrayList<String> locallyDeletedItems, ArrayList<ServerItem> serverItems) {
        // Find the server items to delete
        ArrayList<ServerItem> serverItemsToDelete = new ArrayList<>();

        // FIXME: Double iteration!  Bad perf!
        for (String locallyDeletedItem : locallyDeletedItems) {
            for (ServerItem serverItem : serverItems) {
                if (serverItem.uuid.equals(locallyDeletedItem)) {
                    serverItemsToDelete.add(serverItem);
                    break;
                }
            }
        }
        Log.d("Server items to delete: " + serverItemsToDelete);
        for (ServerItem serverItem : serverItemsToDelete) {
            Log.d("Delete (mark as trashed) " + serverItem);
            DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, serverItem.driveId);
            // Mark the file as trashed
            Status status = driveFile.trash(googleApiClient).await(AWAIT_DELAY_SHORT, AWAIT_UNIT_SHORT);
            Log.d("status=" + status);
            if (!status.isSuccess()) {
                Log.w("Could not mark as trashed " + serverItem);
                return false;
            }
        }
        return true;
    }

    @WorkerThread
    private void purgeLocallyDeletedItems() {
        Log.d();
        RideSelection rideSelection = new RideSelection();
        rideSelection.state(RideState.DELETED);
        rideSelection.delete(mContext);
    }

    @WorkerThread
    private void locallyDeleteRemotelyDeletedItems(ArrayList<ServerItem> serverItems) {
        Log.d();
        ArrayList<String> uuidsToDelete = new ArrayList<>(serverItems.size());
        for (ServerItem serverItem : serverItems) {
            if (serverItem.deleted) uuidsToDelete.add(serverItem.uuid);
        }
        Log.d("uuidsToDelete=" + uuidsToDelete);
        if (uuidsToDelete.size() > 0) {
            RideSelection rideSelection = new RideSelection();
            rideSelection.uuid(uuidsToDelete.toArray(new String[uuidsToDelete.size()]));
            rideSelection.delete(mContext);
        }
    }

    @WorkerThread
    private ArrayList<String> getNewLocalItems(ArrayList<ServerItem> serverItems) {
        Log.d();
        RideSelection rideSelection = new RideSelection();
        rideSelection.stateNot(RideState.DELETED);
        RideCursor c = rideSelection.query(mContext);
        ArrayList<String> res = new ArrayList<>();
        // FIXME: Double iteration!  Bad perf!
        while (c.moveToNext()) {
            String uuid = c.getUuid();

            boolean existsOnServer = false;
            for (ServerItem serverItem : serverItems) {
                if (serverItem.uuid.equals(uuid)) {
                    // Already exists on the server!  Skip it
                    existsOnServer = true;
                    break;
                }
            }
            if (!existsOnServer) res.add(uuid);
        }
        c.close();
        return res;
    }

    @WorkerThread
    private boolean sendNewLocalItems(GoogleApiClient googleApiClient, ArrayList<String> newLocalItems) {
        Log.d();
        for (String uuid : newLocalItems) {
            Log.d("Creating " + uuid);

            DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(googleApiClient).await(AWAIT_DELAY_LONG, AWAIT_UNIT_LONG);
            Status status = driveContentsResult.getStatus();
            Log.d("driveContentsResult.status=" + status);
            if (!status.isSuccess()) {
                Log.w("Could not create new Drive contents");
                return false;
            }

            RideSelection rideSelection = new RideSelection();
            rideSelection.uuid(uuid);
            RideCursor rideCursor = rideSelection.query(mContext);
            rideCursor.moveToFirst();
            Uri rideUri = ContentUris.withAppendedId(RideColumns.CONTENT_URI, rideCursor.getId());
            rideCursor.close();
            Log.d("rideUri=" + rideUri);

            DriveContents driveContents = driveContentsResult.getDriveContents();
            OutputStream outputStream = driveContents.getOutputStream();
            BikeyExporter exporter = new BikeyExporter(rideUri);
            exporter.setOutputStream(outputStream);
            try {
                exporter.export();
                outputStream.flush();
                IoUtil.closeSilently(outputStream);
            } catch (IOException e) {
                Log.w("Could not export to Drive contents", e);
                return false;
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(uuid + EXTENSION)
                    .setMimeType(MIME_TYPE).build();
            DriveFolder.DriveFileResult driveFileResult =
                    Drive.DriveApi.getAppFolder(googleApiClient).createFile(googleApiClient, changeSet, driveContents).await(AWAIT_DELAY_LONG,
                            AWAIT_UNIT_LONG);
            status = driveFileResult.getStatus();
            Log.d("driveFileResult.status=" + status);
            if (!status.isSuccess()) {
                Log.w("Could not create new Drive file");
                return false;
            }
        }
        return true;
    }

    @WorkerThread
    private ArrayList<String> getAllLocalItems() {
        Log.d();
        RideSelection rideSelection = new RideSelection();
        rideSelection.stateNot(RideState.DELETED);
        RideCursor c = rideSelection.query(mContext);
        ArrayList<String> res = new ArrayList<>();
        while (c.moveToNext()) {
            res.add(c.getUuid());
        }
        c.close();
        return res;
    }

    private ArrayList<ServerItem> getNewServerItems(ArrayList<ServerItem> serverItems, ArrayList<String> allLocalItems) {
        ArrayList<ServerItem> res = new ArrayList<>();
        // FIXME: Double iteration!  Bad perf!
        for (ServerItem serverItem : serverItems) {
            if (serverItem.deleted) continue;
            boolean existsLocally = false;
            for (String localItem : allLocalItems) {
                if (serverItem.uuid.equals(localItem)) {
                    // Already exists locally!  Skip it
                    existsLocally = true;
                    break;
                }
            }
            if (!existsLocally) res.add(serverItem);
        }
        return res;
    }

    @WorkerThread
    private boolean downloadNewServerItems(GoogleApiClient googleApiClient, ArrayList<ServerItem> newServerItems) {
        Log.d();
        for (ServerItem serverItem : newServerItems) {
            Log.d("Download " + serverItem);
            DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, serverItem.driveId);
            DriveApi.DriveContentsResult driveContentsResult =
                    driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long bytesExpected) {
                            Log.d(bytesDownloaded + "/" + bytesDownloaded);
                        }
                    }).await(AWAIT_DELAY_LONG, AWAIT_UNIT_LONG);
            Status status = driveContentsResult.getStatus();
            Log.d("driveContentsResult.status=" + status);
            if (!status.isSuccess()) {
                Log.w("Could not open Drive contents");
                return false;
            }

            DriveContents contents = driveContentsResult.getDriveContents();
            InputStream inputStream = contents.getInputStream();
            try {
                new BikeyRideImporter(mContext.getContentResolver(), inputStream).doImport();
            } catch (Exception e) {
                Log.w("Could not parse or read Drive contents", e);
                contents.discard(googleApiClient);
                return false;
            }
            contents.discard(googleApiClient);
        }

        return true;
    }
}
