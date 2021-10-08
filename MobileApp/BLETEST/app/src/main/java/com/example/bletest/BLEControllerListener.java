package com.example.bletest;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public interface BLEControllerListener {
    public void BLEControllerConnected();
    public void BLEControllerDisconnected();
    public void BLEDeviceFound(String name, String address);


}
