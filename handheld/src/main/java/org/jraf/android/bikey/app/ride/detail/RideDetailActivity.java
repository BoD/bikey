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
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.bikey.app.ride.edit.RideEditActivity;
import org.jraf.android.bikey.app.ride.map.RideMapActivity;
import org.jraf.android.bikey.backend.export.bikey.BikeyExporter;
import org.jraf.android.bikey.backend.export.genymotion.GenymotionExporter;
import org.jraf.android.bikey.backend.export.gpx.GpxExporter;
import org.jraf.android.bikey.backend.export.kml.KmlExporter;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.bikey.databinding.RideDetailBinding;
import org.jraf.android.util.app.base.BaseAppCompatActivity;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.math.MathUtil;

public class RideDetailActivity extends BaseAppCompatActivity implements AlertDialogListener {
    private static final String FRAGMENT_RETAINED_STATE = "FRAGMENT_RETAINED_STATE";

    private static final int POINTS_TO_GRAPH = 100;

    private static final int DIALOG_CONFIRM_DELETE = 0;
    private static final int DIALOG_SHARE = 1;

    private RideDetailBinding mBinding;

    private Uri mRideUri;
    private RideDetailStateFragment mState;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.ride_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRideUri = getIntent().getData();

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
            protected void onPreExecute() {
                mBinding.pgbLoading.setVisibility(View.VISIBLE);
                mBinding.conRoot.setVisibility(View.INVISIBLE);
            }

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

