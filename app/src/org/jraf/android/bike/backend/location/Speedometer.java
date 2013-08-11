package org.jraf.android.bike.backend.location;

import java.util.ArrayDeque;
import java.util.Deque;

import android.location.Location;

import org.jraf.android.bike.backend.location.LocationManager.ActivityRecognitionListener;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;

public class Speedometer implements LocationListener, ActivityRecognitionListener {
    private static final int MAX_VALUES = 5;

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
    private int mActivityType;

    public Speedometer() {
        for (int i = 0; i < MAX_VALUES; i++) {
            mDistanceDurations.addFirst(new DistanceDuration(0, 1000));
        }
    }

    public void startListening() {
        LocationManager.get().addLocationListener(this);
        LocationManager.get().addActivityRecognitionListener(this);
    }

    public void stopListening() {
        LocationManager.get().removeLocationListener(this);
        LocationManager.get().removeLocationListener(this);
    }

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
        if (mActivityType == DetectedActivity.STILL) return 0f;
        float res = 0f;
        int count = 0;
        for (DistanceDuration distanceDuration : mDistanceDurations) {
            res += distanceDuration.getSpeed();
            count++;
        }
        if (count == 0) return 0f;
        return res / count;
    }

    @Override
    public void onActivityRecognized(int activityType, int confidence) {
        mActivityType = activityType;
    }
}
