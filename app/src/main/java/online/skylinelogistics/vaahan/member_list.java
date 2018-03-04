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

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import online.skylinelogistics.vaahan.firebase.RegistrationService;

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

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(config.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {


                } else {

                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    sendRegistrationToServer(refreshedToken);
                }
            }
        };

        registerReceiver();

        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);

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

        registerReceiver();
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

    private void sendRegistrationToServer(String refreshedToken) {
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(config.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
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
