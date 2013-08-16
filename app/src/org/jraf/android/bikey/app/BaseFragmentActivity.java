package org.jraf.android.bikey.app;

import android.support.v4.app.FragmentActivity;

public class BaseFragmentActivity extends FragmentActivity {
    private boolean mPaused;

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
    }

    public boolean isPaused() {
        return mPaused;
    }

}
