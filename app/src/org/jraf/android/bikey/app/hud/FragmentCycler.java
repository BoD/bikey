package org.jraf.android.bikey.app.hud;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;


public class FragmentCycler {
    private int mContainerResId;
    private List<String> mFragmentTags = new ArrayList<String>(10);
    private List<Checkable> mTabs = new ArrayList<Checkable>(10);
    private int mCurrentVisibleIndex = 0;

    public FragmentCycler(int containerResId) {
        mContainerResId = containerResId;
    }

    public void add(Activity activity, Fragment fragment, int tabResId) {
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
        Checkable tab = (Checkable) activity.findViewById(tabResId);
        mTabs.add(tab);
        ((View) tab).setOnClickListener(mTabOnClickListener);
    }

    public void show(Activity activity) {
        String tag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getFragmentManager();
        fragmentManager.executePendingTransactions();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.show(fragment);
        t.commit();
        mTabs.get(mCurrentVisibleIndex).setChecked(true);
    }

    public void cycle(Activity activity) {
        int newIndex = (mCurrentVisibleIndex + 1) % mFragmentTags.size();
        setCurrentVisibleIndex(activity, newIndex);
    }

    private void setCurrentVisibleIndex(Activity activity, int newIndex) {
        int previousVisibleIndex = mCurrentVisibleIndex;
        mCurrentVisibleIndex = newIndex;
        String hideTag = mFragmentTags.get(previousVisibleIndex);
        String showTag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment showFragment = fragmentManager.findFragmentByTag(showTag);
        Fragment hideFragment = fragmentManager.findFragmentByTag(hideTag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.hide(hideFragment);
        t.show(showFragment);
        t.commit();
        mTabs.get(previousVisibleIndex).setChecked(false);
        mTabs.get(mCurrentVisibleIndex).setChecked(true);
    }

    private String getTag(Fragment fragment) {
        return fragment.getClass().getName();
    }

    private OnClickListener mTabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!(v instanceof Checkable)) return;
            Checkable checkable = (Checkable) v;
            if (!checkable.isChecked()) checkable.setChecked(true);
            int newIndex = mTabs.indexOf(checkable);
            if (mCurrentVisibleIndex == newIndex) return;
            setCurrentVisibleIndex((Activity) v.getContext(), newIndex);
        }
    };

}
