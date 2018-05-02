package online.skylinelogistics.vaahan;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import online.skylinelogistics.vaahan.update.*;

public class launcher extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent update_service_intent;
    private UpdateService update_service;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ctx = this;

        checkFirstRun();

        update_service = new UpdateService(getCtx());
        update_service_intent = new Intent(getCtx(),update_service.getClass());

        if(!isServiceRunning(update_service.getClass()))
        {
            startService(update_service_intent);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals("registrationComplete")) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic("VaahanMessages");
                    Toast.makeText(launcher.this, "Subscribed To Notification Server", Toast.LENGTH_SHORT).show();

                } else if (intent.getAction().equals("pushNotification")) {

                }
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(launcher.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("registrationComplete"));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));
    }

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkFirstRun() {

        final String PREFS_NAME = config.SHARED_PREF_NAME;
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)
            update_confirmation(String.valueOf(currentVersionCode));

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
            update_confirmation(String.valueOf(currentVersionCode));
        }
    }

    private void update_confirmation(final String vc)
    {
        final String PREFS_NAME = config.SHARED_PREF_NAME;
        final String PREF_VERSION_CODE_KEY = "version_code";
        final String DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, config.UPDATE_CONFIRM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server]
                        try {
                            JSONObject res = new JSONObject(response);
                            String status = res.getString("status");

                            if (status.equalsIgnoreCase(config.LOGIN_SUCCESS)) {
                                // Update the shared preferences with the current version code
                                prefs.edit().putInt(PREF_VERSION_CODE_KEY, Integer.parseInt(vc)).apply();

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

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put("vc", vc);
                params.put("device",DeviceID);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
