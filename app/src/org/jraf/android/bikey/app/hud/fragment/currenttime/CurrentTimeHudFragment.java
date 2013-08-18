package org.jraf.android.bikey.app.hud.fragment.currenttime;

import java.text.DateFormat;
import java.util.Date;

import android.os.Handler;

import org.jraf.android.bikey.app.hud.fragment.SimpleHudFragment;

public class CurrentTimeHudFragment extends SimpleHudFragment {
    protected static final long REFRESH_RATE = 30 * 1000;
    private Handler mHandler = new Handler();
    private DateFormat mTimeFormat;

    public static CurrentTimeHudFragment newInstance() {
        return new CurrentTimeHudFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
        setTextEnabled(true);
        mHandler.post(mShowTimeRunnable);
    }

    @Override
    public void onStop() {
        mHandler.removeCallbacks(mShowTimeRunnable);
        super.onStop();
    }

    private Runnable mShowTimeRunnable = new Runnable() {
        @Override
        public void run() {
            setText(mTimeFormat.format(new Date()));
            mHandler.postDelayed(mShowTimeRunnable, REFRESH_RATE);
        }
    };
}
