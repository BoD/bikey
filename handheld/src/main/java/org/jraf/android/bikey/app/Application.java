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

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.util.log.wrapper.Log;

import com.crashlytics.android.Crashlytics;

import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashInfo;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashReport;
import io.fabric.sdk.android.Fabric;

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
            // Crashlytics
            try {
                Fabric.with(this, new Crashlytics());
            } catch (Throwable t) {
                Log.w("Problem while initializing Crashlytics", t);
            }

            // AndroidWearCrashReport
            try {
                CrashReport.getInstance(this).setOnCrashListener(new CrashReport.IOnCrashListener() {
                    @Override
                    public void onCrashReceived(CrashInfo crashInfo) {
                        Exception exception = new Exception("Crash on the wearable app " + crashInfo.getManufacturer() + "/" + crashInfo.getModel() + "/" +
                                crashInfo.getProduct() + "/" + crashInfo.getFingerprint(), crashInfo.getThrowable());
                        Crashlytics.logException(exception);
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
                // Do not detect disk reads/writes because it seems it causes bugs in Google Maps (?!)
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().detectNetwork().penaltyLog().build());
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            }
        });
    }

    public static Context getApplication() {
        return sApplication;
    }
}
