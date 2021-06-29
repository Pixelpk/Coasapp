package com.coasapp.coas.general;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

public class NewTicketActivity extends MyAppCompatActivity implements APPConstants {
    List<JSONObject> lista = new ArrayList<>();
    List<String> listOrders = new ArrayList<>();
    APICallbacks apiCallbacks;
    APIService apiService;
    String cat = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        Spinner spinnerTicketCat = findViewById(R.id.spinnerCat);
        Spinner spinnerOrderId = findViewById(R.id.spinnerOrderId);
        EditText editTextSub = findViewById(R.id.editTextSub);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOrders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderId.setAdapter(adapter);
        findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cat = spinnerTicketCat.getSelectedItem().toString();
                String orderId = spinnerOrderId.getSelectedItem().toString();
                String sub = editTextSub.getText().toString().trim();
                if (orderId.equalsIgnoreCase("select"))
                    APPHelper.showToast(getApplicationContext(), "select order id");
                else if (sub.equalsIgnoreCase(""))
                    APPHelper.showToast(getApplicationContext(), "enter subject");
                else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id", sharedPreferences.getString("userId", ""));
                    map.put("category", cat);
                    map.put("order_id", orderId);
                    map.put("email", sharedPreferences.getString("email", ""));
                    map.put("title", sub);
                    apiService.callAPI(map, MAIN_URL + "raise_ticket.php", "ticket");
                }

            }
        });
        spinnerTicketCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    cat = spinnerTicketCat.getSelectedItem().toString();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id", sharedPreferences.getString("userId", "0"));
                    map.put("type", cat);
                    apiService.callAPI(map, MAIN_URL + "get_orders.php", "orders");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {

            }

            @Override
            public void taskEnd(String type, String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);

                    if (type.equals("orders")) {

                        listOrders.clear();
                        lista.clear();
                        listOrders.add("Select");

                        JSONArray array = object.getJSONArray("orders");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            if (cat.equalsIgnoreCase("renting")) {
                                object1.put("order_id", object1.getString("book_ref"));
                            } else if (cat.equalsIgnoreCase("shopping")) {
                                object1.put("order_id", object1.getString("order_track_id"));
                            } else if (cat.equalsIgnoreCase("bargain")) {
                                object1.put("order_id", object1.getString("bargain_ref"));
                            }

                            lista.add(object1);
                            listOrders.add(object1.getString("order_id"));

                        }
                        Log.i("Ordres", listOrders.toString());
                        adapter.notifyDataSetChanged();

                    }

                    if (type.equalsIgnoreCase("ticket")) {
                        if (object.getString("response_code").equalsIgnoreCase("1")) {
                            Intent intent = new Intent(getApplicationContext(), TicketMessagesActivity.class);
                            try {
                                intent.putExtra("ticketId", object.getString("ticket_id"));
                                intent.putExtra("ticketNo", object.getString("ticket_no"));
                                startActivity(intent);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        apiService = new APIService(apiCallbacks, getApplicationContext());
    }
}
