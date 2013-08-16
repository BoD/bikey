package org.jraf.android.bikey.util;

import java.text.DecimalFormat;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class UnitUtil {
    private static DecimalFormat FORMAT_SPEED = new DecimalFormat("0.0");
    private static DecimalFormat FORMAT_DISTANCE = new DecimalFormat("0.00");

    public static CharSequence formatSpeed(float metersPerSecond) {
        if (metersPerSecond == 0f) return "0";
        float kmPerHour = metersPerSecond * 3.6f;
        String speedStr = FORMAT_SPEED.format(kmPerHour);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(.5f), speedStr.length() - 2, speedStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters) {
        if (meters == 0f) return "0";
        float km = meters / 1000f;
        String speedStr = FORMAT_DISTANCE.format(km);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(.5f), speedStr.length() - 3, speedStr.length(), 0);
        return builder;
    }
}
