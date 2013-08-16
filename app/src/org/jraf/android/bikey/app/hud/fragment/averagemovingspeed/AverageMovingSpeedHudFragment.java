package org.jraf.android.bikey.app.hud.fragment.averagemovingspeed;

import org.jraf.android.bikey.app.hud.fragment.LogHudFragment;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.util.UnitUtil;

public class AverageMovingSpeedHudFragment extends LogHudFragment {
    public static AverageMovingSpeedHudFragment newInstance() {
        return new AverageMovingSpeedHudFragment();
    }

    @Override
    protected CharSequence queryValue() {
        return UnitUtil.formatSpeed(LogManager.get().getAverageMovingSpeed(getRideUri()));
    }
}
