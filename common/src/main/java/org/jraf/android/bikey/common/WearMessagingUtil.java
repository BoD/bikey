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
package org.jraf.android.bikey.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.jraf.android.util.log.wrapper.Log;

public class WearMessagingUtil {
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private GoogleApiClient mGoogleApiClient;

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
                Log.d("result=" + result);
            }
        }).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        Log.d();
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    public void sendMessage(final Path path) {
        Log.d("path=" + path);
        mThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                sendMessageNow(path);
            }
        });
    }

    private void sendMessageNow(Path path) {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            Log.d("node=" + node);
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node, path.toString(), null).await();
            if (!result.getStatus().isSuccess()) {
                Log.e("Could not send message: " + result.getStatus(), null);
            }
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }
}
