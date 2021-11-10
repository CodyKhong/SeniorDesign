package com.example.collarcompanionv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.BroadcastReceiver;
//import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import top.defaults.colorpicker.ColorPickerPopup;

import android.net.Uri;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AdminDashboard extends AppCompatActivity implements BLEControllerListener{
    private TextView logView;
    private Button connectButton;
    private Button disconnectButton;
    private Button switchLEDButton;
    private Button sendButton;

    private Button TempReqButton;
    private Button StepsReqButton;
    private Button TempRecButton;
    private Button StepsRecButton;
    private RequestQueue mQueue;


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

        TempReqButton = (Button) findViewById(R. id. tempReqButton);
        TempRecButton = (Button) findViewById(R. id. tempRecButton);
        StepsReqButton = (Button) findViewById(R. id. stepsReqbutton);
        StepsRecButton = (Button) findViewById(R. id. stepsRecButton);
        mQueue= Volley.newRequestQueue(this);

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
                                remoteControl.LEDSend(color);
                                byte RedByte = (byte)((color>>16) & 0xFF);
                                String R = String.format("%8s", Integer.toBinaryString(RedByte & 0xFF)).replace(' ', '0');
                                byte GreenByte = (byte)((color>>8) & 0xFF);
                                String G = String.format("%8s", Integer.toBinaryString(GreenByte & 0xFF)).replace(' ', '0');
                                byte BlueByte = (byte)(color & 0xFF);
                                String B = String.format("%8s", Integer.toBinaryString(BlueByte & 0xFF)).replace(' ', '0');
                                log("LED switched to: " + hexColor);
//                                log("Red: "+ R);
//                                log("Green: "+ G);
//                                log("Blue: "+ B);
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

        TempRecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                jsonParse1();
            }
        });

        StepsRecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                jsonParse2();
            }
        });

        TempReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                urlreq1();
            }
        });

        StepsReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                urlreq2();
            }
        });


//        Button TempReqLED = (Button) findViewById(R.id.TempReqbutton);
//        TempReqLED.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                remoteControl.TempRequest();
//            }
//        });

    }

    public void jsonParse1() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1"; //change this with you http request "READ A CHANNEL FEED"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            String TempValue = feeds.getString("field1");
                            log("Temperature is: " + TempValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request); }

    public void jsonParse2() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1"; //change this with you http request "READ A CHANNEL FEED"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            String AccValue = feeds.getString("field2");
                            log("Temperature is: " + AccValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);}

    public void urlreq1() {

        RequestQueue queue = Volley.newRequestQueue(this);
        final String base = "https://api.thingspeak.com/update?";
        final String api_key = "api_key";
        final String field_3 = "field3";

        // Build up the query URI
        Uri builtURI = Uri.parse(base).buildUpon()
                .appendQueryParameter(api_key, "RX257VSH16FJ63TH") //change this with your write api key
                .appendQueryParameter(field_3, "1" )
                .build();
        String url = builtURI.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display response string.

                        if (response.equals("0")) {
                            log("Set failed" + "\n" + "try again !!"+ "\n" + "Response Id: "+ response);

                        } else { log("Set success" +"\n" + "Response Id: "+ response);}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log("no internet");

            }
        });
        queue.add(stringRequest);}

    public void urlreq2() {

        RequestQueue queue = Volley.newRequestQueue(this);
        final String base = "https://api.thingspeak.com/update?";
        final String api_key = "api_key";
        final String field_3 = "field3";

        // Build up the query URI
        Uri builtURI = Uri.parse(base).buildUpon()
                .appendQueryParameter(api_key, "RX257VSH16FJ63TH") //change this with your write api key
                .appendQueryParameter(field_3, "2" )
                .build();
        String url = builtURI.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display response string.

                        if (response.equals("0")) {
                            log("Set failed" + "\n" + "try again !!"+ "\n" + "Response Id: "+ response);

                        } else { log("Set success" +"\n" + "Response Id: "+ response);}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log("no internet");

            }
        });
        queue.add(stringRequest);}
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

//    private void initGetTempButton(){
//        this.remoteControl.DATAREAD();
//    }
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
            log("[BLE]\tSearching for Collar Companion...");
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