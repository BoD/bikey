/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.app.heart.bluetooth;

import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ListFragment;

import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.wrapper.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScanListFragment extends ListFragment {
    private static final int REQUEST_ENABLE_BT = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BleScanListAdapter mBleScanListAdapter;
    private Set<String> mFoundDeviceAddressList = new HashSet<>();

    public BleScanListFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mScanning) startScan();
    }

    @Override
    public void onDestroy() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        super.onDestroy();
    }

    private void startScan() {
        mScanning = true;

        boolean enabled = ensureBluetoothEnabled();
        Log.d("enabled=" + enabled);
        if (!enabled) {
            return;
        }
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private boolean ensureBluetoothEnabled() {
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    startScan();
                } else {
                    getActivity().finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("device=" + device.getAddress());

            if (mFoundDeviceAddressList.contains(device.getAddress())) {
                // Device already listed: ignore
                return;
            }
            mFoundDeviceAddressList.add(device.getAddress());

            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBleScanListAdapter == null) {
                        mBleScanListAdapter = new BleScanListAdapter(getActivity());
                        setListAdapter(mBleScanListAdapter);
                    }
                    mBleScanListAdapter.add(device);
                }
            });
        }
    };

}
