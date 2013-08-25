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
package org.jraf.android.bikey.app;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.jraf.android.bikey.Config;
import org.jraf.android.bikey.R;
import org.jraf.android.util.Log;

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

        // ACRA
        if (Config.ACRA) {
            try {
                ACRA.init(this);
                ACRAConfiguration config = ACRA.getConfig();
                config.setFormUriBasicAuthLogin(getString(R.string.acra_login));
                config.setFormUriBasicAuthPassword(getString(R.string.acra_password));
            } catch (Throwable t) {
                Log.w("Problem while initializing ACRA", t);
            }
        }

        if (Config.STRICT_MODE) setupStrictMode();
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
