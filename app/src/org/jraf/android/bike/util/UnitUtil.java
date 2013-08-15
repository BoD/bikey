package org.jraf.android.bike.util;

import java.text.DecimalFormat;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class UnitUtil {
    private static DecimalFormat FORMAT_SPEED = new DecimalFormat("##0.0");
    private static DecimalFormat FORMAT_DISTANCE = new DecimalFormat("##0.0");

    public static CharSequence formatSpeed(float metersPerSecond) {
        float kmPerHour = metersPerSecond * 3.6f;
        if (kmPerHour == 0f) return "0";
        String speedStr = FORMAT_SPEED.format(kmPerHour);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(.5f), speedStr.length() - 2, speedStr.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters) {
        //TODO for now we use exactly the same format for speed and distances
        return formatSpeed(meters);
    }
}
