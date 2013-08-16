package org.jraf.android.bikey.app.hud;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;


public class FragmentCycler {
    private int mContainerResId;
    private List<String> mFragmentTags = new ArrayList<String>(10);
    private int mCurrentVisibleIndex = 0;

    public FragmentCycler(int containerResId) {
        mContainerResId = containerResId;
    }

    public void add(Activity activity, Fragment fragment) {
        String tag = getTag(fragment);
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment foundFragment = fragmentManager.findFragmentByTag(tag);
        if (foundFragment == null) {
            FragmentTransaction t = fragmentManager.beginTransaction();
            t.add(mContainerResId, fragment, tag);
            t.hide(fragment);
            t.commit();
        } else {
            FragmentTransaction t = fragmentManager.beginTransaction();
            t.hide(foundFragment);
            t.commit();
        }
        mFragmentTags.add(tag);
    }

    public void show(Activity activity) {
        String tag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getFragmentManager();
        fragmentManager.executePendingTransactions();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.show(fragment);
        t.commit();
    }

    public void cycle(Activity activity) {
        int previousVisibleIndex = mCurrentVisibleIndex;
        mCurrentVisibleIndex = (mCurrentVisibleIndex + 1) % mFragmentTags.size();
        String hideTag = mFragmentTags.get(previousVisibleIndex);
        String showTag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment showFragment = fragmentManager.findFragmentByTag(showTag);
        Fragment hideFragment = fragmentManager.findFragmentByTag(hideTag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.hide(hideFragment);
        t.show(showFragment);
        t.commit();
    }

    private String getTag(Fragment fragment) {
        return fragment.getClass().getName();
    }
}
