package com.example.bletest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BLEControllerListener{

    private RemoteControl remoteControl;
    private BLEController bleController;
    private String deviceAddress;
    private boolean isLEDOn = false;
    private boolean isAlive = false;
    private Button cntButton;
    private Button disButton;
    private Button switchBut;
    private TextView logText;
    private Thread heartBeatThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.logText = findViewById(R.id.logView);
        this.logText.setMovementMethod(new ScrollingMovementMethod());
        this.bleController = BLEController.getInstance(this);

        initConnectButton();
        initDisconnectButton();
        initSwitchLEDButton();

        checkBLESupport();
        checkPermissions();

        disableButtons();

    }

//    public void startHeartBeat(){
//        this.isAlive = true;
//        this.heartBeatThread = createHeartBeatThread();
//        this.heartBeatThread.start();
//    }
//
//    public void stopHeartBeat(){
//        if (this.isAlive){
//            this.isAlive=false;
//            this.heartBeatThread.interrupt();
//        }
//    }
//
//    private Thread createHeartBeatThread(){
//        return run()
//        {
//            while(MainActivity.this.isAlive){
//                try{
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }{return;}
//            }
//        }
//    }


    private void log(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.setText(logText.getText() + "\n" + text);
            }
        });
    }

    private void checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            log("\"Access Fine Location\" permission not granted yet!");
            log("Whitout this permission Blutooth devices cannot be searched!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }

    @Override
    public void BLEControllerConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log("[BLE]\tConnected");
                switchBut.setEnabled(true);
                disButton.setEnabled(true);
            }
        });
//        startHeartBeat();
    }

    @Override
    public void BLEControllerDisconnected() {
        log("[BLE]\tDisconnected");
        disableButtons();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cntButton.setEnabled(true);
            }
        });
        this.isLEDOn=false;
//        stopHeartBeat();
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        log("Device " + name + " found with address " + address);
        this.deviceAddress=address;
        this.cntButton.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            log("[BLE]\tSearching for this_is_the_fucking_BLE...");
            this.bleController.init();
        }
    }

    protected void onPause() {
        super.onPause();
        this.bleController.removeBLEControllerListener(this);
    }

    private void initConnectButton() {
        this.cntButton = findViewById(R.id.connectButton);
        this.cntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cntButton.setEnabled(false);
                log("Connecting...");
                bleController.connectToDevice(deviceAddress);
                switchBut.setEnabled(true);
                disButton.setEnabled(true);
            }
        });
    }

    private void initDisconnectButton() {
        this.disButton = findViewById(R.id.disconnectButton);
        this.disButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disButton.setEnabled(false);
                log("Disconnecting...");
                bleController.disconnect();
                cntButton.setEnabled(true);
            }
        });
    }


    private void initSwitchLEDButton() {
        this.switchBut = findViewById(R.id.switchButton);
        this.switchBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLEDOn = !isLEDOn;
                remoteControl.switchLED(isLEDOn);
            }
        });
    }

    private void disableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cntButton.setEnabled(false);
                disButton.setEnabled(false);
                switchBut.setEnabled(false);
            }
        });
    }

}