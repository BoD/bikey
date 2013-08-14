package org.jraf.android.bike.app.hud.fragment.elapsedtime;

import android.net.Uri;
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
        RideManager rideManager = RideManager.get();
        rideManager.addListener(mRideListener);

        Uri rideUri = getRideUri();
        long duration = rideManager.getDuration(rideUri);
        boolean isActive = rideManager.getState(rideUri) == RideState.ACTIVE;
        if (isActive) {
            long activatedDate = rideManager.getActivatedDate(rideUri);
            long additionalDuration = System.currentTimeMillis() - activatedDate;
            setChronometerDuration(duration + additionalDuration);
            mChronometer.setEnabled(true);
            mChronometer.start();
        } else {
            setChronometerDuration(duration);
        }
    }

    @Override
    public void onStop() {
        RideManager.get().removeListener(mRideListener);
        super.onStop();
    }

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            long duration = RideManager.get().getDuration(rideUri);
            setChronometerDuration(duration);
            mChronometer.start();
            mChronometer.setEnabled(true);
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            mChronometer.stop();
            mChronometer.setEnabled(false);
        }
    };
}
