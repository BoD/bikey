package org.jraf.android.bike.app.hud.fragment.elapsedtime;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.fragment.SimpleHudFragment;

public class ElapsedTimeHudFragment extends SimpleHudFragment {
    public static ElapsedTimeHudFragment newInstance() {
        return new ElapsedTimeHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_elapsed_time;
    }


}
