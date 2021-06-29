package com.coasapp.coas.general;

import android.content.Intent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicketListActivity extends MyAppCompatActivity implements APPConstants {

    List<JSONObject> listTicket = new ArrayList<>();
    List<JSONObject> listTicketS = new ArrayList<>();

    TicketListAdapter ticketListAdapter = new TicketListAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        EditText editTextSearch = findViewById(R.id.editTextSearch);

        ticketListAdapter.setList(listTicketS);

        ticketListAdapter.setActivity(this);
        recyclerView.setAdapter(ticketListAdapter);
        ticketListAdapter.setOnItemClick(new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                JSONObject object = listTicketS.get(position);
                Intent intent = new Intent(getApplicationContext(), TicketMessagesActivity.class);
                try {
                    intent.putExtra("ticketId", object.getString("ticket_id"));
                    intent.putExtra("ticketNo", object.getString("ticket_no"));
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.floatingActionButtonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NewTicketActivity.class));
            }
        });
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
                    if (type.equalsIgnoreCase("tickets")) {
                        listTicketS.clear();
                        listTicket.clear();
                        JSONArray array = object.getJSONArray("tickets");
                        for (int i = 0; i < array.length(); i++) {
                            listTicket.add(array.getJSONObject(i));
                            listTicketS.add(array.getJSONObject(i));
                        }

                        ticketListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        callAPI(apiService);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI(apiService);
            }
        });
    }

    void callAPI(APIService apiService) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", getSharedPreferences(APP_PREF, 0).getString("userId", "0"));
        apiService.callAPI(map, MAIN_URL + "get_ticket_list.php", "tickets");
    }

    void filter(String search) {
        listTicketS.clear();
        for (int i = 0; i < listTicket.size(); i++) {
            JSONObject object = listTicket.get(i);
            try {
                if (object.getString("ticket_no").toLowerCase().contains(search.toLowerCase()) || object.getString("ticket_title").toLowerCase().contains(search.toLowerCase())) {
                    listTicketS.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ticketListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
