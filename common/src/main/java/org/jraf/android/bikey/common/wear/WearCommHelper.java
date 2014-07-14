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

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.jraf.android.bikey.common.UnitUtil;
import org.jraf.android.util.log.wrapper.Log;

/**
 * Helper singleton class to communicate with wearables.<br/>
 * Note: {@link #connect(android.content.Context)} must be called prior to calling all the other methods.<br/>
 * Note: a connection to a {@link com.google.android.gms.common.api.GoogleApiClient} is maintained by this class, which may or may not a performance problem.
 */
public class WearCommHelper {
    private static final WearCommHelper INSTANCE = new WearCommHelper();

    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
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

    public void sendMessage(final String path) {
        Log.d("path=" + path);
        mThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                sendMessageNow(path);
            }
        });
    }

    private void sendMessageNow(String path) {
        Collection<String> nodes = getConnectedNodes();
        for (String node : nodes) {
            Log.d("node=" + node);
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node, path, null).await();
            if (!result.getStatus().isSuccess()) {
                Log.w("Could not send message: " + result.getStatus());
            }
        }
    }

    public void updateRideValues(final boolean ongoing, final long duration, final float speed, final float distance, final int heartRate) {
        Log.d();
        mThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                updateRideValuesNow(ongoing, duration, speed, distance, heartRate);
            }
        });
    }

    private void updateRideValuesNow(boolean ongoing, long duration, float speed, float distance, int heartRate) {
        Log.d("ongoing=" + ongoing + " duration=" + duration + " speed=" + speed + " distance=" + distance + " heartRate=" + heartRate);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CommConstants.PATH_RIDE_VALUES);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putBoolean(CommConstants.EXTRA_ONGOING, ongoing);
        dataMap.putLong(CommConstants.EXTRA_DURATION, duration);
        dataMap.putFloat(CommConstants.EXTRA_SPEED, speed);
        dataMap.putFloat(CommConstants.EXTRA_DISTANCE, distance);
        dataMap.putInt(CommConstants.EXTRA_HEART_RATE, heartRate);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
    }

    public void updatePreferences() {
        Log.d();
        mThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                updatePreferencesNow();
            }
        });
    }

    private void updatePreferencesNow() {
        Log.d();
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CommConstants.PATH_PREFERENCES);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putString(CommConstants.EXTRA_UNITS, UnitUtil.getUnits());

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
    }

    private Collection<String> getConnectedNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }
}
