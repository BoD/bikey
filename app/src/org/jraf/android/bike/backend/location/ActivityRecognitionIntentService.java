package org.jraf.android.bike.backend.location;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import org.jraf.android.bike.Constants;
import org.jraf.android.util.string.StringUtil;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionIntentService extends IntentService {
    private static final String TAG = Constants.TAG + ActivityRecognitionIntentService.class.getSimpleName();
    private Handler mHandler = new Handler();

    public ActivityRecognitionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "intent=" + StringUtil.toString(intent));
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            final int activityType = mostProbableActivity.getType();
            final int confidence = mostProbableActivity.getConfidence();
            // Dispatching is done on the main/ui thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LocationManager.get().onActivityRecognized(activityType, confidence);
                }
            });
        }
    }
}
