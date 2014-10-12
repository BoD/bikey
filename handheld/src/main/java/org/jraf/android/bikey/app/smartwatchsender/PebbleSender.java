package org.jraf.android.bikey.app.smartwatchsender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.util.log.wrapper.Log;

/**
 * SmartwatchSender implementation for the Pebble watch.
 */
public class PebbleSender extends SmartwatchSender {
    private Context mContext;
    private PebbleKit.PebbleDataReceiver sportsDataHandler = null;
    private int sportsState = Constants.SPORTS_STATE_INIT;

    @Override
    public void startSending(Context context) {
        mContext = context;
        startWatchApp();
        super.startSending(context);
    }

    @Override
    public void stopSending() {
        super.stopSending();
        if (sportsDataHandler != null) {
            mContext.unregisterReceiver(sportsDataHandler);
            sportsDataHandler = null;
        }
        stopWatchApp();
        mContext = null;
    }

    /**
     * Send a broadcast to launch the specified application on the connected Pebble.
     */
    private void startWatchApp() {
        final Bitmap customIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_pebble);
        PebbleKit.customizeWatchApp(mContext, Constants.PebbleAppType.SPORTS, "Bikey", customIcon);

        PebbleKit.startAppOnPebble(mContext, Constants.SPORTS_UUID);
    }

    /**
     * Send a broadcast to close the specified application on the connected Pebble.
     */
    private void stopWatchApp() {
        PebbleKit.closeAppOnPebble(mContext, Constants.SPORTS_UUID);
    }

    @Override
    protected void sendValues() {
        Log.d();
        float totalDistance = LogManager.get().getTotalDistance(mActiveRideUri);
        long startDateOffset = (System.currentTimeMillis() - mActivatedDate + mInitialDuration) / 1000;
        double speed = mSpeedometer.getSpeed() * 3.6; // m/s => km/h
        updatePebble(totalDistance, startDateOffset, speed);
    }

    private void updatePebble(float totalDistance, long startDateOffset, double speed) {
        long seconds = startDateOffset % 60;
        long minutes = startDateOffset / 60;
        String time = String.format("%02d:%02d", minutes, seconds);
        String distance = String.format("%2.2f", totalDistance / 1000f);
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
