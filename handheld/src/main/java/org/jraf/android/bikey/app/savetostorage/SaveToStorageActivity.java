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
package org.jraf.android.bikey.app.savetostorage;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.jraf.android.bikey.R;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.intent.IntentUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class SaveToStorageActivity extends FragmentActivity {
    private static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";
    private static final int REQUEST_PICK_DIRECTORY = 0;
    private Uri mSourceFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(StringUtil.toString(getIntent()));

        Parcelable extra = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (extra == null || !(extra instanceof Uri)) {
            Toast.makeText(this, getString(R.string.saveToStorage_failedToast), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mSourceFileUri = (Uri) extra;
        if (!"file".equals(mSourceFileUri.getScheme())) {
            Toast.makeText(this, getString(R.string.saveToStorage_failedToast), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent pickDirectoryIntent = new Intent(ACTION_PICK_DIRECTORY);
        if (!IntentUtil.isCallable(this, pickDirectoryIntent)) {
            // Cannot pick a directory: save to the root of the external storage
            copyFile(Environment.getExternalStorageDirectory());
        } else {
            startActivityForResult(pickDirectoryIntent, REQUEST_PICK_DIRECTORY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_DIRECTORY:
                if (resultCode != RESULT_OK) return;
                File destDir = new File(data.getData().getPath());
                copyFile(destDir);
                break;
        }
    }

    private void copyFile(final File destDir) {
        final String fileName = mSourceFileUri.getLastPathSegment();
        final File src = new File(mSourceFileUri.getPath());
        final File dest = new File(destDir, fileName);
        String successToast = getString(R.string.saveToStorage_successToast, dest);
        new TaskFragment(new Task<SaveToStorageActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                FileUtil.copy(src, dest);
            }

            @Override
            protected void onPostExecuteFail() {
                super.onPostExecuteFail();
                finish();
            }

            @Override
            protected void onPostExecuteOk() {
                super.onPostExecuteOk();
                finish();
            }
        }.toastFail(R.string.saveToStorage_failedToast).toastOk(successToast)).execute(getSupportFragmentManager());
    }
}
