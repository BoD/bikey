package org.jraf.android.bike.util;

import java.text.DecimalFormat;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class UnitUtil {
    public static CharSequence formatSpeed(float metersPerSecond) {
        float kmPerHour = metersPerSecond * 3.6f;
        if (kmPerHour < 1) return "0";
        DecimalFormat decimalFormat = new DecimalFormat("##0.0");
        String speedStr = decimalFormat.format(kmPerHour);
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(0.75f), speedStr.length() - 2, speedStr.length(), 0);
        return builder;
    }
}
