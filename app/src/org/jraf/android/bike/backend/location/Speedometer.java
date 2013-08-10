package org.jraf.android.bike.backend.location;

import java.util.ArrayDeque;
import java.util.Deque;

import android.location.Location;

import com.google.android.gms.location.LocationListener;

public class Speedometer implements LocationListener {
    private static final int MAX_VALUES = 10;

    private static class DistanceDuration {
        public float distance;
        public long duration;

        public DistanceDuration(float distance, long duration) {
            this.distance = distance;
            this.duration = duration;
        }

        public float getSpeed() {
            return distance / (duration / 1000f);
        }
    }

    private long mLastDate = 0;
    private Location mLastLocation = null;
    private Deque<DistanceDuration> mDistanceDurations = new ArrayDeque<DistanceDuration>(MAX_VALUES);


    @Override
    public void onLocationChanged(Location location) {
        long now = System.currentTimeMillis();
        if (mLastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (mDistanceDurations.size() == MAX_VALUES) {
                mDistanceDurations.removeLast();
            }
            mDistanceDurations.addFirst(new DistanceDuration(results[0], now - mLastDate));
        }

        mLastDate = now;
        mLastLocation = location;
    }

    public float getSpeed() {
        float res = 0f;
        int count = 0;
        for (DistanceDuration distanceDuration : mDistanceDurations) {
            res += distanceDuration.getSpeed();
            count++;
        }
        if (count == 0) return 0f;
        return res / count;
    }
}
