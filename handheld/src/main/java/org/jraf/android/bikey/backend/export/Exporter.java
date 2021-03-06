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
package org.jraf.android.bikey.backend.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import org.jraf.android.bikey.app.Application;


public abstract class Exporter {
    private final Context mContext;
    private final Uri mRideUri;
    private OutputStream mOutputStream;
    private final String mExportedFileName;
    private File mExportFile;

    protected Exporter(Uri rideUri) {
        mContext = Application.getApplication();
        mRideUri = rideUri;
        mExportedFileName = getExportedFileName();
    }

    protected abstract String getExportedFileName();

    @WorkerThread
    public abstract void export() throws IOException;

    protected Uri getRideUri() {
        return mRideUri;
    }

    public File getExportFile() {
        if (mExportFile == null) {
            mExportFile = new File(mContext.getExternalFilesDir(null), mExportedFileName);
        }
        return mExportFile;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getString(int resId) {
        return mContext.getString(resId);
    }

    protected String getString(int resId, Object... args) {
        return mContext.getString(resId, args);
    }

    public void setOutputStream(OutputStream outputStream) {
        mOutputStream = outputStream;
    }

    protected OutputStream getOutputStream() throws FileNotFoundException {
        if (mOutputStream == null) {
            mOutputStream = new FileOutputStream(getExportFile());
        }
        return mOutputStream;
    }
}
