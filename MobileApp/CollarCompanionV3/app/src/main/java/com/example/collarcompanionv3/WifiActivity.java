package com.example.collarcompanionv3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.collarcompanionv3.R;

import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class WifiActivity extends AppCompatActivity {

    private TextView note;
    private TextView TempReading;
    private TextView StepsReading;
    private Button ReqButton;
    private Button TempRecButton;
    private Button StepsRecButton;
    private RequestQueue mQueue;
    public String dataUP;
    public String TempStatus;
    public String StepStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        note = findViewById(R. id. note);
        TempReading = findViewById(R. id. tempReading);
        StepsReading = findViewById(R. id. stepsReading);
        ReqButton = (Button) findViewById(R. id. requestButton);
        TempRecButton = (Button) findViewById(R. id. tempButton);
        StepsRecButton = (Button) findViewById(R. id. stepsButton);
        mQueue= Volley.newRequestQueue(this);

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

        ReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                TextView dataup = (TextView) findViewById(R.id.ReadRequest);
                dataUP = dataup.getText().toString();
                urlreq();
            }
        });

    }

    public void jsonParse1() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1"; //change this with you http request "READ A CHANNEL FEED"
//        String urlfeed = "https://api.thingspeak.com/channels/1502861/fields/2/last.json?api_key=AZBKPYY7B3KKION9";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            TempStatus = feeds.getString("field1");
                            TempReading.setText(TempStatus);
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
        mQueue.add(request);
    }

    public void jsonParse2() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1";
//        String urlfeed = "https://api.thingspeak.com/channels/1502861/fields/2/last.json?api_key=AZBKPYY7B3KKION9";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            StepStatus = feeds.getString("field1");
                            StepsReading.setText(StepStatus);
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
        mQueue.add(request);
    }

    public void urlreq() {

        RequestQueue queue = Volley.newRequestQueue(this);
        final String base = "https://api.thingspeak.com/update?";
        final String api_key = "api_key";
        final String field_3 = "field1";

        // Build up the query URI
        Uri builtURI = Uri.parse(base).buildUpon()
                .appendQueryParameter(api_key, "RX257VSH16FJ63TH") //change this with your write api key
                .appendQueryParameter(field_3, dataUP )
                .build();
        String url = builtURI.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display response string.

                        if (response.equals("0")) {
                            note.setText("Set failed" + "\n" + "try again !!"+ "\n" + "Response Id: "+ response);

                        } else { note.setText("Set success" +"\n" + "Response Id: "+ response);}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                note.setText("no internet");

            }
        });
        queue.add(stringRequest);}
}