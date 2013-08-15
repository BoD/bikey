package org.jraf.android.bike.app.hud.fragment.elapsedtime;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.fragment.SimpleHudFragment;
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.bike.backend.ride.RideListener;
import org.jraf.android.bike.backend.ride.RideManager;

public class ElapsedTimeHudFragment extends SimpleHudFragment {
    private Chronometer mChronometer;

    public static ElapsedTimeHudFragment newInstance() {
        return new ElapsedTimeHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_elapsed_time;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChronometer = (Chronometer) view.findViewById(R.id.chronometer);
    }

    private void setChronometerDuration(long duration) {
        mChronometer.setBase(SystemClock.elapsedRealtime() - duration);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ride updates
        final RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        final Uri rideUri = getRideUri();

        new AsyncTask<Void, Void, Void>() {
            private long mDuration;
            private boolean mIsActive;
            private long mActivatedDate;

            @Override
            protected Void doInBackground(Void... params) {
                mDuration = rideManager.getDuration(rideUri);
                mIsActive = rideManager.getState(rideUri) == RideState.ACTIVE;
                if (mIsActive) {
                    mActivatedDate = rideManager.getActivatedDate(rideUri);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mIsActive) {
                    long additionalDuration = System.currentTimeMillis() - mActivatedDate;
                    setChronometerDuration(mDuration + additionalDuration);
                    mChronometer.setEnabled(true);
                    mChronometer.start();
                } else {
                    setChronometerDuration(mDuration);
                }
            }
        }.execute();
    }

    @Override
    public void onStop() {
        // Ride updates
        RideManager.get().removeListener(mRideListener);
        super.onStop();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(final Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;

            new AsyncTask<Void, Void, Void>() {
                private long mDuration;

                @Override
                protected Void doInBackground(Void... params) {
                    mDuration = RideManager.get().getDuration(rideUri);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    setChronometerDuration(mDuration);
                    mChronometer.start();
                    mChronometer.setEnabled(true);
                }
            }.execute();
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            mChronometer.stop();
            mChronometer.setEnabled(false);
        }
    };
}
