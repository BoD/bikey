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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;

import org.jraf.android.bikey.backend.provider.BikeyProvider;
import org.jraf.android.bikey.backend.provider.TestBikeyProvider;
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.provider.ride.RideSelection;
import org.jraf.android.bikey.backend.provider.ride.RideState;

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
        RideSelection selection = new RideSelection();
        RideCursor cursor = selection.query(mContentResolver);

        // Verify that the ride was created
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        // Verify the attributes of the ride
        assertTrue(cursor.getId() > 0);
        assertEquals(RideState.PAUSED, cursor.getState());
        assertEquals("4245b4dc-ee6b-4e77-a659-fd3987edb5ee", cursor.getUuid());
        assertEquals("Papa's Route", cursor.getName());
        Date activatedDate = cursor.getActivatedDate();
        assertNotNull(activatedDate);
        assertEquals(0, activatedDate.getTime());
        Date createdDate = cursor.getCreatedDate();
        assertNotNull(createdDate);
        assertEquals(1391038068267l, createdDate.getTime());
        assertEquals(14518.9f, cursor.getDistance());
        assertEquals(3905722, cursor.getDuration());
        Date firstActivatedDate = cursor.getFirstActivatedDate();
        assertNull(firstActivatedDate);

        // Cleanup
        cursor.close();
        assertTrue(cursor.isClosed());
    }
}
