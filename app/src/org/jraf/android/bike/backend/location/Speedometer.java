package org.jraf.android.bike.backend.location;

import java.util.ArrayDeque;
import java.util.Deque;

import android.location.Location;

import org.jraf.android.bike.backend.location.LocationManager.ActivityRecognitionListener;
import org.jraf.android.bike.util.UnitUtil;
import org.jraf.android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;

/**
 * Keeps a log of distance/duration. The size of the log depends on the last measured speed.
 */
public class Speedometer implements LocationListener, ActivityRecognitionListener {
    /**
     * Number of entries to keep when going slow.
     */
    private static final int LOG_SIZE_SLOW = 2;

    /**
     * Number of entries to keep when going at medium speed.
     */
    private static final int LOG_SIZE_MEDIUM = 8;

    /**
     * Number of entries to keep when going fast.
     */
    private static final int LOG_SIZE_MAX = 15;

    /**
     * Below this speed, we only keep LOG_SIZE_SLOW log entries.
     */
    private static final float SPEED_MEDIUM_M_S = 10f / 3.6f;

    /**
     * Below this speed, we only keep LOG_SIZE_MEDIUM log entries.
     */
    private static final float SPEED_FAST_M_S = 20f / 3.6f;

    /**
     * Speeds below this value will be reported as 0 (because of GPS low precision).
     */
    private static final float SPEED_MIN_THRESHOLD_M_S = 2.5f / 3.6f;

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

        @Override
        public String toString() {
            return "DistanceDuration [distance=" + distance + ", duration=" + duration + ", speed=" + getSpeed() + "]";
        }

    }

    private long mLastDate = 0;
    private Location mLastLocation = null;
    private Deque<DistanceDuration> mLog = new ArrayDeque<DistanceDuration>(LOG_SIZE_MAX);
    private int mActivityType = DetectedActivity.STILL;

    /*
    public Speedometer() {
        for (int i = 0; i < LOG_SIZE_MAX; i++) {
            mLog.addFirst(new DistanceDuration(0, 1000));
        }
    }
    */

    public void startListening() {
        LocationManager.get().addLocationListener(this);
        //        LocationManager.get().addActivityRecognitionListener(this);
    }

    public void stopListening() {
        LocationManager.get().removeLocationListener(this);
        //        LocationManager.get().removeActivityRecognitionListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        long now = System.currentTimeMillis();
        if (mLastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (mLog.size() == LOG_SIZE_MAX) {
                mLog.removeLast();
            }
            DistanceDuration distanceDuration = new DistanceDuration(results[0], now - mLastDate);
            float currentSpeed = distanceDuration.getSpeed();
            Log.d("Adding speed:" + UnitUtil.formatSpeed(currentSpeed) + "(" + currentSpeed + ")");
            mLog.addFirst(distanceDuration);

            if (currentSpeed < SPEED_MEDIUM_M_S) {
                // Low speed: we only keep LOG_SIZE_SLOW values in the log
                Log.d("Slow speed: keep only " + LOG_SIZE_SLOW + " values");
                while (mLog.size() > LOG_SIZE_SLOW) {
                    mLog.removeLast();
                }
            } else if (currentSpeed < SPEED_FAST_M_S) {
                // Medium speed: we only keep LOG_SIZE_MEDIUM values in the log
                Log.d("Medium speed: keep only " + LOG_SIZE_MEDIUM + " values");
                while (mLog.size() > LOG_SIZE_SLOW) {
                    mLog.removeLast();
                }
            }
        }

        mLastDate = now;
        mLastLocation = location;
    }

    public float getSpeed() {
        Log.d("mLog=" + mLog);
        //        if (mActivityType == DetectedActivity.STILL) return 0f;
        float distance = 0f;
        float duration = 0f;
        int count = 0;
        for (DistanceDuration distanceDuration : mLog) {
            distance += distanceDuration.distance;
            duration += distanceDuration.duration;
            count++;
        }
        if (count == 0) return 0f;
        float res = distance / (duration / 1000f);
        Log.d("res=" + res);
        if (res < SPEED_MIN_THRESHOLD_M_S) {
            Log.d("Speed under threshold: return 0");
            return 0f;
        }
        return res;
    }

    @Override
    public void onActivityRecognized(int activityType, int confidence) {
        mActivityType = activityType;
    }
}
