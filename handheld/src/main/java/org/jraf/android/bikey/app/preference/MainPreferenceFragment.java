/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * Copyright (C) 2017 Carmen Alvarez (c@rmen.ca)
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.heartrate.HeartRateListener;
import org.jraf.android.bikey.backend.heartrate.HeartRateManager;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.bikey.util.MediaButtonUtil;

public class MainPreferenceFragment extends PreferenceFragment {
    private PreferenceCallbacks mCallbacks;

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        updateListPreferenceSummary(Constants.PREF_UNITS);

        // Show heart rate section only if supported
        boolean heartRateSupported = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (heartRateSupported) {
            findPreference(Constants.PREF_HEART_RATE_SCAN).setOnPreferenceClickListener(mOnPreferenceClickListener);
        } else {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(Constants.PREF_CATEGORY_HEART_RATE);
            getPreferenceScreen().removePreference(preferenceCategory);
        }

        // The headset button implementation is easier if we support lollipop+ only.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(Constants.PREF_CATEGORY_GENERAL);
            Preference headsetPreference = preferenceCategory.findPreference(Constants.PREF_LISTEN_TO_HEADSET_BUTTON);
            preferenceCategory.removePreference(headsetPreference);
        }
        findPreference(Constants.PREF_EXPORT).setOnPreferenceClickListener(mOnPreferenceClickListener);
        findPreference(Constants.PREF_IMPORT).setOnPreferenceClickListener(mOnPreferenceClickListener);
        findPreference(Constants.SYNC_WITH_GOOGLE_DRIVE).setOnPreferenceClickListener(mOnPreferenceClickListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (PreferenceCallbacks) activity;
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    public PreferenceCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        // HeartRate
        HeartRateManager heartRateManager = HeartRateManager.get();
        heartRateManager.addListener(mHeartRateListener);
        if (heartRateManager.isConnecting()) {
            mHeartRateListener.onConnecting();
        } else if (heartRateManager.isConnected()) {
            mHeartRateListener.onConnected();
        } else {
            mHeartRateListener.onHeartRateChange(heartRateManager.getLastValue());
            mHeartRateListener.onDisconnected();
        }
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        // HeartRate
        HeartRateManager.get().removeListener(mHeartRateListener);
        super.onStop();
    }

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        updateListPreferenceSummary(key);
        switch (key) {
            case Constants.PREF_UNITS:
                // Update units
                UnitUtil.readPreferences(getActivity());

                // Propagate unit preferences to wearables
                WearCommHelper.get().updatePreferences();

                // Notify observers of rides since they display distances using a conversion depending on the preference
                getActivity().getContentResolver().notifyChange(RideColumns.CONTENT_URI, null);
                break;

            case Constants.PREF_LISTEN_TO_HEADSET_BUTTON:
                if (sharedPreferences.getBoolean(key, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
                    MediaButtonUtil.registerMediaButtonEventReceiver(getActivity());
                } else {
                    MediaButtonUtil.unregisterMediaButtonEventReceiver();
                }
                break;

            case Constants.PREF_RECORD_CADENCE:
                if (sharedPreferences.getBoolean(Constants.PREF_RECORD_CADENCE, Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                    getCallbacks().showRecordCadenceConfirmDialog();
                } else {
                    // The pref was unchecked because the user pressed 'No' in the confirmation dialog.
                    // Update the switch.
                    SwitchPreference pref = (SwitchPreference) getPreferenceManager().findPreference(Constants.PREF_RECORD_CADENCE);
                    pref.setChecked(false);
                }
                break;
        }
    };

    private void updateListPreferenceSummary(String key) {
        if (Constants.PREF_UNITS.equals(key)) {
            ListPreference pref = (ListPreference) getPreferenceManager().findPreference(key);
            CharSequence entry = pref.getEntry();
            pref.setSummary(entry);
        }
    }

    private OnPreferenceClickListener mOnPreferenceClickListener = preference -> {
        if (Constants.PREF_EXPORT.equals(preference.getKey())) {
            getCallbacks().startExport();
            return true;
        } else if (Constants.PREF_IMPORT.equals(preference.getKey())) {
            getCallbacks().startImport();
            return true;
        } else if (Constants.SYNC_WITH_GOOGLE_DRIVE.equals(preference.getKey())) {
            getCallbacks().syncWithGoogleDrive();
            return true;
        } else if (Constants.PREF_HEART_RATE_SCAN.equals(preference.getKey())) {
            HeartRateManager heartRateManager = HeartRateManager.get();
            if (heartRateManager.isConnected()) {
                getCallbacks().disconnectHeartRateMonitor();
            } else if (heartRateManager.isConnecting()) {
                getCallbacks().tryToReconnectHeartRateMonitorOrGiveUp();
            } else {
                getCallbacks().startHeartRateMonitorScan();
            }
            return true;
        }
        return false;
    };

    private HeartRateListener mHeartRateListener = new HeartRateListener() {
        @Override
        public void onConnecting() {
            Preference pref = getPreferenceManager().findPreference(Constants.PREF_HEART_RATE_SCAN);
            pref.setTitle(R.string.preference_heartRate_connecting_title);
            pref.setSummary(R.string.preference_heartRate_connecting_summary);
            pref.setWidgetLayoutResource(R.layout.heart_rate_pref_widget_connecting);
        }

        @Override
        public void onConnected() {
            Preference pref = getPreferenceManager().findPreference(Constants.PREF_HEART_RATE_SCAN);
            pref.setTitle(R.string.preference_heartRate_disconnect_title);
            pref.setSummary(R.string.preference_heartRate_disconnect_summary);
            pref.setWidgetLayoutResource(R.layout.heart_rate_pref_widget_connected);
        }

        @Override
        public void onHeartRateChange(int bpm) {}

        @Override
        public void onDisconnected() {
            Preference pref = getPreferenceManager().findPreference(Constants.PREF_HEART_RATE_SCAN);
            if (pref == null) {
                // Could be null here, on devices where BTLE is not supported
                return;
            }
            pref.setEnabled(true);
            pref.setTitle(R.string.preference_heartRate_scan_title);
            pref.setSummary(R.string.preference_heartRate_scan_summary);
            pref.setWidgetLayoutResource(0);
        }

        @Override
        public void onError() {
            Toast.makeText(getActivity(), R.string.preference_heartRate_bt_error, Toast.LENGTH_LONG).show();
        }
    };
}
