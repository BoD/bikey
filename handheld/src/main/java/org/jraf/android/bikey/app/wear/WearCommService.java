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
package org.jraf.android.bikey.app.wear;

import java.util.Arrays;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.jraf.android.bikey.app.collect.LogCollectorService;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.bikey.common.wear.CommConstants;
import org.jraf.android.util.log.wrapper.Log;

public class WearCommService extends WearableListenerService {
    public WearCommService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(messageEvent.toString());
        switch (messageEvent.getPath()) {
            case CommConstants.PATH_RIDE_CONTROL:
                byte[] payload = messageEvent.getData();
                if (Arrays.equals(CommConstants.PAYLOAD_PAUSE, payload)) {
                    pauseCurrentRide();
                } else if (Arrays.equals(CommConstants.PAYLOAD_RESUME, payload)) {
                    resumeCurrentRide();
                }
                break;
        }
    }

    private void pauseCurrentRide() {
        Log.d();
        Uri currentRideUri = RideManager.get().getCurrentRide();
        if (currentRideUri == null) return;
        startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, currentRideUri, this, LogCollectorService.class));
    }

    private void resumeCurrentRide() {
        Log.d();
        Uri currentRideUri = RideManager.get().getCurrentRide();
        if (currentRideUri == null) return;
        startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, currentRideUri, this, LogCollectorService.class));
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {}
}