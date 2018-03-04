package online.skylinelogistics.vaahan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class vehicle_list extends AppCompatActivity {

    private String member;
    private StringRequest stringRequest;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout srl_vl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        sharedPreferences = getSharedPreferences(config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        Intent i = getIntent();
        member = i.getStringExtra("member");
        srl_vl = (SwipeRefreshLayout) findViewById(R.id.srl_vl);
        srl_vl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stringRequest = new StringRequest(Request.Method.POST, config.VEHICLE_LIST,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                    srl_vl.setRefreshing(false);
                                try {
                                    JSONArray res = new JSONArray(response);
                                    JSONObject tmp;
                                    Button btn;
                                    String vehicle_no;
                                    LinearLayout sv = (LinearLayout) findViewById(R.id.VehicleList);
                                    sv.removeAllViews();
                                    for(int i=0;i<res.length();i++)
                                    {
                                        tmp = res.getJSONObject(i);
                                        btn = new Button(vehicle_list.this);
                                        vehicle_no = tmp.getString("vehicle_no");
                                        btn.setText(vehicle_no);
                                        final String finalVno = vehicle_no;
                                        btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                view_detail(finalVno);
                                            }
                                        });
                                        sv.addView(btn);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(vehicle_list.this, "Can't Connect To Server", Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        //Adding parameters to request
                        params.put("id", member);

                        //returning parameter
                        return params;
                    }
                };

                //Adding the string request to the queue
                requestQueue = Volley.newRequestQueue(vehicle_list.this);
                requestQueue.add(stringRequest);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        stringRequest = new StringRequest(Request.Method.POST, config.VEHICLE_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject tmp;
                            Button btn;
                            String vehicle_no;
                            LinearLayout sv = (LinearLayout) findViewById(R.id.VehicleList);
                            sv.removeAllViews();
                            for(int i=0;i<res.length();i++)
                            {
                                tmp = res.getJSONObject(i);
                                btn = new Button(vehicle_list.this);
                                vehicle_no = tmp.getString("vehicle_no");
                                btn.setText(vehicle_no);
                                final String finalVno = vehicle_no;
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        view_detail(finalVno);
                                    }
                                });
                                sv.addView(btn);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(vehicle_list.this, "Can't Connect To Server", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put("id", member);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void view_detail(String finalVno) {
        Intent i = new Intent(vehicle_list.this,MainActivity.class);
        i.putExtra("vehicle_no",finalVno);
        startActivity(i);
        finish();
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
        Intent intent = new Intent(vehicle_list.this, LoginActivity.class);
        startActivity(intent);
    }
}
