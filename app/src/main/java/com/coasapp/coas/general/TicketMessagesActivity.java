package com.coasapp.coas.general;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicketMessagesActivity extends MyAppCompatActivity {

    List<JSONObject> listTickets = new ArrayList<>();
    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_messages);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        TicketMessagesAdapter messagesAdapter = new TicketMessagesAdapter(this, listTickets);
        recyclerView.setAdapter(messagesAdapter);
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        String ticketId = getIntent().getStringExtra("ticketId");
        getSupportActionBar().setTitle(getIntent().getStringExtra("ticketNo"));
        APICallbacks apiCallbacks = new APICallbacks() {
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
                            callAPI(ticketId);
                        }
                    }
                    if (type.equalsIgnoreCase("messages")) {
                        listTickets.clear();
                        JSONArray array = object.getJSONArray("messages");
                        for (int i = 0; i < array.length(); i++) {
                            listTickets.add(array.getJSONObject(i));
                        }
                        messagesAdapter.notifyDataSetChanged();
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
                    map.put("ticket_id", ticketId);
                    map.put("user_id", sharedPreferences.getString("userId", "0"));
                    map.put("user_type", "user");
                    map.put("message", msg);
                    apiService.callAPI(map, MAIN_URL + "send_ticket_msg.php", "send_msg");
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI(ticketId);
            }
        });
        callAPI(ticketId);

    }


    void callAPI(String ticketId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("ticket_id", ticketId);
        apiService.callAPI(map, MAIN_URL + "get_ticket_messages.php", "messages");
    }
}
