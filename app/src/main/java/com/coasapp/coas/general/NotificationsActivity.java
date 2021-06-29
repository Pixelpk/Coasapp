package com.coasapp.coas.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.bargain.BargainHistoryActivity;
import com.coasapp.coas.bargain.MyBargainRequests;
import com.coasapp.coas.bargain.MyVehiclesActivity;
import com.coasapp.coas.roombook.BookingHistoryActivity;
import com.coasapp.coas.roombook.MyBookingsActivity;
import com.coasapp.coas.roombook.MyRoomsActivity;
import com.coasapp.coas.shopping.BuyerOrdersActivity;
import com.coasapp.coas.shopping.MyProductsActivity;
import com.coasapp.coas.shopping.SellerOrdersActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsActivity extends AppCompatActivity implements APPConstants {

    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    NotificationsAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString("userId", "0");
        swipeRefreshLayout = findViewById(R.id.swipe);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new NotificationsAdapter(getApplicationContext(), jsonObjectArrayList);
        recyclerView.setAdapter(adapter);
        new GetRooms().execute(userId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new GetRooms().execute(userId);
            }
        });

        adapter.setOnItemClick(new NotificationsAdapter.OnItemClick() {
            @Override
            public void onItemClick(int position) {
                JSONObject object = jsonObjectArrayList.get(position);
                try {
                    new MarkAsRead().execute(object.getString("id"));
                    switch (object.getString("type")) {
                        case "booking": {
                            Intent intent = new Intent(getApplicationContext(), MyBookingsActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }

                        case "bookingcustomer": {
                            Intent intent = new Intent(getApplicationContext(), BookingHistoryActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "purchase": {
                            Intent intent = new Intent(getApplicationContext(), BuyerOrdersActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "sale": {
                            Intent intent = new Intent(getApplicationContext(), SellerOrdersActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }

                        case "bargaincustomer": {

                            Intent intent = new Intent(getApplicationContext(), BargainHistoryActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "bargaindriver": {
                            Intent intent = new Intent(getApplicationContext(), MyBargainRequests.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "roomapprove": {
                            Intent intent = new Intent(getApplicationContext(), MyRoomsActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "productapprove": {
                            Intent intent = new Intent(getApplicationContext(), MyProductsActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                        case "vehicleapproved": {
                            Intent intent = new Intent(getApplicationContext(), MyVehiclesActivity.class);
                            intent.putExtra("trans", "rm");

                            startActivity(intent);
                            break;
                        }
                        case "Common": {
                            Intent intent = new Intent(getApplicationContext(), NotificationDetail.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("details", object.toString());
                            startActivity(intent);
                            break;
                        }
                        case "ticketreply": {
                            Intent intent = new Intent(getApplicationContext(), TicketListActivity.class);
                            intent.putExtra("trans", "rm");
                            intent.putExtra("details", object.toString());
                            intent.putExtra("notify_id", object.getString("notify_id"));
                            startActivity(intent);
                            break;
                        }
                    }
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

    class GetRooms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            jsonObjectArrayList.clear();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", strings[0]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_notifications.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            APPHelper.showLog("Notify", s);
            try {

         //       Toast.makeText(NotificationsActivity.this, s, Toast.LENGTH_SHORT).show();

                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    jsonObjectArrayList.add(object);
                }

                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class MarkAsRead extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("not_id", strings[0]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_read_count.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            APPHelper.showLog("Notify", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    int unread = sharedPreferences.getInt("unread", 0);
                    //Toast.makeText(NotificationsActivity.this, "" + unread, Toast.LENGTH_SHORT).show();
                    editor.putInt("unread", unread - 1);
                    editor.apply();
                }
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
