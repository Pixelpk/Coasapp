package com.coasapp.coas.roombook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBookingsActivity extends AppCompatActivity implements APPConstants {

    ArrayList<JSONObject> arrayListBookings = new ArrayList<>();
    ArrayList<JSONObject> arrayListSearch = new ArrayList<>();
    BookingHistoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    MyBookingsAdapter myBookingsAdapter;
    String not_id="0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myBookingsAdapter = new MyBookingsAdapter(arrayListSearch, getApplicationContext(), this);
        swipeRefreshLayout = findViewById(R.id.swipe);
        if(getIntent().hasExtra("notify_id"))
            not_id=getIntent().getStringExtra("notify_id");
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                arrayListSearch.clear();
                myBookingsAdapter.notifyDataSetChanged();
                for (int i = 0; i < arrayListBookings.size(); i++) {
                    JSONObject object = arrayListBookings.get(i);
                    try {
                        if (object.getString("book_id").toLowerCase().equals(not_id)||object.getString("room_title").toLowerCase().contains(s.toString()) || object.getString("book_ref").toLowerCase().contains(s.toString())) {
                            arrayListSearch.add(object);
                        }
                        myBookingsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setAdapter(myBookingsAdapter);
        new GetBookings().execute();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetBookings().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 99) {

                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                final String userId = sharedPreferences.getString("userId", "");
                new GetBookings().execute();
            }
        }
    }

    @Override
    public void onBackPressed() {
        /*Intent intent = new Intent();
        intent.putExtra("action", 1);
        setResult(RESULT_OK, intent);*/
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    class GetBookings extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            arrayListBookings.clear();
            arrayListSearch.clear();
            myBookingsAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id",sharedPreferences.getString("userId", "0"));
            map.put("book_id",not_id);
            return new RequestHandler().sendPostRequest(MAIN_URL + "my_bookings.php",map);
            //return new RequestHandler().sendGetRequest(MAIN_URL + "my_bookings.php?user_id=" + sharedPreferences.getString("userId", "0"));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    JSONObject map = new JSONObject();
                    arrayListBookings.add(object);
                    arrayListSearch.add(object);
                }
                myBookingsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
