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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.dbimport.DBImport;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.util.MediaButtonUtil;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.log.wrapper.Log;

public class PreferenceActivity extends android.preference.PreferenceActivity implements AlertDialogListener {
    private static final int REQUEST_IMPORT = 1;
    private static final String ACTION_IMPORT_COMPLETE = "action_import_complete";

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        updateListPreferenceSummary(Constants.PREF_UNITS);
        Preference prefImport = findPreference(Constants.PREF_IMPORT);
        prefImport.setOnPreferenceClickListener(mOnPreferenceClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        IntentFilter filter = new IntentFilter(ACTION_IMPORT_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateListPreferenceSummary(key);
            switch (key) {
                case Constants.PREF_UNITS:
                    UnitUtil.readPreferences(PreferenceActivity.this);

                    // Notify observers of rides since they display distances using a conversion depending on the preference
                    getContentResolver().notifyChange(RideColumns.CONTENT_URI, null);
                    break;

                case Constants.PREF_LISTEN_TO_HEADSET_BUTTON:
                    if (sharedPreferences.getBoolean(key, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
                        MediaButtonUtil.registerMediaButtonEventReceiver(PreferenceActivity.this);
                    } else {
                        MediaButtonUtil.unregisterMediaButtonEventReceiver(PreferenceActivity.this);
                    }
                    break;

                case Constants.PREF_RECORD_CADENCE:
                    if (sharedPreferences.getBoolean(Constants.PREF_RECORD_CADENCE, Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                        new AlertDialogFragment().show(getFragmentManager());
                    }
                    break;
            }
        }
    };

    private void updateListPreferenceSummary(String key) {
        if (Constants.PREF_UNITS.equals(key)) {
            @SuppressWarnings("deprecation")
            ListPreference pref = (ListPreference) getPreferenceManager().findPreference(key);
            CharSequence entry = pref.getEntry();
            pref.setSummary(entry);
        }
    }

    /*
     * Import rides from a file
     */

    private void importRides(final Uri ridesFile) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
                progressDialogFragment.setCancelable(false);
                progressDialogFragment.show(getFragmentManager());
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    DBImport.importDB(getApplicationContext(), ridesFile);
                    return true;
                } catch (Exception e) {
                    Log.e(e.getMessage(), e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) Toast.makeText(getApplicationContext(), R.string.import_successToast, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.import_failToast, Toast.LENGTH_LONG).show();
                // Notify ourselves with a broadcast.  If the user rotated the device, this activity
                // won't be visible any more. The new activity will receive the broadcast and update
                // the UI.
                Intent intent = new Intent(ACTION_IMPORT_COMPLETE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }.execute();
    }

    /**
     * A progress dialog for the DB import.
     */
    public static class ProgressDialogFragment extends DialogFragment {
        private static final String PREFIX = ProgressDialogFragment.class.getName() + ".";
        public static final String FRAGMENT_TAG = PREFIX + "FRAGMENT_TAG";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.common_pleaseWait));
            return dialog;
        }

        /**
         * Show this {@link ProgressDialogFragment}.
         */
        public void show(FragmentManager manager) {
            show(manager, FRAGMENT_TAG);
        }
    }

    public static class AlertDialogFragment extends DialogFragment {
        private static final String PREFIX = AlertDialogFragment.class.getName() + ".";
        public static final String FRAGMENT_TAG = PREFIX + "FRAGMENT_TAG";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.preference_recordCadence_confirmDialog_title);
            builder.setMessage(R.string.preference_recordCadence_confirmDialog_message);
            OnClickListener positiveOnClickListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AlertDialogListener) getActivity()).onClickPositive(0, null);
                }
            };

            OnClickListener negativeOnClickListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AlertDialogListener) getActivity()).onClickNegative(0, null);
                }
            };
            builder.setPositiveButton(R.string.common_yes, positiveOnClickListener);
            builder.setNegativeButton(R.string.common_no, negativeOnClickListener);
            return builder.create();
        }

        /**
         * Show this {@link AlertDialogFragment}.
         */
        public void show(FragmentManager manager) {
            show(manager, FRAGMENT_TAG);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);

            if (getActivity() instanceof AlertDialogListener) {
                ((AlertDialogListener) getActivity()).onClickNegative(0, null);
            }
        }
    }

    @Override
    public void onClickPositive(int tag, Object payload) {}

    @Override
    public void onClickNegative(int tag, Object payload) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_RECORD_CADENCE, false).commit();
        SwitchPreference pref = (SwitchPreference) getPreferenceManager().findPreference(Constants.PREF_RECORD_CADENCE);
        pref.setChecked(false);

    }

    @Override
    public void onClickListItem(int tag, int index, Object payload) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMPORT:
                if (resultCode != RESULT_OK) return;
                importRides(data.getData());
                break;
        }
    }

    private OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (Constants.PREF_IMPORT.equals(preference.getKey())) {
                Intent importIntent = new Intent(Intent.ACTION_GET_CONTENT);
                importIntent.setType("file/*");
                startActivityForResult(Intent.createChooser(importIntent, getResources().getText(R.string.ride_list_importDialog_title)), REQUEST_IMPORT);
                return true;
            }
            return false;
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // The DB import has completed.  Dismiss the progress dialog.
            if (ACTION_IMPORT_COMPLETE.equals(intent.getAction())) {
                ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
                if (dialogFragment != null) dialogFragment.dismiss();
            }
        }
    };

}
