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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import org.jraf.android.bikey.Constants;
import org.jraf.android.util.annotation.Background;

public class UnitUtil {
    private static final float M_TO_KM = 0.001f;
    private static final float M_S_TO_KM_H = 3.6f;
    private static final float M_S_TO_MPH = 2.2369363f;
    private static final float M_TO_MI = 0.00062137119f;

    private static DecimalFormat FORMAT_SPEED = new DecimalFormat("0.0");
    private static DecimalFormat FORMAT_DISTANCE = new DecimalFormat("0.00");
    private static char sDecimalSeparator;
    private static String sUnit;

    static {
        sDecimalSeparator = FORMAT_DISTANCE.getDecimalFormatSymbols().getDecimalSeparator();
    }

    @Background
    public static void readPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sUnit = preferences.getString(Constants.PREF_UNITS, Constants.PREF_UNITS_DEFAULT);
    }

    public static CharSequence formatSpeed(float metersPerSecond) {
        if (metersPerSecond == 0f) return "0";
        float converted;
        if (Constants.PREF_UNITS_METRIC.equals(sUnit)) {
            converted = metersPerSecond * M_S_TO_KM_H;
        } else {
            converted = metersPerSecond * M_S_TO_MPH;
        }
        String speedStr = FORMAT_SPEED.format(converted);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(.5f), speedStr.indexOf(sDecimalSeparator), speedStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters, boolean withUnit) {
        String unit = "";
        float converted;
        if (Constants.PREF_UNITS_METRIC.equals(sUnit)) {
            unit = " km";
            converted = meters * M_TO_KM;
        } else {
            unit = " miles";
            converted = meters * M_TO_MI;
        }
        if (meters == 0f) return "0" + unit;
        String distStr = FORMAT_DISTANCE.format(converted) + unit;
        SpannableString builder = new SpannableString(distStr);
        builder.setSpan(new RelativeSizeSpan(.5f), distStr.indexOf(sDecimalSeparator), distStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters) {
        return formatDistance(meters, false);
    }
}
