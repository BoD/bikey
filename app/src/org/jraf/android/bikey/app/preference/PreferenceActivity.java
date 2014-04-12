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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.util.MediaButtonUtil;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.util.dialog.AlertDialogListener;

public class PreferenceActivity extends android.preference.PreferenceActivity implements AlertDialogListener {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        updateListPreferenceSummary(Constants.PREF_UNITS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
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

}
