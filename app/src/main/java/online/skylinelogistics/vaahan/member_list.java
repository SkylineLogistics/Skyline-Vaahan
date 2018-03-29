package online.skylinelogistics.vaahan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class member_list extends AppCompatActivity {


    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private LayoutInflater inflater;
    private String members_json;
    private boolean self;
    private boolean isReceiverRegistered;
    private SharedPreferences sharedPreferences;
    private SwipeRefreshLayout srl_ml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_list);

        inflater = LayoutInflater.from(member_list.this);
        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

       Intent i = getIntent();
       self = i.getBooleanExtra("self",false);
       members_json = i.getStringExtra("members");

       if(members_json == null)
       {
           members_json = sharedPreferences.getString("members","[]");
       }

       if(self == false)
       {
           self = sharedPreferences.getBoolean("self",false);
       }

       srl_ml = (SwipeRefreshLayout) findViewById(R.id.srl_ml);

       srl_ml.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {
               LinearLayout sv = (LinearLayout) findViewById(R.id.MemberList);

               sv.removeAllViews();

               Button btn;

               if(self)
               {
                   final String id = sharedPreferences.getString("ID","Not Available");
                   btn = new Button(member_list.this);
                   btn.setText(id);
                   btn.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           view_vehicle(id);
                       }
                   });

                   sv.addView(btn);
               }

               try {
                   JSONArray res = new JSONArray(members_json);
                   JSONObject tmp;
                   String member_name;

                   for(int j=0;j<res.length();j++)
                   {
                       tmp = res.getJSONObject(j);
                       btn = new Button(member_list.this);
                       member_name = tmp.getString("ID");
                       btn.setText(member_name);
                       final String finalMem = member_name;
                       btn.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               view_vehicle(finalMem);
                           }
                       });
                       sv.addView(btn);
                   }
               } catch (JSONException e) {
                   e.printStackTrace();
               }
               srl_ml.setRefreshing(false);
           }
       });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals("registrationComplete")) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic("VaahanMessages");
                    Toast.makeText(member_list.this,"Subscribed To Notification Server",Toast.LENGTH_SHORT).show();

                } else if (intent.getAction().equals("pushNotification")) {

                }
            }
        };
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
        Intent intent = new Intent(member_list.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(" "));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));

        NotificationUtils.clearNotifications(getApplicationContext());

        LinearLayout sv = (LinearLayout) findViewById(R.id.MemberList);

        sv.removeAllViews();

        Button btn;

        if(self)
        {
            final String id = sharedPreferences.getString("ID","Not Available");
            btn = new Button(this);
            btn.setText(id);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view_vehicle(id);
                }
            });

            sv.addView(btn);
        }

        try {
            JSONArray res = new JSONArray(members_json);
            JSONObject tmp;
            String member_name;

            for(int j=0;j<res.length();j++)
            {
                tmp = res.getJSONObject(j);
                btn = new Button(this);
                member_name = tmp.getString("ID");
                btn.setText(member_name);
                final String finalMem = member_name;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view_vehicle(finalMem);
                    }
                });
                sv.addView(btn);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void view_vehicle(String member)
    {
        Intent i = new Intent(this, vehicle_list.class);
        i.putExtra("member",member);
        startActivity(i);
        finish();
    }
}
