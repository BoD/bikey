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
package org.jraf.android.bikey.app;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.util.log.wrapper.Log;

import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashInfo;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashReport;

//@formatter:off
@ReportsCrashes(
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.acra_toast,
    formKey = "",
    formUri = "https://bod.cloudant.com/acra-bikey/_design/acra-storage/_update/report",
    reportType = org.acra.sender.HttpSender.Type.JSON,
    httpMethod = org.acra.sender.HttpSender.Method.PUT,
    customReportContent = {
        ReportField.REPORT_ID,
        ReportField.APP_VERSION_CODE,
        ReportField.APP_VERSION_NAME,
        ReportField.PACKAGE_NAME,
        ReportField.FILE_PATH,
        ReportField.PHONE_MODEL,
        ReportField.BRAND,
        ReportField.PRODUCT,
        ReportField.ANDROID_VERSION,
        ReportField.BUILD,
        ReportField.TOTAL_MEM_SIZE,
        ReportField.AVAILABLE_MEM_SIZE,
        ReportField.CUSTOM_DATA,
        ReportField.IS_SILENT,
        ReportField.STACK_TRACE,
        ReportField.INITIAL_CONFIGURATION,
        ReportField.CRASH_CONFIGURATION,
        ReportField.DISPLAY,
        ReportField.USER_COMMENT,
        ReportField.USER_EMAIL,
        ReportField.USER_APP_START_DATE,
        ReportField.USER_CRASH_DATE,
        ReportField.DUMPSYS_MEMINFO,
        ReportField.LOGCAT,
        ReportField.INSTALLATION_ID,
        ReportField.DEVICE_FEATURES,
        ReportField.ENVIRONMENT,
        ReportField.SHARED_PREFERENCES,
        ReportField.SETTINGS_SYSTEM,
        ReportField.SETTINGS_SECURE
    },
    logcatArguments = { "-t", "300", "-v", "time" })
//@formatter:on
public class Application extends android.app.Application {
    /**
     * This is highly controversial.
     */
    private static Context sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // Log
        Log.init(Constants.TAG);

        // Crash reporting
        if (BuildConfig.CRASH_REPORT) {
            // ACRA
            try {
                ACRA.init(this);
                ACRAConfiguration config = ACRA.getConfig();
                config.setFormUriBasicAuthLogin(getString(R.string.acra_login));
                config.setFormUriBasicAuthPassword(getString(R.string.acra_password));
            } catch (Throwable t) {
                Log.w("Problem while initializing ACRA", t);
            }

            // AndroidWearCrashReport
            try {
                CrashReport.getInstance(this).setOnCrashListener(new CrashReport.IOnCrashListener() {
                    @Override
                    public void onCrashReceived(CrashInfo crashInfo) {
                        Exception exception = new Exception("Crash on the wearable app " + crashInfo.getManufacturer() + "/" + crashInfo.getModel() + "/" +
                                crashInfo.getProduct() + "/" + crashInfo.getFingerprint(), crashInfo.getThrowable());
                        ACRA.getErrorReporter().handleSilentException(exception);
                    }
                });
            } catch (Throwable t) {
                Log.w("Problem while initializing AndroidWearCrashReport", t);
            }
        }

        // Units
        UnitUtil.readPreferences(this);

        // Connect Google Play Services in wear communication helper
        WearCommHelper.get().connect(this);

        // Strict mode
        if (BuildConfig.STRICT_MODE) setupStrictMode();
    }

    private void setupStrictMode() {
        // Do this in a Handler.post because of this issue: http://code.google.com/p/android/issues/detail?id=35298
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            }
        });
    }

    public static Context getApplication() {
        return sApplication;
    }
}
