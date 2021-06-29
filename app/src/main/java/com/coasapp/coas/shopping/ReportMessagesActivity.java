package com.coasapp.coas.shopping;

import android.content.SharedPreferences;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportMessagesActivity extends AppCompatActivity implements APPConstants {

    List<JSONObject> listMsg = new ArrayList<>();
    APICallbacks apiCallbacks;
    APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_messages);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);

        ReportMessagesAdapter reportMessagesAdapter = new ReportMessagesAdapter(this, listMsg);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerView.setAdapter(reportMessagesAdapter);
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        try {
            JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            String orderId = object.getString("order_id");

            getSupportActionBar().setTitle(object.getString("order_track_id") + SPACE + object.getString("order_reason"));
            apiCallbacks = new APICallbacks() {
                @Override
                public void taskStart() {
                    swipeRefreshLayout.setRefreshing(true);
                }

                @Override
                public void taskEnd(String type, String response) {
                    swipeRefreshLayout.setRefreshing(false);
                    try {

                        JSONObject object = new JSONObject(response);
                        if (type.equalsIgnoreCase("send_msg")) {

                            if (object.getString("response_code").equalsIgnoreCase("1")) {
                                editTextMessage.setText("");
                                callGetMsg(sharedPreferences.getString("userId", "0"), orderId);
                            }
                        } else if (type.equalsIgnoreCase("get_msg")) {
                            listMsg.clear();
                            reportMessagesAdapter.notifyDataSetChanged();
                            JSONArray array = object.getJSONArray("messages");
                            for (int i = 0; i < array.length(); i++) {
                                listMsg.add(array.getJSONObject(i));
                            }

                            reportMessagesAdapter.notifyDataSetChanged();
                            if (listMsg.size() > 0)
                                recyclerView.smoothScrollToPosition(listMsg.size() - 1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            apiService = new APIService(apiCallbacks, getApplicationContext());
            findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextMessage.clearFocus();
                    String msg = editTextMessage.getText().toString();
                    if (msg.equalsIgnoreCase("")) {
                        APPHelper.showToast(getApplicationContext(), "Enter message");
                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("order_id", orderId);
                        map.put("user_id", sharedPreferences.getString("userId", "0"));
                        map.put("user_type", getIntent().getStringExtra("role"));
                        map.put("message", msg);
                        apiService.callAPI(map, MAIN_URL + "send_report_message.php", "send_msg");
                    }
                }
            });
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    callGetMsg(sharedPreferences.getString("userId", "0"), orderId);

                }
            });
            callGetMsg(sharedPreferences.getString("userId", "0"), orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void callGetMsg(String userId, String orderId) {
        HashMap<String, String> map = new HashMap<>();

        map.put("userId", userId);
        map.put("order_id", orderId);
        map.put("user_type", getIntent().getStringExtra("role"));
        apiService.callAPI(map, MAIN_URL + "get_report_messages.php", "get_msg");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
