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
package org.jraf.android.bikey.backend.heartrate;

import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import org.jraf.android.bikey.app.Application;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.listeners.Listeners.Dispatcher;
import org.jraf.android.util.log.LogUtil;
import org.jraf.android.util.log.wrapper.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HeartRateManager {
    private static final HeartRateManager INSTANCE = new HeartRateManager();

    // See https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.heart_rate.xml
    private static final int GATT_SERVICE_HEART_RATE = 0x180D;

    // See https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
    private static final int GATT_CHARACTERISTIC_HEART_RATE_MEASUREMENT = 0x2A37;

    // See https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml    
    private static final int GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = 0x2902;


    public static HeartRateManager get() {
        return INSTANCE;
    }

    private Context mContext;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    private Listeners<HeartRateListener> mListeners = new Listeners<HeartRateListener>() {
        @Override
        protected void onFirstListener() {
            startListening();
        }

        @Override
        protected void onNoMoreListeners() {
            stopListening();
        }
    };

    private int mLastValue = -1;
    private boolean mConnected;

    private HeartRateManager() {
        mContext = Application.getApplication();
    }

    public void addListener(HeartRateListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(HeartRateListener listener) {
        mListeners.remove(listener);
    }

    protected void startListening() {
        Log.d();
    }

    protected void stopListening() {
        Log.d();
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mBluetoothGattCallback);
    }

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("status=" + LogUtil.getConstantName(BluetoothGatt.class, status, "GATT_") + " newState="
                    + LogUtil.getConstantName(BluetoothProfile.class, newState, "STATE_"));
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    onDisconnect();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("status=" + LogUtil.getConstantName(BluetoothGatt.class, status, "GATT_"));
            List<BluetoothGattService> services = gatt.getServices();
            boolean found = false;
            for (BluetoothGattService service : services) {
                Log.d(service.getUuid().toString());
                if (getAssignedNumber(service.getUuid()) == GATT_SERVICE_HEART_RATE) {
                    // Found heart rate service
                    onHeartRateServiceFound(service);
                    found = true;
                    break;
                }
            }
            if (!found) {
                onHeartRateServiceNotFound();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("characteristic=" + characteristic.getUuid());
            int format;
            int flag = characteristic.getProperties();
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            int previousValue = mLastValue;

            int value = characteristic.getIntValue(format, 1);
            if (value < 50) {
                // This is probably a false measurement, consider this as a disconnect
                if (mConnected) {
                    // Disconnect
                    onDisconnect();
                }

                return;
            }


            mLastValue = value;
            Log.d("heartRate=" + mLastValue);

            if (!mConnected) {
                mConnected = true;

                // Inform listeners
                mListeners.dispatch(new Dispatcher<HeartRateListener>() {
                    @Override
                    public void dispatch(HeartRateListener listener) {
                        listener.onConnect();
                    }
                });
            }

            if (previousValue != mLastValue) {
                // Inform listeners
                mListeners.dispatch(new Dispatcher<HeartRateListener>() {
                    @Override
                    public void dispatch(HeartRateListener listener) {
                        listener.onHeartRateChange(mLastValue);
                    }
                });
            }
        }
    };

    private void onDisconnect() {
        mConnected = false;
        mLastValue = -1;
        mListeners.dispatch(new Dispatcher<HeartRateListener>() {
            @Override
            public void dispatch(HeartRateListener listener) {
                listener.onDisconnect();
            }
        });
    }


    /*
     * Service.
     */

    protected void onHeartRateServiceFound(BluetoothGattService service) {
        Log.d();
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            Log.d("characteristic=" + characteristic.getUuid());
            boolean found = false;
            if (getAssignedNumber(characteristic.getUuid()) == GATT_CHARACTERISTIC_HEART_RATE_MEASUREMENT) {
                // Found heart read measurement characteristic
                onHeartRateMeasurementCharacteristicFound(characteristic);
                found = true;
                break;
            }
            if (!found) {
                onHeartRateMeasurementCharacteristicNotFound();
            }
        }
    }

    protected void onHeartRateServiceNotFound() {
        Log.d();
    }


    /*
     * Characteristic.
     */

    private void onHeartRateMeasurementCharacteristicFound(BluetoothGattCharacteristic characteristic) {
        Log.d();
        boolean found = false;
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            Log.d("descriptor=" + descriptor.getUuid());
            if (getAssignedNumber(descriptor.getUuid()) == GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION) {
                onClientCharacteristicConfigurationDescriptorFound(characteristic, descriptor);
                found = true;
                break;
            }
        }
        if (!found) {
            onClientCharacteristicConfigurationDescriptorNotFound();
        }
    }

    private void onHeartRateMeasurementCharacteristicNotFound() {
        Log.d();
    }


    /*
     * Descriptor.
     */

    private void onClientCharacteristicConfigurationDescriptorFound(BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor) {
        Log.d();
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private void onClientCharacteristicConfigurationDescriptorNotFound() {
        Log.d();
    }


    /*
     * Helpers.
     */

    private static int getAssignedNumber(UUID uuid) {
        // Keep only the significant bits of the UUID
        return (int) ((uuid.getMostSignificantBits() & 0x0000FFFF00000000L) >> 32);
    }

    public boolean isConnected() {
        return mConnected;
    }

    public int getLastValue() {
        return mLastValue;
    }
}
