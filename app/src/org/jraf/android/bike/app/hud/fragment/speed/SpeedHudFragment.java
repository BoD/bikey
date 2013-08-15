package org.jraf.android.bike.app.hud.fragment.speed;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.fragment.SimpleHudFragment;
import org.jraf.android.bike.backend.location.LocationManager;
import org.jraf.android.bike.backend.location.LocationManager.StatusListener;
import org.jraf.android.bike.backend.location.Speedometer;
import org.jraf.android.bike.util.UnitUtil;

public class SpeedHudFragment extends SimpleHudFragment {
    private TextView mTxtDebugLastSpeed;

    public static SpeedHudFragment newInstance() {
        return new SpeedHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_speed;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTxtDebugLastSpeed = (TextView) view.findViewById(R.id.txtDebugLastSpeed);
    }

    @Override
    public void onStart() {
        super.onStart();
        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Speed updates
        mSpeedometer.startListening();
    }

    @Override
    public void onStop() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Speed updates
        mSpeedometer.stopListening();
        super.onStop();
    }

    private Speedometer mSpeedometer = new Speedometer() {
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            setText(UnitUtil.formatSpeed(getSpeed()));
            if (mDebugInfo.lastDistanceDuration != null) mTxtDebugLastSpeed.setText(UnitUtil.formatSpeed(mDebugInfo.lastDistanceDuration.getSpeed()));
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            mTxtValue.setEnabled(active);
        }
    };
}
