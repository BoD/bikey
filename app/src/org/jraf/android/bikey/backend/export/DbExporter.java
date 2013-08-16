package org.jraf.android.bikey.backend.export;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

import org.jraf.android.bikey.backend.provider.BikeySQLiteOpenHelper;
import org.jraf.android.bikey.backend.ride.RideManager;
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
        File dbFile = getContext().getDatabasePath(BikeySQLiteOpenHelper.DATABASE_NAME);
        FileUtil.copy(dbFile, getExportFile());
    }
}
