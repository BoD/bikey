package org.jraf.android.bike.app.hud.fragment.totaldistance;

import org.jraf.android.bike.app.hud.fragment.LogHudFragment;
import org.jraf.android.bike.backend.log.LogManager;
import org.jraf.android.bike.util.UnitUtil;

public class TotalDistanceHudFragment extends LogHudFragment {
    public static TotalDistanceHudFragment newInstance() {
        return new TotalDistanceHudFragment();
    }

    @Override
    protected CharSequence queryValue() {
        return UnitUtil.formatDistance(LogManager.get().getTotalDistance(getRideUri()));
    }
}
