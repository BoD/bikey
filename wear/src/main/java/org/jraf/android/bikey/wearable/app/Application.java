/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.wearable.app;

import android.os.Handler;
import android.os.StrictMode;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.common.Constants;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.util.log.Log;

import fr.nicolaspomepuy.androidwearcrashreport.wear.CrashReporter;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Log
        Log.init(this, Constants.TAG, false);

        // Crash reporting
        if (BuildConfig.CRASH_REPORT) {
            // AndroidWearCrashReport
            try {
                CrashReporter.getInstance(this).start();
            } catch (Throwable t) {
                Log.w("Problem while initializing AndroidWearCrashReport", t);
            }
        }

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
}
