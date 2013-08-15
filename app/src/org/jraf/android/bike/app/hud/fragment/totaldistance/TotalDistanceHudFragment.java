package org.jraf.android.bike.app.hud.fragment.totaldistance;

import android.net.Uri;

import org.jraf.android.bike.app.hud.fragment.SimpleHudFragment;
import org.jraf.android.bike.backend.log.LogListener;
import org.jraf.android.bike.backend.log.LogManager;
import org.jraf.android.bike.util.UnitUtil;

public class TotalDistanceHudFragment extends SimpleHudFragment {
    public static TotalDistanceHudFragment newInstance() {
        return new TotalDistanceHudFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Log updates
        LogManager.get().addListener(mLogListener);
    }

    @Override
    public void onStop() {
        // Speed updates
        LogManager.get().removeListener(mLogListener);
        super.onStop();
    }

    private LogListener mLogListener = new LogListener() {
        @Override
        public void onLogAdded(Uri rideUri) {
            if (!rideUri.equals(getRideUri())) return;
            float totalDistance = LogManager.get().getTotalDistance(rideUri);
            mTxtValue.setText(UnitUtil.formatDistance(totalDistance));
        }
    };
}
