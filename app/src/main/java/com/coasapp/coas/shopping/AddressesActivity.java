package com.coasapp.coas.shopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressesActivity extends AppCompatActivity implements APPConstants {

    AddressAdapter addressAdapter;
    JSONArray jsonArray;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerViewAddress = findViewById(R.id.recyclerViewAddress);
        swipeRefreshLayout = findViewById(R.id.swipe);
        addressAdapter = new AddressAdapter(jsonObjectArrayList);
        recyclerViewAddress.setAdapter(addressAdapter);
        new GetAddress().execute();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetAddress().execute();
            }
        });

        findViewById(R.id.floatingActionButtonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddAddressActivity.class);
                intent.putExtra("add_id", "0");
                startActivityForResult(intent, 1);
            }
        });

        addressAdapter.setOnItemClick(new AddressAdapter.OnItemClick() {
            @Override
            public void onItemClick(int position) {
                JSONObject object = jsonObjectArrayList.get(position);
                Intent intent = new Intent(getApplicationContext(), EditAddressActivity.class);
                intent.putExtra("details", object.toString());
                startActivityForResult(intent,1);
            }
        });

        addressAdapter.setOnDelClick(new AddressAdapter.OnDelClick() {
            @Override
            public void onDelClick(int position) {
                JSONObject object = jsonObjectArrayList.get(position);
                try {
                    String addId = object.getString("add_id");
                    new DeleteAddress().execute(addId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
        //super.onBackPressed();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                new GetAddress().execute();
            }
        }
    }

    class GetAddress extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            jsonObjectArrayList.clear();
            addressAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String,String> map=new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //return new RequestHandler().sendGetRequest(MAIN_URL + "get_addresses.php?user_id=" + sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_addresses.php",map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONArray jsonArray = new JSONArray(s);

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObjectArrayList.add(jsonArray.getJSONObject(i));
                }
                addressAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class DeleteAddress extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            jsonObjectArrayList.clear();
            addressAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();

            map.put("address_id", strings[0]);
            map.put("option", "delete");
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "edit_address.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    new GetAddress().execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
