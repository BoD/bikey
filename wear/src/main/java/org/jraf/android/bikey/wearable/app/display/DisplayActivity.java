/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.wearable.app.display;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.common.wear.CommConstants;
import org.jraf.android.bikey.common.wear.WearCommHelper;
import org.jraf.android.bikey.common.widget.fragmentcycler.FragmentCycler;
import org.jraf.android.bikey.databinding.DisplayBinding;
import org.jraf.android.bikey.wearable.app.display.fragment.currenttime.CurrentTimeDisplayFragment;
import org.jraf.android.bikey.wearable.app.display.fragment.elapsedtime.ElapsedTimeDisplayFragment;
import org.jraf.android.bikey.wearable.app.display.fragment.speed.SpeedDisplayFragment;
import org.jraf.android.bikey.wearable.app.display.fragment.totaldistance.TotalDistanceDisplayFragment;
import org.jraf.android.util.log.Log;

public class DisplayActivity extends FragmentActivity {
    private DisplayBinding mBinding;
    private FragmentCycler mFragmentCycler;
    private SpeedDisplayFragment mSpeedDisplayFragment;
    private ElapsedTimeDisplayFragment mElapsedTimeDisplayFragment;
    private TotalDistanceDisplayFragment mTotalDistanceDisplayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding = DataBindingUtil.setContentView(this, R.layout.display);
        mBinding.vieFragmentCycle.setOnTouchListener(this::fragmentCycleOnTouch);
        setupFragments();
        retrieveRideValues();
    }

    private void retrieveRideValues() {
        // Retrieve the latest values now, to show the elapsed time
        new AsyncTask<Void, Void, Bundle>() {
            @Override
            protected Bundle doInBackground(Void... params) {
                return WearCommHelper.get().retrieveRideValues();
            }

            @Override
            protected void onPostExecute(Bundle rideValues) {
                float rideDistance = rideValues.getFloat(CommConstants.EXTRA_DISTANCE);
                float rideSpeed = rideValues.getFloat(CommConstants.EXTRA_SPEED);
                long rideStartDateOffset = rideValues.getLong(CommConstants.EXTRA_START_DATE_OFFSET);

                mSpeedDisplayFragment.setSpeed(rideSpeed);
                mElapsedTimeDisplayFragment.setStartDateOffset(rideStartDateOffset);
                mTotalDistanceDisplayFragment.setTotalDistance(rideDistance);
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WearCommHelper.get().addDataApiListener(mDataListener);
    }

    @Override
    protected void onStop() {
        WearCommHelper.get().removeDataApiListener(mDataListener);
        super.onStop();
    }

    private void setupFragments() {
        mSpeedDisplayFragment = SpeedDisplayFragment.newInstance();
        mElapsedTimeDisplayFragment = ElapsedTimeDisplayFragment.newInstance();
        mTotalDistanceDisplayFragment = TotalDistanceDisplayFragment.newInstance();
        CurrentTimeDisplayFragment currentTimeDisplayFragment = CurrentTimeDisplayFragment.newInstance();

        long updateTitleDelay = 0;
        int tabColorEnabled = 0;
        int tabColorDisabled = 0;
        mFragmentCycler = new FragmentCycler(R.id.conFragments, mBinding.txtTitle, updateTitleDelay, tabColorEnabled, tabColorDisabled);
        mFragmentCycler.add(this, mSpeedDisplayFragment, 0, R.string.display_title_speed);
        mFragmentCycler.add(this, mElapsedTimeDisplayFragment, 0, R.string.display_title_duration);
        mFragmentCycler.add(this, mTotalDistanceDisplayFragment, 0, R.string.display_title_distance);
        mFragmentCycler.add(this, currentTimeDisplayFragment, 0, R.string.display_title_currentTime);

        mFragmentCycler.show(this);
    }

    protected boolean fragmentCycleOnTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mFragmentCycler.cycle(this);
        }
        return false;
    }

    private DataApi.DataListener mDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.d("count=" + dataEvents.getCount());

            for (DataEvent dataEvent : dataEvents) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                Log.d("uri=" + uri);
                String path = uri.getPath();
                Log.d("path=" + path);
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap dataMap = dataMapItem.getDataMap();

                switch (path) {
                    case CommConstants.PATH_RIDE_ONGOING:
                        boolean ongoing = dataMap.getBoolean(CommConstants.EXTRA_VALUE);
                        if (!ongoing) {
                            // Ride is paused: exit the full screen activity
                            finish();
                            return;
                        }
                        break;

                    case CommConstants.PATH_RIDE_VALUES:
                        // Values update
                        float rideDistance = dataMap.getFloat(CommConstants.EXTRA_DISTANCE);
                        float rideSpeed = dataMap.getFloat(CommConstants.EXTRA_SPEED);
                        long rideStartDateOffset = dataMap.getLong(CommConstants.EXTRA_START_DATE_OFFSET);

                        runOnUiThread(() -> {
                            mSpeedDisplayFragment.setSpeed(rideSpeed);
                            mElapsedTimeDisplayFragment.setStartDateOffset(rideStartDateOffset);
                            mTotalDistanceDisplayFragment.setTotalDistance(rideDistance);
                        });
                        break;
                }
            }
        }
    };

}
