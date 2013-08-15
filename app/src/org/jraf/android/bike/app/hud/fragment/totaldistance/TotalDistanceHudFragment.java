package org.jraf.android.bike.app.hud.fragment.totaldistance;

import android.net.Uri;

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
        RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        // Log updates
        LogManager.get().addListener(mLogListener);

        Uri rideUri = getRideUri();
        float totalDistance = LogManager.get().getTotalDistance(rideUri);
        boolean isActive = rideManager.getState(rideUri) == RideState.ACTIVE;
        mTxtValue.setEnabled(isActive);
        mTxtValue.setText(UnitUtil.formatDistance(totalDistance));
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
        public void onLogAdded(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            float totalDistance = LogManager.get().getTotalDistance(rideUri);
            mTxtValue.setText(UnitUtil.formatDistance(totalDistance));
        }
    };
}
