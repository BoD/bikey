/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Carmen Alvarez (c@rmen.ca)
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
package org.jraf.android.bikey.backend.dbimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;

import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;

/**
 * Replace the contents of the current database with the contents of another database.
 * This is based on DBImport from the scrum chatter project.
 */
public class DatabaseImporter {

    /**
     * Replace the database of our app with the contents of the database found at the given uri.
     */
    public static void importDatabase(Context context, Uri uri) throws RemoteException, OperationApplicationException, IOException {
        Log.d();
        if (uri.getScheme().equals("file")) {
            File db = new File(uri.getPath());
            importDatabase(context, db);
        } else {
            InputStream is = context.getContentResolver().openInputStream(uri);
            File tempDb = FileUtil.newTemporaryFile(context, ".db");
            FileOutputStream os = new FileOutputStream(tempDb);
            if (IoUtil.copy(is, os) > 0) {
                importDatabase(context, tempDb);
                tempDb.delete();
            }
        }
    }

    /**
     * In a single database transaction, delete all the cells from the current database, read the data from the given importDb file, create a batch of
     * corresponding insert operations, and execute the inserts.
     */
    private static void importDatabase(Context context, File importDb) throws RemoteException, OperationApplicationException {
        Log.d();
        SQLiteDatabase dbImport = SQLiteDatabase.openDatabase(importDb.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newDelete(LogColumns.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(RideColumns.CONTENT_URI).build());
        Uri insertUri = new Uri.Builder().authority(BikeyProvider.AUTHORITY).appendPath(RideColumns.TABLE_NAME)
                .appendQueryParameter(BikeyProvider.QUERY_NOTIFY, "false").build();
        buildInsertOperations(dbImport, insertUri, RideColumns.TABLE_NAME, operations);
        insertUri = new Uri.Builder().authority(BikeyProvider.AUTHORITY).appendPath(LogColumns.TABLE_NAME)
                .appendQueryParameter(BikeyProvider.QUERY_NOTIFY, "false").build();
        buildInsertOperations(dbImport, insertUri, LogColumns.TABLE_NAME, operations);
        context.getContentResolver().applyBatch(BikeyProvider.AUTHORITY, operations);
        dbImport.close();
    }

    /**
     * Read all cells from the given table from the dbImport database, and add corresponding insert operations to the operations parameter.
     */
    private static void buildInsertOperations(SQLiteDatabase dbImport, Uri uri, String table, ArrayList<ContentProviderOperation> operations) {
        Log.d();
        Cursor c = dbImport.query(false, table, null, null, null, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int columnCount = c.getColumnCount();
                    do {
                        Builder builder = ContentProviderOperation.newInsert(uri);
                        for (int i = 0; i < columnCount; i++) {
                            String columnName = c.getColumnName(i);
                            Object value = c.getString(i);
                            builder.withValue(columnName, value);
                        }
                        operations.add(builder.build());
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }
    }
}
