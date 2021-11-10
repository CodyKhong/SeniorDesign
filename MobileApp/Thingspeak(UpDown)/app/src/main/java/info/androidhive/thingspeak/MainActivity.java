package info.androidhive.thingspeak;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private TextView status;
    private TextView acc_status;
    private TextView note;
    private Button download;
    private Button acc_download;
    private Button upload;
    private RequestQueue mQueue;
    public String Status;
    public String acc_Status;
    public String dataUP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R. id. status);
        acc_status = findViewById(R. id. textView2);
        note = findViewById(R. id. note);
        download = (Button) findViewById(R. id. button1);
        upload = (Button) findViewById(R. id. button2);
        acc_download = (Button) findViewById(R. id. button);
        mQueue= Volley.newRequestQueue(this);

        download.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v){
                                          jsonParse();
                                      }
                                  });

        acc_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                jsonParse1();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                EditText dataup = (EditText) findViewById(R.id.editText);
                dataUP = dataup.getText().toString();
                urlreq();
            }
        });
    }

    public void jsonParse() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1"; //change this with you http request "READ A CHANNEL FEED"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            Status = feeds.getString("field1");
                            status.setText(Status);
                            //copy lines 69-70 bt for separate text fields.

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

    public void jsonParse1() {
        String urlfeed = "https://api.thingspeak.com/channels/1502861/feeds.json?api_key=AZBKPYY7B3KKION9&results=1"; //change this with you http request "READ A CHANNEL FEED"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlfeed, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            JSONObject feeds = jsonArray.getJSONObject(0);
                            acc_Status = feeds.getString("field2");
                            acc_status.setText(acc_Status);
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



    public void urlreq() {

        RequestQueue queue = Volley.newRequestQueue(this);
        final String base = "https://api.thingspeak.com/update?";
        final String api_key = "api_key";
        final String field_3 = "field3";

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