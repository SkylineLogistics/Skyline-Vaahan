package online.skylinelogistics.vaahan.update;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import online.skylinelogistics.vaahan.MainActivity;
import online.skylinelogistics.vaahan.NotificationUtils;
import online.skylinelogistics.vaahan.config;

public class UpdateService extends Service {

    private Timer timer;
    private TimerTask timerTask;
    private NotificationUtils notificationUtils;

    public UpdateService(Context applicationContext) {
        super();
    }

    public UpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("online.skylinelogistics.vaahan.update.UpdateServiceReceiver");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        check_update();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 5000, 21600000);
    }

    public void check_update() {
        final int[] verCode = new int[1];
        timerTask = new TimerTask() {
            public void run() {
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    verCode[0] = pInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                update_checker(String.valueOf(verCode[0]));
                if(get_update())
                {
                    String uri = get_location();
                    send_update_notification(uri);
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void update_checker(final String vc)
    {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, config.UPDATE_CHECK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server]
                        try {
                            JSONObject res = new JSONObject(response);
                            String status = res.getString("status");

                            if (status.equalsIgnoreCase(config.UPDATE_AVAILABLE)) {
                                //Creating editor to store values to shared preferences
                                SharedPreferences sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                //Adding values to editor
                                editor.putBoolean("update_available", true);
                                editor.putString("update_location",res.getString("location"));

                                //Saving values to editor
                                editor.apply();

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

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private Boolean get_update()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Boolean rv = sharedPreferences.getBoolean("update_available",false);
        return rv;
    }

    private String get_location()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String rv = sharedPreferences.getString("update_location","");
        return rv;
    }

    private void send_update_notification(String uri)
    {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));

        showNotificationMessage(getApplicationContext(),"Vaahan Update","Update Available","",intent);
    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }
}
