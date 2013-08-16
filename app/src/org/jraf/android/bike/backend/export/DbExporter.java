package org.jraf.android.bike.backend.export;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

import org.jraf.android.bike.backend.provider.BikeSQLiteOpenHelper;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.file.FileUtil;

public class DbExporter extends Exporter {
    public DbExporter(Uri rideUri) {
        super(rideUri);
    }

    @Override
    @Background
    protected String getExportedFileName() {
        return FileUtil.getValidFileName(RideManager.get().getDisplayName(getRideUri()) + ".db");
    }

    @Override
    public void export() throws IOException {
        File dbFile = getContext().getDatabasePath(BikeSQLiteOpenHelper.DATABASE_NAME);
        FileUtil.copy(dbFile, getExportFile());
    }
}
