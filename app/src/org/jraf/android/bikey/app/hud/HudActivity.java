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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.app.hud;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.BaseFragmentActivity;
import org.jraf.android.bikey.app.hud.fragment.averagemovingspeed.AverageMovingSpeedHudFragment;
import org.jraf.android.bikey.app.hud.fragment.compass.CompassHudFragment;
import org.jraf.android.bikey.app.hud.fragment.currenttime.CurrentTimeHudFragment;
import org.jraf.android.bikey.app.hud.fragment.elapsedtime.ElapsedTimeHudFragment;
import org.jraf.android.bikey.app.hud.fragment.speed.SpeedHudFragment;
import org.jraf.android.bikey.app.hud.fragment.totaldistance.TotalDistanceHudFragment;
import org.jraf.android.bikey.backend.LogCollectorService;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.location.LocationManager.StatusListener;
import org.jraf.android.bikey.backend.provider.RideState;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.Log;
import org.jraf.android.util.ui.checkable.CheckableRelativeLayout;

public class HudActivity extends BaseFragmentActivity {
    private static final long DELAY_HIDE_CONTROLS = 4500;

    private Handler mHandler = new Handler();

    private ImageView mImgGpsStatus;
    private CheckableRelativeLayout mChkRecord;
    private TextView mChkRecordText;
    private Animator mChkRecordTextAnimator;
    private View mConTabsLeft;
    private View mConTabsRight;

    private boolean mNavigationBarHiding = false;
    private Uri mRideUri;
    private FragmentCycler mFragmentCycler;
    private boolean mControlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// TODO only if setting says so

        mRideUri = getIntent().getData();

        setContentView(R.layout.hud);

        mChkRecord = (CheckableRelativeLayout) findViewById(R.id.chkRecord);
        mChkRecord.setEnabled(false);
        mChkRecordText = (TextView) findViewById(R.id.chkRecord_text);
        mChkRecordTextAnimator = AnimatorInflater.loadAnimator(HudActivity.this, R.animator.blink);
        mChkRecordTextAnimator.setTarget(mChkRecordText);
        toggleRecordingIfActive();
        mImgGpsStatus = (ImageView) findViewById(R.id.imgGpsStatus);
        ((AnimationDrawable) mImgGpsStatus.getDrawable()).start();
        findViewById(R.id.vieFragmentCycle).setOnTouchListener(mFragmentCycleOnTouchListener);
        mConTabsLeft = findViewById(R.id.conTabsLeft);
        mConTabsRight = findViewById(R.id.conTabsRight);

        setupFragments();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupNavigationBarHiding();
        }
        scheduleHideControls();
    }

    private void toggleRecordingIfActive() {
        new AsyncTask<Void, Void, Void>() {
            private RideState mRideState;

            @Override
            protected Void doInBackground(Void... params) {
                mRideState = RideManager.get().getState(mRideUri);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                switch (mRideState) {
                    case CREATED:
                        mChkRecord.setChecked(false);
                        mChkRecordText.setText(R.string.hud_chkRecord_created);
                        break;

                    case ACTIVE:
                        mChkRecord.setChecked(true);
                        mChkRecordText.setText(R.string.hud_chkRecord_active);
                        mChkRecordTextAnimator.start();
                        break;

                    case PAUSED:
                        mChkRecord.setChecked(false);
                        mChkRecordText.setText(R.string.hud_chkRecord_paused);
                        break;
                }

                mChkRecord.setEnabled(true);
                mChkRecord.setOnCheckedChangeListener(mRecordingOnCheckedChangeListener);
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);
    }

    @Override
    protected void onStop() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        super.onStop();
    }

    private void setupFragments() {
        mFragmentCycler = new FragmentCycler(R.id.conFragments);
        mFragmentCycler.add(this, SpeedHudFragment.newInstance(), R.id.chkTabSpeed);
        mFragmentCycler.add(this, ElapsedTimeHudFragment.newInstance(), R.id.chkTabDuration);
        mFragmentCycler.add(this, TotalDistanceHudFragment.newInstance(), R.id.chkTabDistance);
        mFragmentCycler.add(this, AverageMovingSpeedHudFragment.newInstance(), R.id.chkTabAverageMovingSpeed);
        //        mFragmentCycler.add(this, SlopeHudFragment.newInstance(), R.id.chkTabSlope);
        mFragmentCycler.add(this, CompassHudFragment.newInstance(), R.id.chkTabCompass);
        mFragmentCycler.add(this, CurrentTimeHudFragment.newInstance(), R.id.chkTabCurrentTime);
        mFragmentCycler.show(this);
    }

    private OnTouchListener mFragmentCycleOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mFragmentCycler.cycle(HudActivity.this);
            }
            return true;
        }
    };


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupNavigationBarHiding() {
        findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d("visibility=" + visibility);
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    Log.d("Navigation bar showing");
                    if (!isPaused()) mFragmentCycler.cycle(HudActivity.this);
                    scheduleHideNavigationBar();
                    showControls();
                    scheduleHideControls();
                }
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

    private Runnable mHideNavigationBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideNavigationBar();
        }
    };

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
                startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, mRideUri, HudActivity.this, LogCollectorService.class));
                mChkRecordText.setText(R.string.hud_chkRecord_active);
                mChkRecordTextAnimator.start();
            } else {
                startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, mRideUri, HudActivity.this, LogCollectorService.class));
                mChkRecordText.setText(R.string.hud_chkRecord_paused);
                mChkRecordTextAnimator.cancel();
                mChkRecordText.setAlpha(1f);
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
     * Controls show / hide.
     */

    private void hideControls() {
        mControlsVisible = false;
        int duration = getResources().getInteger(R.integer.animation_controls_showHide);
        mConTabsLeft.animate().alpha(0).translationX(-mConTabsLeft.getWidth()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
        mConTabsRight.animate().alpha(0).translationX(mConTabsRight.getWidth()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
        mChkRecord.animate().alpha(0).translationY(-mChkRecord.getHeight()).setInterpolator(new AccelerateInterpolator()).setDuration(duration);
    }

    private void showControls() {
        if (mControlsVisible) return;
        mControlsVisible = true;
        int duration = getResources().getInteger(R.integer.animation_controls_showHide);
        mConTabsLeft.animate().alpha(1).translationX(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
        mConTabsRight.animate().alpha(1).translationX(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
        mChkRecord.animate().alpha(1).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration);
    }


    private Runnable mHideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            hideControls();
        }
    };

    private void scheduleHideControls() {
        mHandler.removeCallbacks(mHideControlsRunnable);
        mHandler.postDelayed(mHideControlsRunnable, DELAY_HIDE_CONTROLS);
    }
}
