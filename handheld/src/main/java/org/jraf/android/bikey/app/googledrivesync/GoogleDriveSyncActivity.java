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
package org.jraf.android.bikey.app.googledrivesync;

import android.content.Intent;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.googledrive.GoogleDriveSyncListener;
import org.jraf.android.bikey.backend.googledrive.GoogleDriveSyncManager;
import org.jraf.android.bikey.databinding.GoogleDriveSyncBinding;
import org.jraf.android.util.app.base.BaseAppCompatActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.log.Log;

public class GoogleDriveSyncActivity extends BaseAppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleDriveSyncListener, AlertDialogListener {
    private static final int REQUEST_RESOLVE_CONNECTION = 0;
    private static final int DIALOG_ABORT_SYNC_CONFIRM = 0;

    private GoogleDriveSyncBinding mBinding;
    private GoogleApiClient mGoogleApiClient;
    private volatile boolean mSyncOnGoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.google_drive_sync);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        syncWithGoogleDrive();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSyncOnGoing) {
            AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_ABORT_SYNC_CONFIRM);
            dialog.title(R.string.googleDriveSync_abortSyncDialog_title);
            dialog.message(R.string.googleDriveSync_abortSyncDialog_message);
            dialog.positiveButton(R.string.googleDriveSync_abortSyncDialog_positive);
            dialog.negativeButton(R.string.googleDriveSync_abortSyncDialog_negative);
            dialog.show(this);
        } else {
            super.onBackPressed();
        }
    }

    /*
     * Google Drive.
     */

    @WorkerThread
    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("connectionHint=" + connectionHint);
        startGoogleDriveSyncTask();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("cause=" + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_CONNECTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e("Could not resolve connection failed", e);
            }
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_RESOLVE_CONNECTION:
                if (resultCode == RESULT_OK) {
                    getGoogleApiClient().connect();
                }
                break;
        }
    }

    public void syncWithGoogleDrive() {
        if (getGoogleApiClient().isConnected()) {
            // Already connected: sync how
            startGoogleDriveSyncTask();
        } else {
            // Not connected yet: connect - sync will happen when connected
            getGoogleApiClient().connect();
        }
    }

    public void startGoogleDriveSyncTask() {
        mSyncOnGoing = true;
        new TaskFragment(new Task<GoogleDriveSyncActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                GoogleDriveSyncManager.get(GoogleDriveSyncActivity.this).sync(getGoogleApiClient(), GoogleDriveSyncActivity.this);
                mSyncOnGoing = false;
            }
        }).execute(getSupportFragmentManager(), false);
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        super.onDestroy();
    }


    /*
     * GoogleDriveSyncListener implementation.
     */

    @Override
    public void onSyncStart() {
        Log.d();
    }

    @Override
    public void onDeleteRemoteItemsStart() {}

    @Override
    public void onDeleteRemoteItemsFinish() {
        runOnUiThread(() -> mBinding.txtDeleteRemoteItems.setChecked(true));
    }

    @Override
    public void onDeleteLocalItemsStart() {}

    @Override
    public void onDeleteLocalItemsFinish() {
        runOnUiThread(() -> mBinding.txtDeleteLocalItems.setChecked(true));
    }

    @Override
    public void onUploadNewLocalItemsStart() {}

    @Override
    public void onUploadNewLocalItemsProgress(int progress, int total) {
        runOnUiThread(() -> {
            mBinding.pgbUploadNewLocalItems.setMax(total);
            mBinding.pgbUploadNewLocalItems.setProgress(progress);
        });
    }

    @Override
    public void onUploadNewLocalItemsFinish() {
        runOnUiThread(() -> mBinding.txtUploadNewLocalItems.setChecked(true));
    }

    @Override
    public void onDownloadNewRemoteItemsStart() {}

    @Override
    public void onDownloadNewRemoteItemsOverallProgress(int progress, int total) {
        runOnUiThread(() -> mBinding.txtDownloadNewRemoteItems.setText(getString(R.string.googleDriveSync_downloadNewRemoteItems_progress, progress, total)));
    }

    @Override
    public void onDownloadNewRemoteItemsDownloadProgress(long progress, long total) {
        runOnUiThread(() -> {
            mBinding.pgbDownloadNewRemoteItems.setMax((int) total);
            mBinding.pgbDownloadNewRemoteItems.setProgress((int) progress);
        });
    }

    @Override
    public void onDownloadNewRemoteItemsFinish() {
        runOnUiThread(() -> mBinding.txtDownloadNewRemoteItems.setChecked(true)

        );
    }

    @Override
    public void onSyncFinish(boolean success) {
        Log.d();
        runOnUiThread(() -> {
            if (success) {
                mBinding.txtSuccess.setVisibility(View.VISIBLE);
            } else {
                mBinding.txtFail.setVisibility(View.VISIBLE);
            }
        });
    }


    /*
     * AlertDialogListener.
     */

    @Override
    public void onDialogClickPositive(int i, Object o) {
        GoogleDriveSyncManager.get(this).abort();
        super.onBackPressed();
    }

    @Override
    public void onDialogClickNegative(int i, Object o) { }

    @Override
    public void onDialogClickListItem(int i, int i1, Object o) {}
}