                // Make sure the map is actually available (this is blocking for a few seconds)
                getMap();
            }

            @Override
            protected void onPostExecuteOk() {
                RideDetailActivity a = getActivity();

                if (a.mMap == null) return;

                a.mBinding.pgbLoading.setVisibility(View.GONE);
                a.mBinding.conRoot.setVisibility(View.VISIBLE);

                if (mName != null) a.setTitle(mName);
                a.mBinding.txtDateTimeDate
                        .setText(DateUtils.formatDateTime(a, mCreatedDate.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_YEAR));

                if (mLatLngArray.isEmpty()) {
                    // Special case: we have no points. Show empty screen.
                    a.mBinding.conMap.setVisibility(View.GONE);
                    a.mBinding.conDetailedInfo.setVisibility(View.GONE);
                    a.mBinding.txtEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                if (mFirstActivatedDate != null) {
                    mBinding.txtDateTimeStart.setText(DateUtils.formatDateTime(a, mFirstActivatedDate.getTime(), DateUtils.FORMAT_SHOW_TIME));
                    mBinding.txtDateTimeFinish.setText(DateUtils.formatDateTime(a, mFirstActivatedDate.getTime() + mDuration, DateUtils.FORMAT_SHOW_TIME));
                }
                if (mMovingDuration != null) mBinding.txtDurationMoving.setText(DateTimeUtil.formatDuration(a, mMovingDuration));
                mBinding.txtDurationTotal.setText(DateTimeUtil.formatDuration(a, mDuration));
                mBinding.txtDistanceTotal.setText(UnitUtil.formatDistance(mDistance, true, .85f, false));

                mBinding.txtSpeedAverage.setText(UnitUtil.formatSpeed(mAverageMovingSpeed, true, .85f, false));
                mBinding.txtSpeedMax.setText(UnitUtil.formatSpeed(mMaxSpeed, true, .85f, false));

                // Cadence
                if (mAverageCadence == null) {
                    mBinding.txtCadenceSectionTitle.setVisibility(View.GONE);
                    mBinding.txtCadenceAverage.setVisibility(View.GONE);
                    mBinding.txtCadenceMax.setVisibility(View.GONE);
                    mBinding.grpCadence.setVisibility(View.GONE);
                } else {
                    mBinding.txtCadenceSectionTitle.setVisibility(View.VISIBLE);
                    mBinding.txtCadenceAverage.setVisibility(View.VISIBLE);
                    mBinding.txtCadenceAverage.setText(UnitUtil.formatCadence(mAverageCadence, true));
                    mBinding.txtCadenceMax.setVisibility(View.VISIBLE);
                    mBinding.txtCadenceMax.setText(UnitUtil.formatCadence(mMaxCadence, true));
                    mBinding.grpCadence.setVisibility(View.VISIBLE);
                    mBinding.grpCadence.setColor(0, a.getResources().getColor(R.color.graph_line));
                    mBinding.grpCadence.setValues(0, mCadenceArray);
                }

                // Map
                a.getMap().getUiSettings().setMapToolbarEnabled(false);
                if (mLatLngArray.size() > 0) {
                    // Polyline
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(mLatLngArray);
                    polylineOptions.color(getResources().getColor(R.color.map_polyline));
                    a.getMap().addPolyline(polylineOptions);

                    // Start / finish markers
                    a.getMap().addMarker(new MarkerOptions()
                            .position(mLatLngArray.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    a.getMap().addMarker(new MarkerOptions()
                            .position(mLatLngArray.get(mLatLngArray.size() - 1)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    // Calculate bounds
                    LatLngBounds bounds = new LatLngBounds(mLatLngArray.get(0), mLatLngArray.get(0));
                    for (LatLng latLng : mLatLngArray) {
                        bounds = bounds.including(latLng);
                    }
                    int padding = getResources().getDimensionPixelSize(R.dimen.ride_detail_map_padding);
                    a.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    a.mBinding.conMap.setVisibility(View.VISIBLE);
                }

                // Speed graph
                mBinding.grpSpeed.setColor(0, a.getResources().getColor(R.color.graph_line));
                mBinding.grpSpeed.setValues(0, mSpeedArray);

                // Heart rate
                if (mAverageHeartRate == null) {
                    mBinding.txtHeartRateSectionTitle.setVisibility(View.GONE);
                    mBinding.txtHeartRateAverage.setVisibility(View.GONE);
                    mBinding.txtHeartRateMin.setVisibility(View.GONE);
                    mBinding.txtHeartRateMax.setVisibility(View.GONE);
                    mBinding.grpHeartRate.setVisibility(View.GONE);
                } else {
                    mBinding.txtHeartRateSectionTitle.setVisibility(View.VISIBLE);
                    mBinding.txtHeartRateAverage.setVisibility(View.VISIBLE);
                    mBinding.txtHeartRateAverage.setText(UnitUtil.formatHeartRate(mAverageHeartRate.intValue(), true));
                    mBinding.txtHeartRateMin.setVisibility(View.VISIBLE);
                    mBinding.txtHeartRateMin.setText(UnitUtil.formatHeartRate((int) mMinHeartRate, true));
                    mBinding.txtHeartRateMax.setVisibility(View.VISIBLE);
                    mBinding.txtHeartRateMax.setText(UnitUtil.formatHeartRate((int) mMaxHeartRate, true));
                    mBinding.grpHeartRate.setVisibility(View.VISIBLE);
                    mBinding.grpHeartRate.setColor(0, a.getResources().getColor(R.color.graph_line));
                    mBinding.grpHeartRate.setValues(0, mHeartRateArray);
                }
            }
        }).execute(getSupportFragmentManager(), false);
    }


    /*
     * Map.
     */

    /**
     * Blocks until the map is actually available.
     */
    @WorkerThread
    private GoogleMap getMap() {
        if (mMap == null) {
            CountDownLatch latch = new CountDownLatch(1);
            HandlerUtil.runOnUiThread(() -> {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment == null) {
                    latch.countDown();
                    return;
                }
                mapFragment.getMapAsync(googleMap -> {
                    mMap = googleMap;
                    latch.countDown();
                });
            });

            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
        }
        return mMap;
    }

    public void onMapClicked(View view) {
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
        dialog.title(R.string.preference_heartRate_disconnect_confirmDialog_title);
        dialog.message(R.string.ride_detail_deleteDialog_message);
        dialog.positiveButton(android.R.string.ok);
        dialog.negativeButton(android.R.string.cancel);
        dialog.show(this);
    }

    private void delete() {
        long[] ids = {ContentUris.parseId(mRideUri)};
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
        dialog.title(R.string.ride_list_shareDialog_title);
        dialog.items(R.array.export_choices);
        dialog.show(this);
    }

    @Override
    public void onDialogClickListItem(int tag, int index, Object payload) {
        switch (index) {
            case 0:
                // Gpx
                mState.mExporter = new GpxExporter(mRideUri);
                break;
            case 1:
                // Kml
                mState.mExporter = new KmlExporter(mRideUri);
                break;
            case 2:
                // Genymotion script
                mState.mExporter = new GenymotionExporter(mRideUri);
                break;
            case 3:
                // Bikey
                mState.mExporter = new BikeyExporter(mRideUri);
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
    public void onDialogClickPositive(int tag, Object payload) {
        switch (tag) {
            case DIALOG_CONFIRM_DELETE:
                delete();
                break;
        }
    }

    @Override
    public void onDialogClickNegative(int tag, Object payload) {}
}
