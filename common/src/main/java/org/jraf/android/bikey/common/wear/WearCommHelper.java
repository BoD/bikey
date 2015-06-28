/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.common.wear;

import java.util.HashSet;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.util.log.wrapper.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Helper singleton class to communicate with wearables.<br/>
 * Note: {@link #connect(android.content.Context)} must be called prior to calling all the other methods.<br/>
 * Note: a connection to a {@link com.google.android.gms.common.api.GoogleApiClient} is maintained by this class, which may or may not be a performance problem.
 */
public class WearCommHelper {
    private static final WearCommHelper INSTANCE = new WearCommHelper();

    private GoogleApiClient mGoogleApiClient;

    private WearCommHelper() {}

    public static WearCommHelper get() {
        return INSTANCE;
    }

    public void connect(Context context) {
        Log.d();
        if (mGoogleApiClient != null) return;
        mGoogleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
                Log.d("connectionHint=" + connectionHint);
            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d("cause=" + cause);
                // TODO reconnect
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.w("result=" + result);
                // TODO handle failures
            }
        }).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        Log.d();
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    public void sendMessage(final String path, @Nullable final byte[] payload) {
        Log.d("path=" + path);
        HashSet<String> results = new HashSet<>();
        PendingResult<NodeApi.GetConnectedNodesResult> nodesPendingResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodesPendingResult.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                for (Node node : result.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, payload);
                }
            }
        });
    }

    @WorkerThread
    public void sendMessageRideResume() {
        sendMessage(CommConstants.PATH_RIDE_CONTROL, CommConstants.PAYLOAD_RESUME);
    }

    @WorkerThread
    public void sendMessageRidePause() {
        sendMessage(CommConstants.PATH_RIDE_CONTROL, CommConstants.PAYLOAD_PAUSE);
    }


    /*
     * Ride values.
     */

    public void updateRideOngoing(boolean ongoing) {
        Log.d();
        updateValueNow(CommConstants.PATH_RIDE_ONGOING, ongoing);
    }

    public void clearRideValues() {
        Log.d();
        Wearable.DataApi.deleteDataItems(mGoogleApiClient, createUri(CommConstants.PATH_RIDE_VALUES));
    }

    public void updateRideValues(long startDateOffset, float speed, float distance, int heartRate) {
        Log.d("startDateOffset=" + startDateOffset + " speed=" + speed + " distance=" + distance + " heartRate=" + heartRate);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CommConstants.PATH_RIDE_VALUES);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putLong(CommConstants.EXTRA_START_DATE_OFFSET, startDateOffset);
        dataMap.putFloat(CommConstants.EXTRA_SPEED, speed);
        dataMap.putFloat(CommConstants.EXTRA_DISTANCE, distance);
        dataMap.putInt(CommConstants.EXTRA_HEART_RATE, heartRate);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    public Bundle retrieveRideValues() {
        Log.d();
        Uri uri = createUri(CommConstants.PATH_RIDE_VALUES);
        PendingResult<DataItemBuffer> pendingResult = Wearable.DataApi.getDataItems(mGoogleApiClient, uri);
        DataItemBuffer dataItemBuffer = pendingResult.await();
        if (dataItemBuffer.getCount() == 0) {
            Log.d("No result");
            dataItemBuffer.release();
            return null;
        }
        DataItem dataItem = dataItemBuffer.get(0);
        DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
        Bundle res = dataMap.toBundle();
        Log.d("res=" + res);
        dataItemBuffer.release();
        return res;
    }


    /*
     * Preferences.
     */

    public void updatePreferences() {
        Log.d();
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CommConstants.PATH_PREFERENCES);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putString(CommConstants.EXTRA_UNITS, UnitUtil.getUnits());

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    public String retrievePreferences(String prefExtraName) {
        Log.d();
        Uri uri = new Uri.Builder().scheme("wear").path(CommConstants.PATH_PREFERENCES).build();
        PendingResult<DataItemBuffer> pendingResult = Wearable.DataApi.getDataItems(mGoogleApiClient, uri);
        DataItemBuffer dataItemBuffer = pendingResult.await();
        if (dataItemBuffer.getCount() == 0) {
            Log.d("No result");
            dataItemBuffer.release();
            return null;
        }
        DataItem dataItem = dataItemBuffer.get(0);
        DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
        String res = dataMap.getString(prefExtraName);
        Log.d("res=" + res);
        dataItemBuffer.release();
        return res;
    }


    private void updateValueNow(String path, boolean value) {
        Log.d("path=" + path + " value=" + value);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putBoolean(CommConstants.EXTRA_VALUE, value);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    private void updateValueNow(String path, float value) {
        Log.d("path=" + path + " value=" + value);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putFloat(CommConstants.EXTRA_VALUE, value);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    private void updateValueNow(String path, long value) {
        Log.d("path=" + path + " value=" + value);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putLong(CommConstants.EXTRA_VALUE, value);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    private void updateValueNow(String path, int value) {
        Log.d("path=" + path + " value=" + value);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putInt(CommConstants.EXTRA_VALUE, value);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    public void addDataApiListener(DataApi.DataListener dataListener) {
        Wearable.DataApi.addListener(mGoogleApiClient, dataListener);
    }

    public void removeDataApiListener(DataApi.DataListener dataListener) {
        Wearable.DataApi.removeListener(mGoogleApiClient, dataListener);
    }

    private static Uri createUri(String path) {
        return new Uri.Builder().scheme("wear").path(path).build();
    }
}
