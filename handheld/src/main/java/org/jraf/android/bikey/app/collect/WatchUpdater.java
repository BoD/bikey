package org.jraf.android.bikey.app.collect;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.jraf.android.bikey.backend.location.Speedometer;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.log.wrapper.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by geoffrey on 11/08/14.
 */
public abstract class WatchUpdater {
    protected static long SEND_VALUES_RATE_S = 3;
    protected Uri mActiveRideUri;
    protected long mInitialDuration;
    protected long mActivatedDate;
    protected ScheduledExecutorService mScheduledExecutorService;
    protected Speedometer mSpeedometer = new Speedometer();
    protected Runnable mSendValueRunnable;
    protected RideListener mRideListener;

    public void startUpdates(Context context) {
        initUpdater();
        initListener();
        Log.d();

        // Ride updates
        RideManager.get().addListener(mRideListener);

        // Speed updates
        mSpeedometer.startListening();
    }

    public void stopUpdates() {
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

    protected void initListener(){
        mRideListener = new RideListener() {
            @Override
            public void onActivated(final Uri rideUri) {
                Log.d();
                mActiveRideUri = rideUri;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        mInitialDuration = RideManager.get().getDuration(rideUri);
                        mActivatedDate = RideManager.get().getActivatedDate(rideUri).getTime();

                        // Start the scheduled task now
                        if (mScheduledExecutorService == null) {
                            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
                        }
                        mScheduledExecutorService.scheduleAtFixedRate(mSendValueRunnable, 0, SEND_VALUES_RATE_S, TimeUnit.SECONDS);

                        return null;
                    }
                }.execute();
            }

            @Override
            public void onPaused(Uri rideUri) {
                mActiveRideUri = null;
            }
        };
    }

    protected abstract void initUpdater();
}
