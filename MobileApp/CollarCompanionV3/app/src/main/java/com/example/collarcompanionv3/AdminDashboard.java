package com.example.collarcompanionv3;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import top.defaults.colorpicker.ColorPickerPopup;



public class AdminDashboard extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";
    BluetoothAdapter mBluetoothAdapter;
    private Button BTTest;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver (){
        public void onReceive (Context context, Intent intent){
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: State off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: State on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: turning on");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }

    //LED Section of the Test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Button btnONOFF = (Button) findViewById(R.id.button7);

        //testing connectivity
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisabledBT();
            }
        });

        BTTest = (Button) findViewById(R.id.button9);
        BTTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){

                openActivity3();
            }
        });


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
                                String hexColor = "#" + Integer.toHexString(color).substring(2);
                                btnTEXT.setText(hexColor);
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


    }

    public void openActivity3(){
        Intent intent = new Intent(this, BluetoothTest.class);
        startActivity(intent);
    }

    //Bluetooth Connectivity
    public void enableDisabledBT(){
        if (mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: does not have bt capbbility");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
}