package com.example.collarcompanionv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import top.defaults.colorpicker.ColorPickerPopup;



public class AdminDashboard extends AppCompatActivity implements BLEControllerListener{
    private TextView logView;
    private Button connectButton;
    private Button disconnectButton;
    private Button switchLEDButton;
    private Button sendButton;

    private BLEController bleController;
    private RemoteControl remoteControl;
    private String deviceAddress;

    private boolean isLEDOn = false;

    private boolean isAlive = false;
    private Thread heartBeatThread = null;

    private static final String TAG = "AdminDashboard";

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
    }

    //LED Section of the Test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        this.bleController = BLEController.getInstance(this);
        this.remoteControl = new RemoteControl(this.bleController);

        this.logView = findViewById(R.id.logView);
        this.logView.setMovementMethod(new ScrollingMovementMethod());

        initConnectButton();
        initDisconnectButton();
        initSwitchLEDButton();
//        initSendButton();

        checkBLESupport();
        checkPermissions();

        disableButtons();

        Button btnLED = (Button) findViewById(R.id.button8);
        TextView btnTEXT = (TextView) findViewById(R.id.textView10);
        btnLED.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                new ColorPickerPopup.Builder(AdminDashboard.this)
                        .initialColor(Color.RED)
                        .enableBrightness(true)
                        .enableAlpha(true)
                        .okTitle("Choose")
                        .cancelTitle("Cancel")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(view, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                //this method call when user selct color
                                btnTEXT.setTextColor(color);
                                String hexColor = Integer.toHexString(color).substring(2);
                                btnTEXT.setText(hexColor);
<<<<<<< Updated upstream
                                remoteControl.DATASEND(color);
=======
                                remoteControl.LEDSend(color);
>>>>>>> Stashed changes
                                byte RedByte = (byte)((color>>16) & 0xFF);
                                String R = String.format("%8s", Integer.toBinaryString(RedByte & 0xFF)).replace(' ', '0');
                                byte GreenByte = (byte)((color>>8) & 0xFF);
                                String G = String.format("%8s", Integer.toBinaryString(GreenByte & 0xFF)).replace(' ', '0');
                                byte BlueByte = (byte)(color & 0xFF);
                                String B = String.format("%8s", Integer.toBinaryString(BlueByte & 0xFF)).replace(' ', '0');
                                log("LED switched to: " + hexColor);
                                log("Red: "+ R);
                                log("Green: "+ G);
                                log("Blue: "+ B);
                            }

                            @Override
                            public void onColor(int color, boolean fromUser) {
                                //this method call when user selecting color
                                btnTEXT.setTextColor(color);
                                String hexColor = "#" + Integer.toHexString(color).substring(2);
                                btnTEXT.setText(hexColor);

                            }
                        });

            }
        });
<<<<<<< Updated upstream
=======

        Button TempReqLED = (Button) findViewById(R.id.TempReqbutton);
        TempReqLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remoteControl.TempRequest();
            }
        });

>>>>>>> Stashed changes
    }
    private void initConnectButton() {
        this.connectButton = findViewById(R.id.connectButton);
        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButton.setEnabled(false);
                log("Connecting...");
                bleController.connectToDevice(deviceAddress);
            }
        });
    }

    private void initDisconnectButton() {
        this.disconnectButton = findViewById(R.id.disconnectButton);
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectButton.setEnabled(false);
                log("Disconnecting...");
                bleController.disconnect();
            }
        });
    }

    private void initSwitchLEDButton() {
        this.switchLEDButton = findViewById(R.id.switchButton);
        this.switchLEDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLEDOn = !isLEDOn;
                remoteControl.switchLED(isLEDOn);
                log("LED switched " + (isLEDOn?"On":"Off"));
            }
        });
    }

<<<<<<< Updated upstream
=======
//    private void initGetTempButton(){
//        this.remoteControl.DATAREAD();
//    }
>>>>>>> Stashed changes
//    private void initSendButton() {
//        this.sendButton = findViewById(R.id.button8);
//        this.sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int fuckyou=0x12;
//                remoteControl.DATASEND(fuckyou);
//                log("LED switched ");
//            }
//        });
//    }

    private void disableButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(false);
                switchLEDButton.setEnabled(false);
            }
        });
    }

    private void log(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.setText(logView.getText() + "\n" + text);
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();

        this.deviceAddress = null;
        this.bleController = BLEController.getInstance(this);
        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
<<<<<<< Updated upstream
            log("[BLE]\tSearching for BLE DEVICe...");
=======
            log("[BLE]\tSearching for this_is_the_fucking_BLE...");
>>>>>>> Stashed changes
            this.bleController.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.bleController.removeBLEControllerListener(this);
//        stopHeartBeat();
    }

    @Override
    public void BLEControllerConnected() {
        log("[BLE]\tConnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnectButton.setEnabled(true);
                switchLEDButton.setEnabled(true);
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
                connectButton.setEnabled(true);
            }
        });
        this.isLEDOn = false;
//        stopHeartBeat();
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        log("Device " + name + " found with address " + address);
        this.deviceAddress = address;
        this.connectButton.setEnabled(true);
    }


}