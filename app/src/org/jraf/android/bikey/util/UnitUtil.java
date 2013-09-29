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

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class UnitUtil {
    private static DecimalFormat FORMAT_SPEED = new DecimalFormat("0.0");
    private static DecimalFormat FORMAT_DISTANCE = new DecimalFormat("0.00");
    private static DecimalFormat FORMAT_SLOPE = new DecimalFormat("0.0");
    private static char sDecimalSeparator;

    static {
        sDecimalSeparator = FORMAT_DISTANCE.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public static CharSequence formatSpeed(float metersPerSecond) {
        if (metersPerSecond == 0f) return "0";
        float kmPerHour = metersPerSecond * 3.6f;
        String speedStr = FORMAT_SPEED.format(kmPerHour);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(.5f), speedStr.indexOf(sDecimalSeparator), speedStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters, boolean withUnit) {
        String unit = withUnit ? " km" : "";
        if (meters == 0f) return "0" + unit;
        float km = meters / 1000f;
        String distStr = FORMAT_DISTANCE.format(km) + unit;
        SpannableString builder = new SpannableString(distStr);
        builder.setSpan(new RelativeSizeSpan(.5f), distStr.indexOf(sDecimalSeparator), distStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters) {
        return formatDistance(meters, false);
    }

    public static CharSequence formatSlope(float fraction) {
        if (fraction == 0f) return "0";
        float percent = fraction * 100f;
        String slopeStr = FORMAT_SLOPE.format(percent);
        SpannableString builder = new SpannableString(slopeStr);
        builder.setSpan(new RelativeSizeSpan(.5f), slopeStr.indexOf(sDecimalSeparator), slopeStr.length(), 0);
        return builder;
    }

    public static CharSequence formatCompass(float fraction) {
        if (fraction == 0f) return "0";
        String slopeStr = FORMAT_SLOPE.format(fraction);
        SpannableString builder = new SpannableString(slopeStr);
        builder.setSpan(new RelativeSizeSpan(.5f), slopeStr.indexOf(sDecimalSeparator), slopeStr.length(), 0);
        return builder;
    }
}
