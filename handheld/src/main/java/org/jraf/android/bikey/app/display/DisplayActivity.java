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
package org.jraf.android.bikey.app.display;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.collect.LogCollectorService;
import org.jraf.android.bikey.app.display.fragment.compass.CompassDisplayFragment;
import org.jraf.android.bikey.app.display.fragment.currenttime.CurrentTimeDisplayFragment;
import org.jraf.android.bikey.app.display.fragment.elapsedtime.ElapsedTimeDisplayFragment;
import org.jraf.android.bikey.app.display.fragment.heartrate.HeartRateDisplayFragment;
import org.jraf.android.bikey.app.display.fragment.speed.SpeedDisplayFragment;
import org.jraf.android.bikey.app.display.fragment.totaldistance.TotalDistanceDisplayFragment;
import org.jraf.android.bikey.backend.heartrate.HeartRateManager;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.location.LocationManager.StatusListener;
import org.jraf.android.bikey.backend.ride.RideListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.widget.fragmentcycler.FragmentCycler;
import org.jraf.android.util.app.base.BaseFragmentActivity;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.ui.checkable.CheckableRelativeLayout;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DisplayActivity extends BaseFragmentActivity {
    private static final long DELAY_HIDE_CONTROLS = 4500;

    private Handler mHandler = new Handler();

    private ImageView mImgGpsStatus;
    private CheckableRelativeLayout mChkRecord;
    private TextView mChkRecordText;
    private Animator mChkRecordTextAnimator;
    private View mConTabsA;
    private View mConTabsB;
    private View mConFragments;
    private TextView mTxtTitle;

    private boolean mNavigationBarHiding = false;
    private Uri mRideUri;
    private FragmentCycler mFragmentCycler;
    private boolean mControlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// TODO only if setting says so

        mRideUri = getIntent().getData();

        setContentView(R.layout.display);

        mChkRecord = (CheckableRelativeLayout) findViewById(R.id.chkRecord);
        mChkRecord.setEnabled(false);
        mChkRecordText = (TextView) findViewById(R.id.chkRecord_text);
        mChkRecordTextAnimator = AnimatorInflater.loadAnimator(this, R.animator.blink);
        mChkRecordTextAnimator.setTarget(mChkRecordText);
        mImgGpsStatus = (ImageView) findViewById(R.id.imgGpsStatus);
        ((AnimationDrawable) mImgGpsStatus.getDrawable()).start();
        findViewById(R.id.vieFragmentCycle).setOnTouchListener(mFragmentCycleOnTouchListener);
        mConTabsA = findViewById(R.id.conTabsA);
        mConTabsB = findViewById(R.id.conTabsB);
        mConFragments = findViewById(R.id.conFragments);
        mTxtTitle = (TextView) findViewById(R.id.txtTitle);

        setupFragments(savedInstanceState == null ? 0 : savedInstanceState.getInt("mFragmentCycler.currentVisibleIndex"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupNavigationBarHiding();
        }
        scheduleHideControls();

        setupFragmentContainer();
    }

    private void setupFragmentContainer() {
        mConFragments.post(() -> {
            PointF shrinkPercents = getShrinkPercents();
            mConFragments.setScaleX(shrinkPercents.x);
            mConFragments.setScaleY(shrinkPercents.y);

            mTxtTitle.setAlpha(0);
        });
    }

    private void toggleRecordingIfActive() {
        Single.fromCallable(() -> RideManager.get().getState(mRideUri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rideState -> {
                    switch (rideState) {
                        case CREATED:
                            mChkRecord.setChecked(false, false);
                            mChkRecordText.setText(R.string.display_chkRecord_created);
                            break;

                        case ACTIVE:
                            mChkRecord.setChecked(true, false);
                            mChkRecordText.setText(R.string.display_chkRecord_active);
                            if (!mChkRecordTextAnimator.isStarted()) mChkRecordTextAnimator.start();
                            break;

                        case PAUSED:
                            mChkRecord.setChecked(false, false);
                            mChkRecordText.setText(R.string.display_chkRecord_paused);
                            if (mChkRecordTextAnimator.isStarted()) mChkRecordTextAnimator.cancel();
                            mChkRecordText.setAlpha(1f);

                            break;
                    }

                    mChkRecord.setEnabled(true);
                    mChkRecord.setOnCheckedChangeListener(mRecordingOnCheckedChangeListener);

                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        toggleRecordingIfActive();

        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Ride
        RideManager.get().addListener(mRideListener);
    }

    @Override
    protected void onStop() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Ride
        RideManager.get().removeListener(mRideListener);

        super.onStop();
    }

    private void setupFragments(int currentVisibleIndex) {
        long updateTitleDelay = getResources().getInteger(R.integer.animation_controls_showHide);
        int tabColorEnabled = getResources().getColor(R.color.bright_foreground_dark);
        int tabColorDisabled = getResources().getColor(R.color.bright_foreground_disabled_dark);
        mFragmentCycler = new FragmentCycler(R.id.conFragments, mTxtTitle, updateTitleDelay, tabColorEnabled, tabColorDisabled);
        mFragmentCycler.setCurrentVisibleIndex(currentVisibleIndex);
        mFragmentCycler.add(this, SpeedDisplayFragment.newInstance(), R.id.chkTabSpeed, R.string.display_title_speed);
        mFragmentCycler.add(this, ElapsedTimeDisplayFragment.newInstance(), R.id.chkTabDuration, R.string.display_title_duration);
        mFragmentCycler.add(this, TotalDistanceDisplayFragment.newInstance(), R.id.chkTabDistance, R.string.display_title_distance);
        mFragmentCycler.add(this, HeartRateDisplayFragment.newInstance(), R.id.chkHeartRate, R.string.display_title_heartRate);
        mFragmentCycler.add(this, CompassDisplayFragment.newInstance(), R.id.chkTabCompass, R.string.display_title_compass);
        mFragmentCycler.add(this, CurrentTimeDisplayFragment.newInstance(), R.id.chkTabCurrentTime, R.string.display_title_currentTime);

        HeartRateManager heartRateManager = HeartRateManager.get();
        mFragmentCycler.setEnabled(this, HeartRateDisplayFragment.class, heartRateManager.isConnected() || heartRateManager.isConnecting());

        mFragmentCycler.show(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("mFragmentCycler.currentVisibleIndex", mFragmentCycler.getCurrentVisibleIndex());
        super.onSaveInstanceState(outState);
    }

    private OnTouchListener mFragmentCycleOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mFragmentCycler.cycle(thiz);
            }
            return true;
        }
    };


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupNavigationBarHiding() {
        findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.d("visibility=" + visibility);
            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                Log.d("Navigation bar showing");
                if (!isPaused()) mFragmentCycler.cycle(thiz);
                scheduleHideNavigationBar();
                showControls();
                scheduleHideControls();
            }
        });
        scheduleHideNavigationBar();
        mNavigationBarHiding = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mNavigationBarHiding) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("up");
                    scheduleHideNavigationBar();
                    scheduleHideControls();
                    break;

                default:
                    mHandler.removeCallbacks(mHideNavigationBarRunnable);
                    mHandler.removeCallbacks(mHideControlsRunnable);
                    showControls();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private Runnable mHideNavigationBarRunnable = this::hideNavigationBar;

    private void scheduleHideNavigationBar() {
        mHandler.removeCallbacks(mHideNavigationBarRunnable);
        mHandler.postDelayed(mHideNavigationBarRunnable, DELAY_HIDE_CONTROLS + 1000);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void hideNavigationBar() {
        Log.d();
        findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private OnCheckedChangeListener mRecordingOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("isChecked=" + isChecked);
            if (isChecked) {
                startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, mRideUri, thiz, LogCollectorService.class));
                mChkRecordText.setText(R.string.display_chkRecord_active);
                //                mChkRecordTextAnimator.start();
            } else {
                startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, mRideUri, thiz, LogCollectorService.class));
                mChkRecordText.setText(R.string.display_chkRecord_paused);
                //                mChkRecordTextAnimator.cancel();
                //                mChkRecordText.setAlpha(1f);
            }
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            if (active) {
                mImgGpsStatus.setVisibility(View.GONE);
            } else {
                mImgGpsStatus.setVisibility(View.VISIBLE);
            }
        }
    };

    public Uri getRideUri() {
        return mRideUri;
    }


    /*
     * Ride listener.
     */

    private RideListener mRideListener = new RideListener() {
        @Override
        public void onActivated(Uri rideUri) {
            if (!rideUri.equals(mRideUri)) return;
            mChkRecord.setChecked(true, false);
            mChkRecordText.setText(R.string.display_chkRecord_active);
            if (!mChkRecordTextAnimator.isStarted()) mChkRecordTextAnimator.start();
        }

        @Override
        public void onPaused(Uri rideUri) {
            if (!rideUri.equals(mRideUri)) return;
            mChkRecord.setChecked(false, false);
            mChkRecordText.setText(R.string.display_chkRecord_paused);
            if (mChkRecordTextAnimator.isStarted()) mChkRecordTextAnimator.cancel();
            mChkRecordText.setAlpha(1f);
        }
    };


    /*
     * Controls show / hide.
     */

    private void hideControls() {
        mControlsVisible = false;
        int duration = getResources().getInteger(R.integer.animation_controls_showHide);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape: tab containers slide to left / right
            mConTabsA.animate().alpha(0).translationX(-mConTabsA.getWidth()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
            mConTabsB.animate().alpha(0).translationX(mConTabsB.getWidth()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
        } else {
            // Portrait: tab containers slide to top / bottom
            mConTabsA.animate().alpha(0).translationY(-mConTabsA.getHeight()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
            mConTabsB.animate().alpha(0).translationY(mConTabsB.getHeight()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
        }

        // 'Uncompress' the main fragment
        mConFragments.animate().scaleX(1f).scaleY(1f).setInterpolator(new AccelerateInterpolator()).setDuration(duration);

        // Record button
        mChkRecord.animate().alpha(0).translationY(-mChkRecord.getHeight()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);

        // Show the title
        mTxtTitle.animate().alpha(1f).setDuration(duration).setStartDelay(duration);
    }

    private PointF getShrinkPercents() {
        float percentX;
        float percentY;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int fragmentWidth = mConFragments.getWidth();
            percentX = (fragmentWidth - 2 * getResources().getDimension(R.dimen.display_tabs_width)) / fragmentWidth;
            // Remove 5% because it looks better
            percentX -= .05f;
            percentY = percentX + (1f - percentX) / 2f;
        } else {
            int fragmentHeight = mConFragments.getHeight();
            percentY = (fragmentHeight - 2 * getResources().getDimension(R.dimen.display_tabs_width)) / fragmentHeight;
            // Remove 5% because it looks better
            percentY -= .05f;
            percentX = percentY + (1f - percentY) / 2f;
        }
        return new PointF(percentX, percentY);
    }

    private void showControls() {
        if (mControlsVisible) return;
        mControlsVisible = true;
        int duration = getResources().getInteger(R.integer.animation_controls_showHide);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape: tab containers slide from left / right
            mConTabsA.animate().alpha(1).translationX(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
            mConTabsB.animate().alpha(1).translationX(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
        } else {
            // Portrait: tab containers slide from top / bottom
            mConTabsA.animate().alpha(1).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
            mConTabsB.animate().alpha(1).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
        }

        // 'Compress' the main fragment, to make space for the tab containers
        PointF shrinkPercents = getShrinkPercents();
        mConFragments.animate().scaleX(shrinkPercents.x).scaleY(shrinkPercents.y).setInterpolator(new DecelerateInterpolator()).setDuration(duration);

        // Record button
        mChkRecord.animate().alpha(1).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);

        mTxtTitle.animate().alpha(0).setDuration(duration).setStartDelay(0);
    }

    private Runnable mHideControlsRunnable = this::hideControls;

    private void scheduleHideControls() {
        mHandler.removeCallbacks(mHideControlsRunnable);
        mHandler.postDelayed(mHideControlsRunnable, DELAY_HIDE_CONTROLS);
    }
}
