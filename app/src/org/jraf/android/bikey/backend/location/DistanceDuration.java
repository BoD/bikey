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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.backend.location;

import android.location.Location;

public class DistanceDuration {
    public float distance;
    public long duration;

    public DistanceDuration(Location previousLocation, Location newLocation) {
        float[] distanceResults = new float[1];
        Location.distanceBetween(previousLocation.getLatitude(), previousLocation.getLongitude(), newLocation.getLatitude(), newLocation.getLongitude(),
                distanceResults);
        distance = distanceResults[0];
        duration = newLocation.getTime() - previousLocation.getTime();
    }

    public float getSpeed() {
        return distance / (duration / 1000f);
    }

    @Override
    public String toString() {
        return String.valueOf(getSpeed());
    }
}
