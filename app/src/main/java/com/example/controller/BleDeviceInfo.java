package com.example.controller;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Екатерина Захарова on 25.02.2016.
 */
public class BleDeviceInfo {
    private BluetoothDevice mBtDevice;
    private int mRssi;

    public BleDeviceInfo(BluetoothDevice device, int rssi) {
        this.mBtDevice = device;
        this.mRssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.mBtDevice;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void updateRssi(int rssiValue) {
        this.mRssi = rssiValue;
    }
}
