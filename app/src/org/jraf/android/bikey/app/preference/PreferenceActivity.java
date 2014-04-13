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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.dbimport.DatabaseImporter;
import org.jraf.android.util.app.base.BaseFragmentActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;

public class PreferenceActivity extends BaseFragmentActivity implements PreferenceCallbacks, AlertDialogListener {
    private static final int REQUEST_PICK_FILE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }


    /*
     * Cadence confirmation.
     */

    @Override
    public void showRecordCadenceConfirmDialog() {
        AlertDialogFragment.newInstance(0, R.string.preference_recordCadence_confirmDialog_title, R.string.preference_recordCadence_confirmDialog_message, 0,
                R.string.common_yes, R.string.common_no, (Parcelable) null).show(getSupportFragmentManager());
    }

    @Override
    public void onClickNegative(int tag, Object payload) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_RECORD_CADENCE, false).commit();
    }

    @Override
    public void onClickPositive(int tag, Object payload) {}

    @Override
    public void onClickListItem(int tag, int index, Object payload) {}


    /*
     * Database import.
     */

    @Override
    public void startPickFileActivity() {
        Intent importIntent = new Intent(Intent.ACTION_GET_CONTENT);
        String contentType = "application/octet-stream";
        importIntent.setType(contentType);
        importIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (getPackageManager().resolveActivity(importIntent, 0) == null) {
            // No file manager found, try the Samsung specific one.
            importIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            importIntent.putExtra("CONTENT_TYPE", contentType);
        }

        startActivityForResult(Intent.createChooser(importIntent, getString(R.string.ride_list_importDialog_title)), REQUEST_PICK_FILE);
    }

    private void importRides(final Uri ridesFile) {
        new TaskFragment(new Task<PreferenceActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                DatabaseImporter.importDatabase(thiz, ridesFile);
            }
        }.toastFail(R.string.import_failToast).toastOk(R.string.import_successToast)).execute(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_FILE:
                if (resultCode != RESULT_OK) return;
                importRides(data.getData());
                break;
        }
    }
}
