package org.jraf.android.bike.app.hud.fragment;

import android.net.Uri;
import android.os.AsyncTask;

import org.jraf.android.bike.backend.log.LogListener;
import org.jraf.android.bike.backend.log.LogManager;
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.bike.backend.ride.RideListener;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.annotation.Background;

public abstract class LogHudFragment extends SimpleHudFragment {
    @Override
    public void onStart() {
        super.onStart();
        // Ride updates
        final RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        // Log updates
        final LogManager logManager = LogManager.get();
        logManager.addListener(mLogListener);

        final Uri rideUri = getRideUri();

        new AsyncTask<Void, Void, Void>() {
            private boolean mIsActive;
            private CharSequence mValue;

            @Override
            protected Void doInBackground(Void... params) {
                mIsActive = rideManager.getState(rideUri) == RideState.ACTIVE;
                mValue = queryValue();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                setTextEnabled(mIsActive);
                setText(mValue);
            }
        }.execute();
    }

    @Override
    public void onStop() {
        // Ride updates
        RideManager.get().removeListener(mRideListener);

        // Log updates
        LogManager.get().removeListener(mLogListener);
        super.onStop();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            setTextEnabled(true);
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            setTextEnabled(false);
        }
    };

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(final Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;

            new AsyncTask<Void, Void, Void>() {
                private CharSequence mValue;

                @Override
                protected Void doInBackground(Void... params) {
                    mValue = queryValue();
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    setText(mValue);
                }
            }.execute();
        }
    };

    @Background
    protected abstract CharSequence queryValue();
}
