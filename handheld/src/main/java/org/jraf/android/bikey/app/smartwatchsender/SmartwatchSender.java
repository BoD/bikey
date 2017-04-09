package org.jraf.android.bikey.app.smartwatchsender;

import android.content.Context;
import android.net.Uri;

import org.jraf.android.bikey.backend.location.Speedometer;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.log.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;

/**
 * Abstract class to send values to smartwatches.
 */
public abstract class SmartwatchSender {
    protected static long SEND_VALUES_RATE_S = 3;

    protected Uri mActiveRideUri;
    protected long mInitialDuration;
    protected long mActivatedDate;
    protected ScheduledExecutorService mScheduledExecutorService;
    protected Speedometer mSpeedometer = new Speedometer();

    public void startSending(Context context) {
        Log.d();

        // Ride updates
        RideManager.get().addListener(mRideListener);

        // Speed updates
        mSpeedometer.startListening();
    }

    public void stopSending() {
        Log.d();

        // Ride updates
        RideManager.get().removeListener(mRideListener);

        // Speed updates
        mSpeedometer.stopListening();

        // Stop the scheduled task
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(final Uri rideUri) {
            Log.d();
            mActiveRideUri = rideUri;
            Schedulers.io().scheduleDirect(() -> {
                        mInitialDuration = RideManager.get().getDuration(rideUri);
                        mActivatedDate = RideManager.get().getActivatedDate(rideUri).getTime();

                        // Start the scheduled task now
                        if (mScheduledExecutorService == null) {
                            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
                        }
                        mScheduledExecutorService.scheduleAtFixedRate(mSendValueRunnable, 0, SEND_VALUES_RATE_S, TimeUnit.SECONDS);
                    }
            );
        }

        @Override
        public void onPaused(Uri rideUri) {
            mActiveRideUri = null;
        }
    };


    private Runnable mSendValueRunnable = new Runnable() {
        @Override
        public void run() {
            if (mActiveRideUri == null) return;
            sendValues();
        }
    };

    protected abstract void sendValues();
}
