package com.coasapp.coas.roombook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.general.COASHomeActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class BookingHistoryActivity extends AppCompatActivity implements APPConstants {

    ArrayList<JSONObject> arrayListBookings = new ArrayList<>();
    BookingHistoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<JSONObject> arrayListSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                APPHelper.showLog("Search", s.toString());
                arrayListSearch.clear();
                adapter.notifyDataSetChanged();
                for (int i = 0; i < arrayListBookings.size(); i++) {
                    JSONObject object = arrayListBookings.get(i);
                    try {
                        if (object.getString("name").toLowerCase().contains(s.toString()) || object.getString("room_title").toLowerCase().contains(s.toString()) || object.getString("book_ref").toLowerCase().contains(s.toString())) {
                            arrayListSearch.add(object);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();

            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString("userId", "");
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new BookingHistoryAdapter(arrayListSearch, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
        new GetBookings().execute(userId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new GetBookings().execute(userId);
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
                new GetBookings().execute(userId);
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("checkout", false)) {
            Intent intent = new Intent(getApplicationContext(), COASHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("action", getIntent().getIntExtra("action", 0));
            setResult(RESULT_OK, intent);
            finish();
            //super.onBackPressed();
        }

    }

    class GetBookings extends AsyncTask<String, Void, String> {
String bookId ="0";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arrayListBookings.clear();
            arrayListSearch.clear();
            adapter.notifyDataSetChanged();

        }

        @Override
        protected String doInBackground(String... strings) {
            if(getIntent().hasExtra("notify_id")){
                bookId = getIntent().getStringExtra("notify_id");
            }
            HashMap<String, String> map = new HashMap<>();

            map.put("user_id", strings[0]);
            map.put("book_id", bookId);
            return new RequestHandler().sendPostRequest(MAIN_URL + "room_book_history.php", map);

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
                adapter.notifyDataSetChanged();
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
