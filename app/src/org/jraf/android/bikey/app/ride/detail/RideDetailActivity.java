/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.app.ride.detail;

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.hud.HudActivity;
import org.jraf.android.bikey.app.ride.map.RideMapActivity;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.bikey.widget.LabelTextView;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.log.wrapper.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RideDetailActivity extends FragmentActivity {
    private Uri mRideUri;

    @InjectView(R.id.txtDateTimeDate)
    protected LabelTextView mTxtDateTimeDate;

    @InjectView(R.id.txtDateTimeStart)
    protected LabelTextView mTxtDateTimeStart;

    @InjectView(R.id.txtDateTimeFinish)
    protected LabelTextView mTxtDateTimeFinish;

    @InjectView(R.id.txtDurationMoving)
    protected LabelTextView mTxtDurationMoving;

    @InjectView(R.id.txtDurationTotal)
    protected LabelTextView mTxtDurationTotal;

    @InjectView(R.id.txtDistanceTotal)
    protected LabelTextView mTxtDistanceTotal;

    @InjectView(R.id.txtSpeedAverage)
    protected LabelTextView mTxtSpeedAverage;

    @InjectView(R.id.txtSpeedMax)
    protected LabelTextView mTxtSpeedMax;

    @InjectView(R.id.txtCadenceSectionTitle)
    protected TextView mTxtCadenceSectionTitle;

    @InjectView(R.id.txtCadenceAverage)
    protected LabelTextView mTxtCadenceAverage;

    @InjectView(R.id.txtCadenceMax)
    protected LabelTextView mTxtCadenceMax;

    @InjectView(R.id.conMap)
    protected FrameLayout mConMap;

    @InjectView(R.id.conDetailedInfo)
    protected View mConDetailedInfo;

    @InjectView(R.id.txtEmpty)
    protected View mTxtEmpty;

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mRideUri = getIntent().getData();

        ButterKnife.inject(this);

        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ride_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_hud:
                finish();
                Intent intent = new Intent(this, HudActivity.class);
                intent.setData(mRideUri);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        new TaskFragment(new Task<RideDetailActivity>() {
            private String mName;
            private Date mCreatedDate;
            private long mDuration;
            private double mDistance;
            private double mAverageMovingSpeed;
            private double mMaxSpeed;
            private Date mFirstActivatedDate;
            private Double mMovingDuration;
            private Float mAverageCadence;
            private Float mMaxCadence;
            private List<LatLng> mLatLngArray;

            @Override
            protected void doInBackground() throws Throwable {
                RideManager rideManager = RideManager.get();
                Uri rideUri = getActivity().mRideUri;
                RideCursor rideCursor = rideManager.query(rideUri);
                mName = rideCursor.getName();
                mCreatedDate = rideCursor.getCreatedDate();
                mFirstActivatedDate = rideCursor.getFirstActivatedDate();
                mDuration = rideCursor.getDuration();
                mDistance = rideCursor.getDistance();
                rideCursor.close();

                LogManager logManager = LogManager.get();
                mAverageMovingSpeed = logManager.getAverageMovingSpeed(rideUri);
                mMaxSpeed = logManager.getMaxSpeed(rideUri);
                mMovingDuration = logManager.getMovingDuration(rideUri);
                mAverageCadence = logManager.getAverageCadence(rideUri);
                mMaxCadence = logManager.getMaxCadence(rideUri);

                mLatLngArray = logManager.getLatLngArray(rideUri, 100);
            }

            @Override
            protected void onPostExecuteOk() {
                RideDetailActivity a = getActivity();
                if (mName != null) a.setTitle(mName);
                a.mTxtDateTimeDate.setText(DateUtils.formatDateTime(a, mCreatedDate.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_YEAR));

                if (mLatLngArray.isEmpty()) {
                    // Special case: we have no points. Show empty screen.
                    a.mConMap.setVisibility(View.GONE);
                    a.mConDetailedInfo.setVisibility(View.GONE);
                    a.mTxtEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                if (mFirstActivatedDate != null) {
                    a.mTxtDateTimeStart.setText(DateUtils.formatDateTime(a, mFirstActivatedDate.getTime(), DateUtils.FORMAT_SHOW_TIME));
                    a.mTxtDateTimeFinish.setText(DateUtils.formatDateTime(a, mFirstActivatedDate.getTime() + mDuration, DateUtils.FORMAT_SHOW_TIME));
                }
                if (mMovingDuration != null) a.mTxtDurationMoving.setText(DateTimeUtil.formatDuration(a, mMovingDuration.longValue()));
                a.mTxtDurationTotal.setText(DateTimeUtil.formatDuration(a, mDuration));
                a.mTxtDistanceTotal.setText(UnitUtil.formatDistance((float) mDistance, true, .85f));

                a.mTxtSpeedAverage.setText(UnitUtil.formatSpeed((float) mAverageMovingSpeed, true, .85f));
                a.mTxtSpeedMax.setText(UnitUtil.formatSpeed((float) mMaxSpeed, true, .85f));

                if (mAverageCadence == null) {
                    a.mTxtCadenceSectionTitle.setVisibility(View.GONE);
                    a.mTxtCadenceAverage.setVisibility(View.GONE);
                    a.mTxtCadenceMax.setVisibility(View.GONE);
                } else {
                    a.mTxtCadenceSectionTitle.setVisibility(View.VISIBLE);
                    a.mTxtCadenceAverage.setVisibility(View.VISIBLE);
                    a.mTxtCadenceAverage.setText(UnitUtil.formatCadence(mAverageCadence, true));
                    a.mTxtCadenceMax.setVisibility(View.VISIBLE);
                    a.mTxtCadenceMax.setText(UnitUtil.formatCadence(mMaxCadence, true));
                }

                // Map
                if (mLatLngArray.size() > 0) {
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(mLatLngArray);
                    polylineOptions.color(getResources().getColor(R.color.map_polyline));
                    Polyline polyline = a.getMap().addPolyline(polylineOptions);
                    // Calculate bounds
                    LatLngBounds bounds = new LatLngBounds(mLatLngArray.get(0), mLatLngArray.get(0));
                    for (LatLng latLng : mLatLngArray) {
                        bounds = bounds.including(latLng);
                    }
                    int padding = getResources().getDimensionPixelSize(R.dimen.ride_detail_map_padding);
                    a.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    a.mConMap.setVisibility(View.VISIBLE);
                }
            }
        }).execute(getSupportFragmentManager());
    }


    /*
     * Map.
     */

    private GoogleMap getMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        return mMap;
    }

    @OnClick(R.id.vieMapClickLayer)
    protected void onMapClicked() {
        Log.d();
        Intent intent = new Intent(this, RideMapActivity.class);
        intent.setData(mRideUri);
        startActivity(intent);
    }
}
