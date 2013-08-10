package org.jraf.android.bike.app;

import android.content.Context;
import android.os.StrictMode;

public class Application extends android.app.Application {
    private static Context sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    public static Context getApplication() {
        return sApplication;
    }
}
