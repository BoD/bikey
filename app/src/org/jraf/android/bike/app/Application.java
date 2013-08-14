package org.jraf.android.bike.app;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;

import org.jraf.android.bike.Config;

public class Application extends android.app.Application {
    private static Context sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
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
