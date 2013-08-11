package org.jraf.android.bike.app.hud;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.fragment.elapsedtime.ElapsedTimeHudFragment;
import org.jraf.android.bike.app.hud.fragment.speed.SpeedHudFragment;
import org.jraf.android.bike.backend.DataCollectingService;
import org.jraf.android.bike.backend.location.LocationManager;
import org.jraf.android.bike.backend.location.LocationManager.ActivityRecognitionListener;
import org.jraf.android.bike.backend.location.LocationManager.StatusListener;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.Log;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

import com.google.android.gms.location.DetectedActivity;


public class HudActivity extends FragmentActivity {
    private Handler mHandler = new Handler();

    private ImageView mImgGpsStatus;
    private ToggleButton mTogRecording;
    private ImageView mImgActivity;

    private boolean mNavigationBarHiding = false;
    private Uri mRideUri;
    private FragmentCycler mFragmentCycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// TODO only if setting says so

        mRideUri = getIntent().getData();

        setContentView(R.layout.hud);

        mTogRecording = (ToggleButton) findViewById(R.id.togRecording);
        mTogRecording.setEnabled(false);
        toggleRecordingIfActive();
        mImgGpsStatus = (ImageView) findViewById(R.id.imgGpsStatus);
        mImgActivity = (ImageView) findViewById(R.id.imgActivity);
        findViewById(R.id.vieFragmentCycle).setOnClickListener(mFragmentCycleOnClickListener);

        setupFragments();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupNavigationBarHiding();
        }
    }

    private void toggleRecordingIfActive() {
        new TaskFragment(new Task<HudActivity>() {
            private Uri mActiveRideUri;

            @Override
            protected void doInBackground() throws Throwable {
                mActiveRideUri = RideManager.get().getActiveRide();
            }

            @Override
            protected void onPostExecuteOk() {
                if (getActivity().mRideUri.equals(mActiveRideUri)) {
                    getActivity().mTogRecording.setChecked(true);
                }
                getActivity().mTogRecording.setEnabled(true);
                getActivity().mTogRecording.setOnCheckedChangeListener(getActivity().mRecordingOnCheckedChangeListener);
            }
        }).execute(getSupportFragmentManager());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Activity
        LocationManager.get().addActivityRecognitionListener(mActivityRecognitionListener);
    }

    @Override
    protected void onPause() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Activity
        LocationManager.get().removeActivityRecognitionListener(mActivityRecognitionListener);

        super.onPause();
    }

    private void setupFragments() {
        mFragmentCycler = new FragmentCycler(R.id.conFragments);
        mFragmentCycler.add(this, SpeedHudFragment.newInstance());
        mFragmentCycler.add(this, ElapsedTimeHudFragment.newInstance());
        mFragmentCycler.show(this);
    }

    private OnClickListener mFragmentCycleOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mFragmentCycler.cycle(HudActivity.this);
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
                    mFragmentCycler.cycle(HudActivity.this);
                    scheduleHideNavigationBar();
                }
            }
        });
        hideNavigationBar();
        mNavigationBarHiding = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("ev=" + ev);
        if (mNavigationBarHiding) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("down");
                    mHandler.removeCallbacks(mHideNavigationBarRunnable);
                    // Return true for down so we receive following events for this gesture
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("up");
                    scheduleHideNavigationBar();
                    break;

                default:
                    mHandler.removeCallbacks(mHideNavigationBarRunnable);
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
        mHandler.postDelayed(mHideNavigationBarRunnable, 2000);
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
                startService(new Intent(DataCollectingService.ACTION_START_COLLECTING, mRideUri, HudActivity.this, DataCollectingService.class));
            } else {
                startService(new Intent(DataCollectingService.ACTION_STOP_COLLECTING, mRideUri, HudActivity.this, DataCollectingService.class));
            }
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            if (active) {
                mImgGpsStatus.setImageResource(R.color.hud_gps_first_fix);
            } else {
                mImgGpsStatus.setImageResource(R.color.hud_gps_stopped);
            }
        }
    };

    private ActivityRecognitionListener mActivityRecognitionListener = new ActivityRecognitionListener() {
        @Override
        public void onActivityRecognized(int activityType, int confidence) {
            if (activityType == DetectedActivity.STILL) {
                mImgActivity.setImageResource(R.color.hud_activity_still);
            } else {
                mImgActivity.setImageResource(R.color.hud_activity_other);
            }
        }
    };
}
