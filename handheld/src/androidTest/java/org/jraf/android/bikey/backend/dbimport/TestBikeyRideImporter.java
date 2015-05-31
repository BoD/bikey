/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Carmen Alvarez (c@rmen.ca)
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

import android.content.ContentResolver;
import android.test.ProviderTestCase2;

import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.TestBikeyProvider;
import org.jraf.android.bikey.backend.provider.log.LogCursor;
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.provider.ride.RideSelection;
import org.jraf.android.bikey.backend.provider.ride.RideState;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

public class TestBikeyRideImporter extends ProviderTestCase2<TestBikeyProvider> {

    private ContentResolver mContentResolver;

    public TestBikeyRideImporter() {
        super(TestBikeyProvider.class, BikeyProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getMockContentResolver();
        new LogSelection().delete(mContentResolver);
        new RideSelection().delete(mContentResolver);
    }


    public void testRideImporterShortRide() throws IOException, ParseException {
        // Import the file
        InputStream is = getClass().getClassLoader().getResourceAsStream("assets/ride-short.ride");
        BikeyRideImporter importer = new BikeyRideImporter(mContentResolver, is);
        importer.doImport();
        RideSelection rideSelection = new RideSelection();
        rideSelection.name("Papa's Route");
        RideCursor rideCursor = rideSelection.query(mContentResolver);

        // Verify that the ride was created
        assertNotNull(rideCursor);
        assertEquals(1, rideCursor.getCount());
        rideCursor.moveToFirst();

        // Verify the attributes of the ride
        assertRideData(rideCursor,
                "Papa's Route", // name
                1391038068267l, // created date
                RideState.PAUSED,
                null, // first activated date
                0l, // activated date
                3905722, // duration
                14518.9f); // distance

        long rideId = rideCursor.getId();
        // Cleanup ride cursor
        rideCursor.close();
        assertTrue(rideCursor.isClosed());

        // Verify that the ride has logs
        LogSelection logSelection = new LogSelection();
        logSelection.rideName("Papa's Route");
        LogCursor logCursor = logSelection.query(mContentResolver);
        assertNotNull(logCursor);
        assertEquals(5, logCursor.getCount());

        // Verify a couple of the log rows
        logCursor.moveToFirst();
        assertLogData(logCursor, rideId,
                1391038162087l, // recorded date
                34.1239, // latitude
                -117.879, // longitude
                157.8, // elevation
                null, // log duration
                null, // log distance
                null, // speed
                null, // cadence
                null); // heart rate

        logCursor.move(2);
        assertLogData(logCursor, rideId,
                1391038163994l, // recorded date
                34.1239, // latitude
                -117.879, // longitude
                156.6, // elevation
                787l, // log duration
                1.40527f, // log distance
                1.78561f, // speed
                null, // cadence
                null); // heart rate

        // Cleanup log cursor
        logCursor.close();
        assertTrue(logCursor.isClosed());
    }

    public void testRideImporterCadence() throws IOException, ParseException {
        // Import the file
        InputStream is = getClass().getClassLoader().getResourceAsStream("assets/ride-cadence.ride");
        BikeyRideImporter importer = new BikeyRideImporter(mContentResolver, is);
        importer.doImport();
        RideSelection rideSelection = new RideSelection();
        rideSelection.createdDate(1396219692235l);
        RideCursor rideCursor = rideSelection.query(mContentResolver);
        assertNotNull(rideCursor);
        assertEquals(1, rideCursor.getCount());
        rideCursor.moveToFirst();

        // Verify the attributes of the ride
        assertRideData(rideCursor,
                null, // name
                1396219692235l, // created date
                RideState.PAUSED,
                null, // first activated date
                0l, // activated date
                1192587, // duration
                3547.17f); // distance

        long rideId = rideCursor.getId();
        // Cleanup ride cursor
        rideCursor.close();
        assertTrue(rideCursor.isClosed());

        // Verify that the ride has logs
        LogSelection logSelection = new LogSelection();
        logSelection.rideCreatedDate(1396219692235l);
        LogCursor logCursor = logSelection.query(mContentResolver);
        assertNotNull(logCursor);
        assertEquals(268, logCursor.getCount());

        // Verify a couple of the log rows
        logCursor.moveToFirst();
        assertLogData(logCursor, rideId,
                1396219716435l, // recorded date
                48.8539, // latitude
                2.28887, // longitude
                65.0, // elevation
                null, // log duration
                null, // log distance
                null, // speed
                49.7035f, // cadence
                null); // heart rate

        logCursor.move(1);
        assertLogData(logCursor, rideId,
                1396219719393l, // recorded date
                48.8538, // latitude
                2.28876, // longitude
                65.0, // elevation
                2958l, // log duration
                11.6525f, // log distance
                3.93931f, // speed
                50.9672f, // cadence
                null); // heart rate

        // Cleanup log cursor
        logCursor.close();
        assertTrue(logCursor.isClosed());
}

    private void assertRideData(RideCursor cursor, String name, Long createdDate, RideState state, Long firstActivatedDate, Long activatedDate, long duration, float distance) {
        assertTrue(cursor.getId() > 0);
        assertEquals(name, cursor.getName());
        assertDate(createdDate, cursor.getCreatedDate());
        assertEquals(state, cursor.getState());
        assertDate(firstActivatedDate, cursor.getFirstActivatedDate());
        assertDate(activatedDate, cursor.getActivatedDate());
        assertEquals(duration, cursor.getDuration());
        assertEquals(distance, cursor.getDistance());
    }

    private void assertLogData(LogCursor cursor, long rideId, Long recordedDate, double latitude, double longitude, double elevation, Long logDuration, Float logDistance, Float speed, Float cadence, Integer heartRate) {
        assertTrue(cursor.getId() > 0);
        assertEquals(rideId, cursor.getRideId());
        assertDate(recordedDate, cursor.getRecordedDate());
        assertEquals(latitude, cursor.getLat());
        assertEquals(longitude, cursor.getLon());
        assertEquals(elevation, cursor.getEle());
        assertEquals(logDuration, cursor.getLogDuration());
        assertEquals(logDistance, cursor.getLogDistance());
        assertEquals(speed, cursor.getSpeed());
        assertEquals(cadence, cursor.getCadence());
        assertEquals(heartRate, cursor.getHeartRate());
    }

    private void assertDate(Long expected, Date actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertEquals(expected.longValue(), actual.getTime());
        }
    }
}
