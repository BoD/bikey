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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.util.log.wrapper.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BikeyRideImporter {
    private static final String DOCUMENT_VERSION = "1";
    private final ContentResolver mContentResolver;
    private final InputStream mInputStream;

    private static enum State {
        BIKEY, RIDE, LOG,
    }

    public BikeyRideImporter(ContentResolver contentResolver, InputStream inputStream) {
        mContentResolver = contentResolver;
        mInputStream = inputStream;
    }

    public void doImport() throws IOException, ParseException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(mInputStream, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "bikey");
            String version = parser.getAttributeValue(null, "version");
            if (!DOCUMENT_VERSION.equals(version)) {
                Log.w("Importing from an unsupported format version!  Continuing anyway, but it may fail.");
            }

            State state = State.BIKEY;
            ContentValues rideContentValues = new ContentValues();
            ContentValues logContentValues = null;
            String value;
            int valueType = -1;
            String tagName = null;
            boolean isInValue = false;
            long rideId = -1;
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();

                        switch (tagName) {
                            case "ride":
                                state = State.RIDE;
                                break;

                            case "logs":
                                // We have all the values about the ride: create it now
                                rideId = createRide(rideContentValues);
                                Log.d("rideId=" + rideId);
                                break;

                            case "log":
                                state = State.LOG;
                                // Save the previous log (if any)
                                if (logContentValues != null) createLog(rideId, logContentValues);
                                logContentValues = new ContentValues();
                                break;

                            case "_id":
                            case "ride_id":
                                // Ignore those: autoincrement ids will be used instead
                                break;

                            default:
                                if (state != State.RIDE && state != State.LOG) break;
                                // "Value" tag
                                String typeStr = parser.getAttributeValue(null, "type");
                                valueType = Integer.parseInt(typeStr);
                                // Log.d("type=" + LogUtil.getConstantName(Cursor.class, valueType, "FIELD_TYPE_"));
                                isInValue = true;
                                break;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (isInValue) {
                            value = parser.getText();

                            ContentValues contentValues = null;
                            switch (state) {
                                case RIDE:
                                    contentValues = rideContentValues;
                                    break;
                                case LOG:
                                    contentValues = logContentValues;
                                    break;
                            }

                            switch (valueType) {
                                case Cursor.FIELD_TYPE_NULL:
                                    contentValues.putNull(tagName);
                                    break;

                                case Cursor.FIELD_TYPE_STRING:
                                    contentValues.put(tagName, value);
                                    break;

                                case Cursor.FIELD_TYPE_INTEGER:
                                    contentValues.put(tagName, Long.parseLong(value));
                                    break;

                                case Cursor.FIELD_TYPE_FLOAT:
                                    contentValues.put(tagName, Double.parseDouble(value));
                                    break;
                            }

                        }
                        isInValue = false;
                        break;
                }
            }
            // Save the last log (if any)
            if (logContentValues != null) createLog(rideId, logContentValues);
        } catch (XmlPullParserException e) {
            ParseException parseException = new ParseException("Could not parse xml", 0);
            parseException.initCause(e);
            throw parseException;
        }
    }

    private long createRide(ContentValues rideContentValues) {
        Log.d();
        Uri rideUri = mContentResolver.insert(RideColumns.CONTENT_URI, rideContentValues);
        return ContentUris.parseId(rideUri);
    }

    private void createLog(long rideId, ContentValues logContentValues) {
        logContentValues.put(LogColumns.RIDE_ID, rideId);
        mContentResolver.insert(LogColumns.CONTENT_URI, logContentValues);
    }
}
