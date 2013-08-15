package org.jraf.android.bike.app.hud.fragment.totaldistance;

import android.net.Uri;
import android.os.AsyncTask;

import org.jraf.android.bike.app.hud.fragment.SimpleHudFragment;
import org.jraf.android.bike.backend.log.LogListener;
import org.jraf.android.bike.backend.log.LogManager;
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.bike.backend.ride.RideListener;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.bike.util.UnitUtil;

public class TotalDistanceHudFragment extends SimpleHudFragment {
    public static TotalDistanceHudFragment newInstance() {
        return new TotalDistanceHudFragment();
    }

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
            private float mTotalDistance;
            private boolean mIsActive;

            @Override
            protected Void doInBackground(Void... params) {
                mTotalDistance = logManager.getTotalDistance(rideUri);
                mIsActive = rideManager.getState(rideUri) == RideState.ACTIVE;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mTxtValue.setEnabled(mIsActive);
                mTxtValue.setText(UnitUtil.formatDistance(mTotalDistance));
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
            mTxtValue.setEnabled(true);
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            mTxtValue.setEnabled(false);
        }
    };

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(final Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;

            new AsyncTask<Void, Void, Void>() {
                private float mTotalDistance;

                @Override
                protected Void doInBackground(Void... params) {
                    mTotalDistance = LogManager.get().getTotalDistance(rideUri);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    mTxtValue.setText(UnitUtil.formatDistance(mTotalDistance));
                }
            }.execute();
        }
    };
}
