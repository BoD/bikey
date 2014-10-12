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
package org.jraf.android.bikey.app.preference;

import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.heartrate.bluetooth.HeartRateMonitorScanActivity;
import org.jraf.android.bikey.backend.dbimport.DatabaseImporter;
import org.jraf.android.bikey.backend.export.db.DbExporter;
import org.jraf.android.bikey.backend.heartrate.HeartRateManager;
import org.jraf.android.util.app.base.BaseFragmentActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.log.wrapper.Log;

public class PreferenceActivity extends BaseFragmentActivity implements PreferenceCallbacks, AlertDialogListener {
    private static final int REQUEST_PICK_FILE_FOR_IMPORT = 0;
    private static final int REQUEST_SCAN_HEART_RATE_MONITOR = 1;

    private static final int DIALOG_RECORD_CADENCE = 0;
    private static final int DIALOG_DISCONNECT_HEART_RATE = 1;
    private static final int DIALOG_RECONNECT_HEART_RATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * Cadence confirmation.
     */

    @Override
    public void showRecordCadenceConfirmDialog() {
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_RECORD_CADENCE);
        dialog.setTitle(R.string.preference_recordCadence_confirmDialog_title);
        dialog.setMessage(R.string.preference_recordCadence_confirmDialog_message);
        dialog.setPositiveButton(R.string.common_yes);
        dialog.setNegativeButton(R.string.common_no);
        dialog.show(getSupportFragmentManager());
    }


    /*
     * Database import / export.
     */

    @Override
    public void startExport() {
        new TaskFragment(new Task<PreferenceActivity>() {
            DbExporter mExporter = new DbExporter();

            @Override
            protected void doInBackground() throws Throwable {
                mExporter.export();
            }

            @Override
            protected void onPostExecuteOk() {
                File exportedFile = mExporter.getExportFile();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject));
                String messageBody = getString(R.string.export_body);
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + exportedFile.getAbsolutePath()));
                sendIntent.setType("application/bikey");
                sendIntent.putExtra(Intent.EXTRA_TEXT, messageBody);

                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.preference_export_title)));
            }
        }.toastFail(R.string.export_failToast)).execute(getSupportFragmentManager());
    }

    @Override
    public void startImport() {
        Intent importIntent = new Intent(Intent.ACTION_GET_CONTENT);
        String contentType = "application/octet-stream";
        importIntent.setType(contentType);
        importIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (getPackageManager().resolveActivity(importIntent, 0) == null) {
            // No file manager found, try the Samsung specific one.
            importIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            importIntent.putExtra("CONTENT_TYPE", contentType);
        }

        startActivityForResult(Intent.createChooser(importIntent, getString(R.string.ride_list_importDialog_title)), REQUEST_PICK_FILE_FOR_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_FILE_FOR_IMPORT:
                if (resultCode != RESULT_OK) return;
                importRides(data.getData());
                break;
        }
    }

    private void importRides(final Uri ridesFile) {
        new TaskFragment(new Task<PreferenceActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                DatabaseImporter.importDatabase(thiz, ridesFile);
            }
        }.toastFail(R.string.import_failToast).toastOk(R.string.import_successToast)).execute(getSupportFragmentManager());
    }


    /*
     * Heart rate monitor.
     */

    @Override
    public void startHeartRateMonitorScan() {
        Intent intent = new Intent(this, HeartRateMonitorScanActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_HEART_RATE_MONITOR);
    }

    @Override
    public void disconnectHeartRateMonitor() {
        Log.d();
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_DISCONNECT_HEART_RATE);
        dialog.setTitle(R.string.preference_heartRate_disconnect_confirmDialog_title);
        dialog.setMessage(R.string.preference_heartRate_disconnect_confirmDialog_message);
        dialog.setPositiveButton(R.string.preference_heartRate_disconnect_confirmDialog_positive);
        dialog.setNegativeButton(R.string.preference_heartRate_disconnect_confirmDialog_negative);
        dialog.setCancelIsNegative(false);
        dialog.show(getSupportFragmentManager());
    }

    @Override
    public void tryToReconnectOrGiveUp() {
        Log.d();
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_RECONNECT_HEART_RATE);
        dialog.setTitle(R.string.preference_heartRate_reconnect_confirmDialog_title);
        dialog.setMessage(R.string.preference_heartRate_reconnect_confirmDialog_message);
        dialog.setPositiveButton(R.string.preference_heartRate_reconnect_confirmDialog_positive);
        dialog.setNegativeButton(R.string.preference_heartRate_reconnect_confirmDialog_negative);
        dialog.setCancelIsNegative(false);
        dialog.show(getSupportFragmentManager());

    }


    /*
     * AlertDialogListener.
     */

    @Override
    public void onClickNegative(int tag, Object payload) {
        switch (tag) {
            case DIALOG_RECORD_CADENCE:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_RECORD_CADENCE, false).commit();
                break;

            case DIALOG_DISCONNECT_HEART_RATE:
            case DIALOG_RECONNECT_HEART_RATE:
                // Disconnect
                HeartRateManager.get().disconnect();
                break;
        }
    }

    @Override
    public void onClickPositive(int tag, Object payload) {
        switch (tag) {
            case DIALOG_DISCONNECT_HEART_RATE:
                // Disconnect
                HeartRateManager.get().disconnect();

                // Turn off bluetooth
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
                break;

            case DIALOG_RECONNECT_HEART_RATE:
                // Disconnect and go back to scanning
                HeartRateManager.get().disconnect();
                startHeartRateMonitorScan();
                break;
        }
    }

    @Override
    public void onClickListItem(int tag, int index, Object payload) {}
}
