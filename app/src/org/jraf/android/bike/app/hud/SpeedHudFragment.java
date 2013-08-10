package org.jraf.android.bike.app.hud;

import android.location.Location;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.location.LocationManager;
import org.jraf.android.bike.backend.location.LocationManager.StatusListener;
import org.jraf.android.bike.backend.location.Speedometer;
import org.jraf.android.bike.util.UnitUtil;

public class SpeedHudFragment extends SimpleHudFragment {
    public static SpeedHudFragment newInstance() {
        return new SpeedHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_speed;
    }

    @Override
    public void onResume() {
        super.onResume();
        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Speed updates
        LocationManager.get().addLocationListener(mSpeedometer);
    }

    @Override
    public void onPause() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Speed updates
        LocationManager.get().removeLocationListener(mSpeedometer);
        super.onPause();
    }

    private Speedometer mSpeedometer = new Speedometer() {
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            setText(UnitUtil.formatSpeed(getSpeed()));
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            mTxtValue.setEnabled(active);
        }
    };
}
