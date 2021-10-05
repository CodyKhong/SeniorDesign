package com.example.collarcompanionv3;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothTest extends AppCompatActivity {
    UUID mUUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        //testing connectivity
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice esp32 = mBluetoothAdapter.getRemoteDevice("94:B9:7E:7B:AD:E2");
        BluetoothSocket btSocket = null;

        int counter = 0;

        do {
            try {
                btSocket = esp32.createRfcommSocketToServiceRecord(mUUID);
                btSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        }while (!btSocket.isConnected()&& counter < 3);

        try {
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = null;
        try {
            inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());
            for (int i = 0; i<26; i++){
                byte b = (byte) inputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}