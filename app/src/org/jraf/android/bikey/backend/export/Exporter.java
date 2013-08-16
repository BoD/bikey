package org.jraf.android.bikey.backend.export;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.net.Uri;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.annotation.Background;


public abstract class Exporter {
    private Context mContext;
    private Uri mRideUri;

    protected Exporter(Uri rideUri) {
        mContext = Application.getApplication();
        mRideUri = rideUri;
    }

    protected abstract String getExportedFileName();

    @Background
    public abstract void export() throws IOException;

    public Uri getRideUri() {
        return mRideUri;
    }

    public File getExportFile() {
        return new File(mContext.getExternalFilesDir(null), getExportedFileName());
    }

    public Context getContext() {
        return mContext;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, Object... args) {
        return mContext.getString(resId, args);
    }
}
