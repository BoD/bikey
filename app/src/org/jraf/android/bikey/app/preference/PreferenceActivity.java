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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.util.MediaButtonUtil;
import org.jraf.android.bikey.util.UnitUtil;

public class PreferenceActivity extends android.preference.PreferenceActivity {
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
            if (Constants.PREF_UNITS.equals(key)) {
                UnitUtil.readPreferences(PreferenceActivity.this);

                // Notify observers of rides since they display distances using a conversion depending on the preference
                getContentResolver().notifyChange(RideColumns.CONTENT_URI, null);
            } else if (Constants.PREF_LISTEN_TO_HEADSET_BUTTON.equals(key)) {
                if (sharedPreferences.getBoolean(key, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
                    MediaButtonUtil.registerMediaButtonEventReceiver(PreferenceActivity.this);
                } else {
                    MediaButtonUtil.unregisterMediaButtonEventReceiver(PreferenceActivity.this);
                }
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
}
