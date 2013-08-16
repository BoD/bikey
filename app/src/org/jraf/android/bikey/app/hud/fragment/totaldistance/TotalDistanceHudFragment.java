package org.jraf.android.bikey.app.hud.fragment.totaldistance;

import org.jraf.android.bikey.app.hud.fragment.LogHudFragment;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.util.UnitUtil;

public class TotalDistanceHudFragment extends LogHudFragment {
    public static TotalDistanceHudFragment newInstance() {
        return new TotalDistanceHudFragment();
    }

    @Override
    protected CharSequence queryValue() {
        return UnitUtil.formatDistance(LogManager.get().getTotalDistance(getRideUri()));
    }
}
