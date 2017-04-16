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
package org.jraf.android.bikey.app.ride.edit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.app.base.BaseAppCompatActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

public class RideEditActivity extends BaseAppCompatActivity {
    private EditText mEdtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_edit);
        mEdtName = (EditText) findViewById(R.id.edtName);
        mEdtName.setOnEditorActionListener((v, actionId, event) -> {
            saveRide();
            return true;
        });
        populateViews();
        setupActionBar();
    }

    private void populateViews() {
        Uri rideUri = getIntent().getData();
        if (rideUri == null) return;
        new TaskFragment(new Task<RideEditActivity>() {
            private String mRideName;

            @Override
            protected void doInBackground() throws Throwable {
                mRideName = RideManager.get().getName(rideUri);
            }

            @Override
            protected void onPostExecuteOk() {
                if (mRideName != null) mEdtName.append(mRideName);
            }
        }).execute(getSupportFragmentManager());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View customActionBarView = getLayoutInflater().inflate(R.layout.ride_edit_actionbar, null);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View btnDone = customActionBarView.findViewById(R.id.actionbar_done);
        btnDone.setOnClickListener(v -> saveRide());

        View btnDiscard = customActionBarView.findViewById(R.id.actionbar_discard);
        btnDiscard.setOnClickListener(v -> finish());
    }

    private void saveRide() {
        new TaskFragment(new Task<RideEditActivity>() {
            private Uri mRideUri;

            @Override
            protected void doInBackground() throws Throwable {
                String name = getActivity().mEdtName.getText().toString().trim();
                Uri rideUri = getIntent().getData();
                if (rideUri == null) {
                    // Create
                    mRideUri = RideManager.get().create(name);

                    // Save the new ride as the current one
                    RideManager.get().setCurrentRide(mRideUri);
                } else {
                    mRideUri = rideUri;
                    RideManager.get().updateName(rideUri, name);
                }
            }

            @Override
            protected void onPostExecuteOk() {
                setResult(RESULT_OK, new Intent(null, mRideUri));
                getActivity().finish();
            }
        }).execute(getSupportFragmentManager());
    }
}
