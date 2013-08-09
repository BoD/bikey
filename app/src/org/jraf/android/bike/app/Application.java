package org.jraf.android.bike.app;

import android.content.Context;

public class Application extends android.app.Application {
    private static Context sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static Context getApplication() {
        return sApplication;
    }
}
