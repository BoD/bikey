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