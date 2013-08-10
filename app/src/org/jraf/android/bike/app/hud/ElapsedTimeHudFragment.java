package org.jraf.android.bike.app.hud;

import org.jraf.android.bike.R;

public class ElapsedTimeHudFragment extends SimpleHudFragment {
    public static ElapsedTimeHudFragment newInstance() {
        return new ElapsedTimeHudFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.hud_elapsed_time;
    }


}
