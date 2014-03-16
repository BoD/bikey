/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.app.hud;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.util.handler.HandlerUtil;


public class FragmentCycler {
    private int mContainerResId;
    private List<String> mFragmentTags = new ArrayList<String>(10);
    private List<Checkable> mTabs = new ArrayList<Checkable>(10);
    private List<Integer> mTitles = new ArrayList<Integer>(10);
    private int mCurrentVisibleIndex = 0;
    private TextView mTxtTitle;

    public FragmentCycler(int containerResId, TextView txtTitle) {
        mContainerResId = containerResId;
        mTxtTitle = txtTitle;
    }

    public void add(FragmentActivity activity, Fragment fragment, int tabResId, int titleResId) {
        String tag = getTag(fragment);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
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
        View tab = activity.findViewById(tabResId);
        mTabs.add((Checkable) tab);
        tab.setOnClickListener(mTabOnClickListener);
        mTitles.add(titleResId);
    }

    public void show(FragmentActivity activity) {
        String tag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.show(fragment);
        t.commit();
        mTabs.get(mCurrentVisibleIndex).setChecked(true);
        updateTitle();
    }

    public void cycle(FragmentActivity activity) {
        int newIndex = (mCurrentVisibleIndex + 1) % mFragmentTags.size();
        setCurrentVisibleIndex(activity, newIndex);
    }

    private void setCurrentVisibleIndex(FragmentActivity activity, int newIndex) {
        int previousVisibleIndex = mCurrentVisibleIndex;
        mCurrentVisibleIndex = newIndex;
        String hideTag = mFragmentTags.get(previousVisibleIndex);
        String showTag = mFragmentTags.get(mCurrentVisibleIndex);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment showFragment = fragmentManager.findFragmentByTag(showTag);
        Fragment hideFragment = fragmentManager.findFragmentByTag(hideTag);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.hide(hideFragment);
        t.show(showFragment);
        t.commit();
        mTabs.get(previousVisibleIndex).setChecked(false);
        mTabs.get(mCurrentVisibleIndex).setChecked(true);
        updateTitle();
    }

    private void updateTitle() {
        int duration = mTxtTitle.getResources().getInteger(R.integer.animation_controls_showHide);
        HandlerUtil.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTxtTitle.setText(mTitles.get(mCurrentVisibleIndex));
            }
        }, duration);
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
            setCurrentVisibleIndex((FragmentActivity) v.getContext(), newIndex);
        }
    };

    public int getCurrentVisibleIndex() {
        return mCurrentVisibleIndex;
    }

    public void setCurrentVisibleIndex(int currentVisibleIndex) {
        mCurrentVisibleIndex = currentVisibleIndex;
    }
}
