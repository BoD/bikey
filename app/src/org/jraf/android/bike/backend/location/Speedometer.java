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
    private static final int LOG_SIZE_SLOW = 3;

    /**
     * Number of entries to keep when going at medium speed.
     */
    private static final int LOG_SIZE_MEDIUM = 4;

    /**
     * Number of entries to keep when going fast.
     */
    private static final int LOG_SIZE_MAX = 5;

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
    private static final float SPEED_MIN_THRESHOLD_M_S = 2.2f / 3.6f;

    public static class DebugInfo {
        public DistanceDuration lastDistanceDuration;
    }

    private Location mLastLocation = null;
    private Deque<DistanceDuration> mLog = new ArrayDeque<DistanceDuration>(LOG_SIZE_MAX);
    private int mActivityType = DetectedActivity.STILL;
    private int mLogSize = LOG_SIZE_SLOW;
    public DebugInfo mDebugInfo = new DebugInfo();

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
        if (mLastLocation != null) {
            DistanceDuration distanceDuration = new DistanceDuration(mLastLocation, location);
            float lastSpeed = distanceDuration.getSpeed();

            if (mLog.size() >= mLogSize) {
                // Make room for the new value
                mLog.removeLast();

                if (mLog.size() >= mLogSize) {
                    // Remove one item to account for the log size which depends on the last measured speed
                    mLog.removeLast();
                }
            }

            mDebugInfo.lastDistanceDuration = distanceDuration;

            if (lastSpeed < SPEED_MIN_THRESHOLD_M_S) {
                Log.d("Speed under threshold: rounding to 0");
                distanceDuration.distance = 0;
            }
            Log.d("Adding speed:" + UnitUtil.formatSpeed(lastSpeed) + "(" + lastSpeed + ")");
            mLog.addFirst(distanceDuration);

            if (lastSpeed < SPEED_MEDIUM_M_S) {
                Log.d("Slow speed: keep " + LOG_SIZE_SLOW + " values");
                mLogSize = LOG_SIZE_SLOW;
            } else if (lastSpeed < SPEED_FAST_M_S) {
                Log.d("Medium speed: keep " + LOG_SIZE_MEDIUM + " values");
                mLogSize = LOG_SIZE_MEDIUM;
            } else {
                Log.d("Fast speed: keep  " + LOG_SIZE_MAX + " values");
                mLogSize = LOG_SIZE_MAX;
            }
        }

        mLastLocation = location;
    }

    public float getSpeed() {
        Log.d("mLog=" + mLog);
        //        if (mActivityType == DetectedActivity.STILL) return 0f;
        float distance = 0f;
        float duration = 0f;
        int count = 0;
        float res = 0;
        for (DistanceDuration distanceDuration : mLog) {
            //            distance += distanceDuration.distance;
            //            duration += distanceDuration.duration;
            res += distanceDuration.getSpeed();
            count++;
        }
        if (count == 0) return 0f;
        //        res = distance / (duration / 1000f);
        res /= count;
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
