package online.skylinelogistics.vaahan;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView vehicle_view;
    private boolean loggedIn = false;
    private String DeviceID;
    private SharedPreferences sharedPreferences;
    private TextView step_last_view;
    private String stage;
    private LinearLayout status_view;
    private LayoutInflater inflater;
    private View inflated_view;
    private RequestQueue requestQueue;
    private String step_last;
    private StringRequest stringRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;
    private double latitude;
    private double longitude;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private String vno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        status_view = (LinearLayout) findViewById(R.id.StatusView);

        Intent i = getIntent();
        vno = i.getStringExtra("vehicle_no");
        vehicle_view = (TextView) findViewById(R.id.vehicle);
        step_last_view = (TextView) findViewById(R.id.previous_update);

    }

    @Override
    protected void onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View svc;
        ViewGroup parent;

        registerReceiver();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        step_last = sharedPreferences.getString("step_last", "Not Available");

        DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        stringRequest = new StringRequest(Request.Method.POST, config.VEHICLE_DETAIL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server]
                        try {
                            JSONObject res = new JSONObject(response);
                            String status = res.getString("status");

                            if (status.equalsIgnoreCase(config.LOGIN_SUCCESS)) {
                                //Creating editor to store values to shared preferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                //Adding values to editor
                                editor.putString(config.VEHICLE_SHARED_PREF, res.getString("vehicle_no"));
                                editor.putString(config.TRIP_STAGE, res.getString("stage"));
                                editor.putString(config.RESPONSE_PREVIOUS_STEP, res.getString("step_last"));

                                //Saving values to editor
                                editor.commit();

                                update_views(res);
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Can't Connect To Server", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put("vno", vno);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        stage = sharedPreferences.getString("stage", "Not Available");


        vehicle_view.setText("Vehicle No.: " + vno);
        step_last_view.setText("Last Step: " + step_last);

        switch (stage) {
            case "0":
                svc = findViewById(R.id.StatusViewChild);
                parent = (ViewGroup) svc.getParent();
                parent.removeView(svc);
                inflated_view = inflater.inflate(R.layout.stage_1, null, false);
                status_view.addView(inflated_view);
                (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("breakdown");
                    }
                });
                (findViewById(R.id.stage_1_0)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("loading_advice");
                    }
                });
                (findViewById(R.id.stage_1_1)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("factory_entry");
                    }
                });
                (findViewById(R.id.stage_1_2)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("loaded");
                    }
                });
                (findViewById(R.id.stage_1_3)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("paper_received");
                    }
                });
                (findViewById(R.id.stage_1_4)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("trip_start");
                    }
                });
                break;
            case "1":
                svc = findViewById(R.id.StatusViewChild);
                parent = (ViewGroup) svc.getParent();
                parent.removeView(svc);
                inflated_view = inflater.inflate(R.layout.stage_2, null, false);
                status_view.addView(inflated_view);
                if (step_last.equalsIgnoreCase("Sample")) {
                    (findViewById(R.id.stage_2_2)).setVisibility(View.VISIBLE);
                }
                (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("breakdown");
                    }
                });
                (findViewById(R.id.stage_2_0)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("destination_reached");
                    }
                });
                (findViewById(R.id.stage_2_1)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("gate_in");
                    }
                });
                (findViewById(R.id.sample_pass)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("sample_pass");
                    }
                });
                (findViewById(R.id.sample_fail)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("sample_fail");
                    }
                });
                (findViewById(R.id.stage_2_3)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("unloading_started");
                    }
                });
                (findViewById(R.id.stage_2_4)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("unloaded");
                    }
                });
                (findViewById(R.id.sample)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("sample");
                    }
                });
                break;
            case "2":
                svc = findViewById(R.id.StatusViewChild);
                parent = (ViewGroup) svc.getParent();
                parent.removeView(svc);
                inflated_view = inflater.inflate(R.layout.stage_3, null, false);
                status_view.addView(inflated_view);
                (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("breakdown");
                    }
                });
                (findViewById(R.id.stage_3_0)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("receiving_received");
                    }
                });
                (findViewById(R.id.stage_3_1)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("free");
                    }
                });
                break;
            default:
                svc = findViewById(R.id.StatusViewChild);
                if(svc != null) {
                    parent = (ViewGroup) svc.getParent();
                    parent.removeView(svc);
                }
        }

        vehicle_view = (TextView) findViewById(R.id.vehicle);
        step_last_view = (TextView) findViewById(R.id.previous_update);


        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String vno = sharedPreferences.getString(config.VEHICLE_SHARED_PREF, "Not Available");
        String step_last = sharedPreferences.getString(config.RESPONSE_PREVIOUS_STEP, "Not Available");


        //In onresume fetching value from sharedpreference
        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        //Fetching the boolean value form sharedpreferences
        loggedIn = sharedPreferences.getBoolean(config.LOGGEDIN_SHARED_PREF, false);

        //If we will get true
        if (!loggedIn) {
            //We will start the Profile Activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    //Logout function
    private void logout() {
        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //Getting editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Puting the value false for loggedin
        editor.putBoolean(config.LOGGEDIN_SHARED_PREF, false);

        //Putting blank value to email
        editor.putString(config.VEHICLE_SHARED_PREF, "");

        //Saving the sharedpreferences
        editor.commit();

        //Starting login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Adding our menu to toolbar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuLogout) {
            //calling logout method when the logout button is clicked
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void sendStatus(final String status) {

        Toast.makeText(MainActivity.this,"Updating Status",Toast.LENGTH_LONG).show();
        SharedPreferences preferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String supervisor = preferences.getString("ID","Not Available");

       latitude = 0; //mLastKnownLocation.getLatitude();
       longitude = 0; //mLastKnownLocation.getLongitude();

        stringRequest = new StringRequest(Request.Method.POST, config.STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //If we are getting success from server
                        JSONObject res = null;
                        try {
                            res = new JSONObject(response);
                        String trip_status = res.getString("status");
                        if(trip_status.equalsIgnoreCase(config.STATUS_SUCCESS)) {
                            //Creating a shared preference
                            SharedPreferences sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                            //Creating editor to store values to shared preferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Adding values to editor
                            editor.putString(config.TRIP_STAGE, res.getString("stage"));
                            editor.putString(config.RESPONSE_PREVIOUS_STEP,res.getString("step_last"));

                            //Saving values to editor
                            editor.commit();

                            update_views(res);
                        }

                        else{
                            //If the server response is not success
                            //Displaying an error message on toast
                            Toast.makeText(MainActivity.this, "Can't Update Status, Logged In From Another Device", Toast.LENGTH_LONG).show();
                            sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME,Context.MODE_PRIVATE);
                            //Getting editor
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Puting the value false for loggedin
                            editor.putBoolean(config.LOGGEDIN_SHARED_PREF, false);

                            //Putting blank value to email
                            editor.putString(config.VEHICLE_SHARED_PREF, "");

                            //Saving the sharedpreferences
                            editor.commit();

                            //Starting login activity
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);

                        } }catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Can't Connect To Server", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put("device_id", DeviceID);
                params.put("stage",sharedPreferences.getString(config.TRIP_STAGE, "Not Available"));
                params.put("step_update", status);
                params.put("vno",sharedPreferences.getString(config.VEHICLE_SHARED_PREF, "Not Available"));
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("supervisor",supervisor);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void update_views(JSONObject res) throws JSONException {

        View svc;
        ViewGroup parent;

        step_last_view.setText("Last Step: "+res.getString("step_last"));

        vehicle_view.setText("Vehicle No.: " + vno);

        stage = res.getString("stage");

        status_view = (LinearLayout) findViewById(R.id.StatusView);
        inflater = LayoutInflater.from(MainActivity.this);
        switch(stage) {
            case "0":
                svc = findViewById(R.id.StatusViewChild);
                if (svc != null) {
                    parent = (ViewGroup) svc.getParent();
                    parent.removeView(svc);
                }
                parent = findViewById(R.id.StatusView);
                    inflated_view = inflater.inflate(R.layout.stage_1, null, false);
                    parent.addView(inflated_view);
                (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStatus("breakdown");
                    }
                });
                    (findViewById(R.id.stage_1_0)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendStatus("loading_advice");
                        }
                    });
                    (findViewById(R.id.stage_1_1)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendStatus("factory_entry");
                        }
                    });
                    (findViewById(R.id.stage_1_2)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendStatus("loaded");
                        }
                    });
                    (findViewById(R.id.stage_1_3)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendStatus("paper_received");
                        }
                    });
                    (findViewById(R.id.stage_1_4)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendStatus("trip_start");
                        }
                    });
                    break;
                    case "1":
                        svc = findViewById(R.id.StatusViewChild);
                        if (svc != null) {
                            parent = (ViewGroup) svc.getParent();
                            parent.removeView(svc);
                        }
                        parent = findViewById(R.id.StatusView);
                            inflated_view = inflater.inflate(R.layout.stage_2, null, false);
                            parent.addView(inflated_view);
                            if (res.getString("step_last").equalsIgnoreCase("Gone For Sample")) {
                                (findViewById(R.id.stage_2_2)).setVisibility(View.VISIBLE);
                            }
                        (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendStatus("breakdown");
                            }
                        });
                            (findViewById(R.id.stage_2_0)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("destination_reached");
                                }
                            });
                            (findViewById(R.id.stage_2_1)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("gate_in");
                                }
                            });
                            (findViewById(R.id.sample_pass)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("sample_pass");
                                }
                            });
                            (findViewById(R.id.sample_fail)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("sample_fail");
                                }
                            });
                            (findViewById(R.id.stage_2_3)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("unloading_started");
                                }
                            });
                            (findViewById(R.id.stage_2_4)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("unloaded");
                                }
                            });
                            (findViewById(R.id.sample)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("sample");
                                }
                            });
                        break;
                    case "2":
                        svc = findViewById(R.id.StatusViewChild);
                        if (svc != null) {
                            parent = (ViewGroup) svc.getParent();
                            parent.removeView(svc);
                        }
                            parent = findViewById(R.id.StatusView);
                            inflated_view = inflater.inflate(R.layout.stage_3, null, false);
                            parent.addView(inflated_view);
                        (findViewById(R.id.breakdown_btn)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendStatus("breakdown");
                            }
                        });
                            (findViewById(R.id.stage_3_0)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("receiving_received");
                                }
                            });
                            (findViewById(R.id.stage_3_1)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendStatus("free");
                                }
                            });
                            break;
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(config.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private void sendRegistrationToServer(final String refreshedToken) {
    }

}
