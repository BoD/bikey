package org.jraf.android.bikey.app.collect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.log.LogManager;

/**
 * Created by geoffrey on 11/08/14.
 */
public class PebbleUpdater extends WatchUpdater{

    private Context mContext;
    private PebbleKit.PebbleDataReceiver sportsDataHandler = null;
    private int sportsState = Constants.SPORTS_STATE_INIT;

    PebbleUpdater(){
        SEND_VALUES_RATE_S = 1;
    }

    public void startUpdates(Context context) {
        mContext = context;
        startWatchApp();
        super.startUpdates(context);
    }

    public void stopUpdates() {
        super.stopUpdates();
        if (sportsDataHandler != null) {
            mContext.unregisterReceiver(sportsDataHandler);
            sportsDataHandler = null;
        }
        stopWatchApp();
    }

    protected void initUpdater() {
        mSendValueRunnable = new Runnable() {
            @Override
            public void run() {
//                Log.d();
                if (mActiveRideUri == null) return;
                float totalDistance = LogManager.get().getTotalDistance(mActiveRideUri);
                long startDateOffset = (System.currentTimeMillis() - mActivatedDate + mInitialDuration)/1000;
                double speed = mSpeedometer.getSpeed() * 3.6; // m/s => km/h
                updatePebble(totalDistance, startDateOffset, speed);
            }
        };
    }

    // Send a broadcast to launch the specified application on the connected Pebble
    public void startWatchApp() {
        final Bitmap customIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_pebble2);
        PebbleKit.customizeWatchApp(mContext, Constants.PebbleAppType.SPORTS, "Bikey", customIcon);

        PebbleKit.startAppOnPebble(mContext, Constants.SPORTS_UUID);
    }

    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp() {
        PebbleKit.closeAppOnPebble(mContext, Constants.SPORTS_UUID);
    }

    private void updatePebble(float totalDistance, long startDateOffset, double speed) {
        long seconds = startDateOffset%60;
        long minutes = startDateOffset/60;
        String time = String.format("%02d:%02d", minutes, seconds);
        String distance = String.format("%2.2f", totalDistance/1000f);
        String addl_data = String.format("%1.0f", speed);

        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(Constants.SPORTS_UNITS_KEY, (byte) (Constants.SPORTS_UNITS_METRIC));
        data.addString(Constants.SPORTS_TIME_KEY, time);
        data.addString(Constants.SPORTS_DISTANCE_KEY, distance);
        data.addString(Constants.SPORTS_DATA_KEY, addl_data);
        data.addUint8(Constants.SPORTS_LABEL_KEY, (byte) (Constants.SPORTS_DATA_SPEED));

        PebbleKit.sendDataToPebble(mContext, Constants.SPORTS_UUID, data);
    }
}
