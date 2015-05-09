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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.bikey.app.ride.edit.RideEditActivity;
import org.jraf.android.bikey.app.ride.map.RideMapActivity;
import org.jraf.android.bikey.backend.export.genymotion.GenymotionExporter;
import org.jraf.android.bikey.backend.export.gpx.GpxExporter;
import org.jraf.android.bikey.backend.export.kml.KmlExporter;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.widget.LabelTextView;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.app.base.BaseAppCompatActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.math.MathUtil;
import org.jraf.android.util.ui.graph.GraphView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RideDetailActivity extends BaseAppCompatActivity implements AlertDialogListener {
    private static final String FRAGMENT_RETAINED_STATE = "FRAGMENT_RETAINED_STATE";

    private static final int POINTS_TO_GRAPH = 100;

    private static final int DIALOG_CONFIRM_DELETE = 0;
    private static final int DIALOG_SHARE = 1;

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

    @InjectView(R.id.grpSpeed)
    protected GraphView mGrpSpeed;

    @InjectView(R.id.grpCadence)
    protected GraphView mGrpCadence;

    @InjectView(R.id.txtHeartRateSectionTitle)
    protected TextView mTxtHeartRateSectionTitle;

    @InjectView(R.id.txtHeartRateMin)
    protected LabelTextView mTxtHeartRateMin;

    @InjectView(R.id.txtHeartRateMax)
    protected LabelTextView mTxtHeartRateMax;

    @InjectView(R.id.txtHeartRateAverage)
    protected LabelTextView mTxtHeartRateAverage;

    @InjectView(R.id.grpHeartRate)
    protected GraphView mGrpHeartRate;

    private RideDetailStateFragment mState;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRideUri = getIntent().getData();

        ButterKnife.inject(this);

        restoreState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void restoreState() {
        mState = (RideDetailStateFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_RETAINED_STATE);
        if (mState == null) {
            mState = new RideDetailStateFragment();
            getSupportFragmentManager().beginTransaction().add(mState, FRAGMENT_RETAINED_STATE).commit();
        }
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

            case R.id.action_display:
                finish();
                Intent intent = new Intent(this, DisplayActivity.class);
                intent.setData(mRideUri);
                startActivity(intent);
                return true;

            case R.id.action_delete:
                showDeleteDialog();
                return true;

            case R.id.action_share:
                showShareDialog();
                return true;

            case R.id.action_edit:
                startActivity(new Intent(this, RideEditActivity.class).setData(mRideUri));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        new TaskFragment(new Task<RideDetailActivity>() {
            private String mName;
            private Date mCreatedDate;
            private long mDuration;
            private float mDistance;
            private float mAverageMovingSpeed;
            private float mMaxSpeed;
            private Date mFirstActivatedDate;
            private Long mMovingDuration;
            private Float mAverageCadence;
            private float mMaxCadence;
            private List<LatLng> mLatLngArray;
            private float[] mSpeedArray;
            private float[] mCadenceArray;
            private float mMinHeartRate;
            private float mMaxHeartRate;
            private Float mAverageHeartRate;
            private float[] mHeartRateArray;

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
                mMinHeartRate = logManager.getMinHeartRate(rideUri);
                mMaxHeartRate = logManager.getMaxHeartRate(rideUri);
                mAverageHeartRate = logManager.getAverageHeartRate(rideUri);

                mLatLngArray = logManager.getLatLngArray(rideUri, POINTS_TO_GRAPH);

                List<Float> speedList = logManager.getSpeedArray(rideUri, POINTS_TO_GRAPH);
                mSpeedArray = CollectionUtil.unwrap(speedList.toArray(new Float[speedList.size()]));
                mSpeedArray = MathUtil.getMovingAverage(mSpeedArray, mSpeedArray.length / 10);

                List<Float> cadenceList = logManager.getCadenceArray(rideUri, POINTS_TO_GRAPH);
                mCadenceArray = CollectionUtil.unwrap(cadenceList.toArray(new Float[cadenceList.size()]));
                mCadenceArray = MathUtil.getMovingAverage(mCadenceArray, mCadenceArray.length / 10);

                List<Float> heartRateList = logManager.getHeartRateArray(rideUri, POINTS_TO_GRAPH);
                mHeartRateArray = CollectionUtil.unwrap(heartRateList.toArray(new Float[heartRateList.size()]));
                mHeartRateArray = MathUtil.getMovingAverage(mHeartRateArray, mHeartRateArray.length / 10);

                // Make sure the map is actually available
                getMap();
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
                a.mTxtDistanceTotal.setText(UnitUtil.formatDistance(mDistance, true, .85f, false));

                a.mTxtSpeedAverage.setText(UnitUtil.formatSpeed(mAverageMovingSpeed, true, .85f, false));
                a.mTxtSpeedMax.setText(UnitUtil.formatSpeed(mMaxSpeed, true, .85f, false));

                // Cadence
                if (mAverageCadence == null) {
                    a.mTxtCadenceSectionTitle.setVisibility(View.GONE);
                    a.mTxtCadenceAverage.setVisibility(View.GONE);
                    a.mTxtCadenceMax.setVisibility(View.GONE);
                    a.mGrpCadence.setVisibility(View.GONE);
                } else {
                    a.mTxtCadenceSectionTitle.setVisibility(View.VISIBLE);
                    a.mTxtCadenceAverage.setVisibility(View.VISIBLE);
                    a.mTxtCadenceAverage.setText(UnitUtil.formatCadence(mAverageCadence, true));
                    a.mTxtCadenceMax.setVisibility(View.VISIBLE);
                    a.mTxtCadenceMax.setText(UnitUtil.formatCadence(mMaxCadence, true));
                    a.mGrpCadence.setVisibility(View.VISIBLE);
                    a.mGrpCadence.setColor(0, a.getResources().getColor(R.color.graph_line));
                    a.mGrpCadence.setValues(0, mCadenceArray);
                }

                // Map
                if (mLatLngArray.size() > 0) {
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(mLatLngArray);
                    polylineOptions.color(getResources().getColor(R.color.map_polyline));
                    a.getMap().addPolyline(polylineOptions);
                    // Calculate bounds
                    LatLngBounds bounds = new LatLngBounds(mLatLngArray.get(0), mLatLngArray.get(0));
                    for (LatLng latLng : mLatLngArray) {
                        bounds = bounds.including(latLng);
                    }
                    int padding = getResources().getDimensionPixelSize(R.dimen.ride_detail_map_padding);
                    a.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    a.mConMap.setVisibility(View.VISIBLE);
                }

                // Speed graph
                a.mGrpSpeed.setColor(0, a.getResources().getColor(R.color.graph_line));
                a.mGrpSpeed.setValues(0, mSpeedArray);

                // Heart rate
                if (mAverageHeartRate == null) {
                    a.mTxtHeartRateSectionTitle.setVisibility(View.GONE);
                    a.mTxtHeartRateAverage.setVisibility(View.GONE);
                    a.mTxtHeartRateMin.setVisibility(View.GONE);
                    a.mTxtHeartRateMax.setVisibility(View.GONE);
                    a.mGrpHeartRate.setVisibility(View.GONE);
                } else {
                    a.mTxtHeartRateSectionTitle.setVisibility(View.VISIBLE);
                    a.mTxtHeartRateAverage.setVisibility(View.VISIBLE);
                    a.mTxtHeartRateAverage.setText(UnitUtil.formatHeartRate(mAverageHeartRate.intValue(), true));
                    a.mTxtHeartRateMin.setVisibility(View.VISIBLE);
                    a.mTxtHeartRateMin.setText(UnitUtil.formatHeartRate((int) mMinHeartRate, true));
                    a.mTxtHeartRateMax.setVisibility(View.VISIBLE);
                    a.mTxtHeartRateMax.setText(UnitUtil.formatHeartRate((int) mMaxHeartRate, true));
                    a.mGrpHeartRate.setVisibility(View.VISIBLE);
                    a.mGrpHeartRate.setColor(0, a.getResources().getColor(R.color.graph_line));
                    a.mGrpHeartRate.setValues(0, mHeartRateArray);
                }
            }
        }).execute(getSupportFragmentManager());
    }


    /*
     * Map.
     */

    /**
     * Blocks until the map is actually available.
     */
    @Background
    private GoogleMap getMap() {
        if (mMap == null) {
            final CountDownLatch latch = new CountDownLatch(1);
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;
                            latch.countDown();
                        }
                    });
                }
            });

            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
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


    /*
     * Delete.
     */

    private void showDeleteDialog() {
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_CONFIRM_DELETE);
        dialog.setTitle(R.string.preference_heartRate_disconnect_confirmDialog_title);
        dialog.setMessage(R.string.ride_detail_deleteDialog_message);
        dialog.setPositiveButton(android.R.string.ok);
        dialog.setNegativeButton(android.R.string.cancel);
        dialog.show(getSupportFragmentManager());
    }

    private void delete() {
        final long[] ids = {ContentUris.parseId(mRideUri)};
        new TaskFragment(new Task<RideDetailActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                RideManager.get().delete(ids);
            }

            @Override
            protected void onPostExecuteOk() {
                finish();
            }
        }).execute(getSupportFragmentManager());
    }


    /*
     * Share.
     */

    public void showShareDialog() {
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(DIALOG_SHARE);
        dialog.setTitle(R.string.ride_list_shareDialog_title);
        dialog.setItems(R.array.export_choices);
        dialog.show(getSupportFragmentManager());
    }

    @Override
    public void onClickListItem(int tag, int index, Object payload) {
        switch (index) {
            case 0:
                // Gpx
                mState.mExporter = new GpxExporter(mRideUri);
                break;
            case 1:
                // Kml
                mState.mExporter = new KmlExporter(mRideUri);
                break;
            case 3:
                // Genymotion script
                mState.mExporter = new GenymotionExporter(mRideUri);
                break;
        }
        startExport();
    }

    private void startExport() {
        new TaskFragment(new Task<RideDetailActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                getActivity().mState.mExporter.export();
            }

            @Override
            protected void onPostExecuteOk() {
                File exportedFile = getActivity().mState.mExporter.getExportFile();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject));
                String messageBody = getString(R.string.export_body);
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + exportedFile.getAbsolutePath()));
                sendIntent.setType("application/bikey");
                sendIntent.putExtra(Intent.EXTRA_TEXT, messageBody);

                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.ride_list_action_share)));

            }
        }.toastFail(R.string.export_failToast)).execute(getSupportFragmentManager());
    }


    /*
     * Dialog callbacks.
     */

    @Override
    public void onClickPositive(int tag, Object payload) {
        switch (tag) {
            case DIALOG_CONFIRM_DELETE:
                delete();
                break;
        }
    }

    @Override
    public void onClickNegative(int tag, Object payload) {}
}
