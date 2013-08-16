package org.jraf.android.bike.backend.export;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.net.Uri;

import org.jraf.android.bike.app.Application;
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
}
