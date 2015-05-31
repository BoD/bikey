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
import org.jraf.android.bikey.backend.provider.log.LogSelection;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.provider.ride.RideSelection;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public class TestBikeyRideImporter extends ProviderTestCase2<BikeyProvider> {

    private ContentResolver mContentResolver;

    public TestBikeyRideImporter() {
        super(BikeyProvider.class, BikeyProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getMockContentResolver();
        new LogSelection().delete(mContentResolver);
        new RideSelection().delete(mContentResolver);
    }


    public void testRideImporter1() throws IOException, ParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("assets/ride1.ride");
        BikeyRideImporter importer = new BikeyRideImporter(is);
        importer.doImport();
        RideSelection selection = new RideSelection();
        RideCursor cursor = selection.name("Papa's Route").query(mContentResolver);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("afa4156d-ea60-4ca3-9afa-8a393455cf00", cursor.getUuid());
        cursor.close();
        assertTrue(cursor.isClosed());
    }
}
