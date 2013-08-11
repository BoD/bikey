package org.jraf.android.bike.app.hud;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.jraf.android.bike.app.hud.fragment.elapsedtime.ElapsedTimeHudFragment;
import org.jraf.android.bike.app.hud.fragment.speed.SpeedHudFragment;

public class HudViewPagerAdapter extends FragmentPagerAdapter {

    public HudViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SpeedHudFragment.newInstance();

            case 1:
                return ElapsedTimeHudFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

}
