package org.jraf.android.bike.app.hud;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.DataCollectingService;
import org.jraf.android.bike.backend.location.LocationManager;
import org.jraf.android.bike.backend.location.LocationManager.StatusListener;
import org.jraf.android.bike.backend.location.Speedometer;
import org.jraf.android.bike.util.UnitUtil;
import org.jraf.android.util.Log;


public class HudActivity extends Activity {
    private Handler mHandler = new Handler();

    private TextView mTxtValue;
    private ImageView mImgGpsStatus;

    private boolean mNavigationBarHiding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// TODO only if setting says so

        setContentView(R.layout.hud);

        mTxtValue = (TextView) findViewById(R.id.txtValue);
        mTxtValue.setOnClickListener(mValueOnClickListener);
        ((ToggleButton) findViewById(R.id.togRecording)).setOnCheckedChangeListener(mRecordingOnCheckedChangeListener);
        mImgGpsStatus = (ImageView) findViewById(R.id.imgGpsStatus);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupNavigationBarHiding();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);

        // Speed updates
        LocationManager.get().addLocationListener(mSpeedometer);
    }

    @Override
    protected void onPause() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        // Speed updates
        LocationManager.get().removeLocationListener(mSpeedometer);

        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupNavigationBarHiding() {
        findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d("visibility=" + visibility);
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    Log.d("Navigation bar showing");
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

    private OnClickListener mValueOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d();
        }
    };

    private OnCheckedChangeListener mRecordingOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("isChecked=" + isChecked);
            if (isChecked) {
                startService(new Intent(DataCollectingService.ACTION_START_COLLECTING));
            } else {
                startService(new Intent(DataCollectingService.ACTION_STOP_COLLECTING));
            }
        }
    };

    private Speedometer mSpeedometer = new Speedometer() {
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            mTxtValue.setText(UnitUtil.formatSpeed(getSpeed()));
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
}
