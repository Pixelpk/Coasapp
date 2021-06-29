package com.coasapp.coas.bargain;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyBargainRequests extends AppCompatActivity implements APPConstants {

    String[] time = {
            "8.00 am",
            "8.30 am",
            "9.00 am",
            "9.30 am",
            "10.00 am"};
    String[] name = {
            "Peter Parker",
            "Tony Stark",
            "Alex Pantiyan",
            "Bruce Wayne",
            "Clark Kent"};
    String[] date = {"12/10/2018", "10/10/2018", "02/10/2018", "25/09/2018", "10/09/2018"};
    int pos = 0;
    String uid = "";
    String did = "";
String bargainId="0";
    private List<JSONObject> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private ItemListAdapter mAdapter;
    String bargin_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        mAdapter = new ItemListAdapter(list, this, getApplicationContext());

        recyclerView.setHasFixedSize(true);

        // vertical RecyclerView
        // keep movie_list_row.xml width to `match_parent`
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        // horizontal RecyclerView
        // keep movie_list_row.xml width to `wrap_content`
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        // adding inbuilt divider line
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // adding custom divider line with padding 16dp
        // recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);
        Intent intent = getIntent();
        if (intent != null) {
            /*Uri deepLink = intent.getData();
            //Toast.makeText(getApplicationContext(), deepLink.toString(), Toast.LENGTH_LONG).show();

            if (deepLink != null) {
                if (!sharedPreferences.getBoolean("loggedIn", false)) {
                    Intent intent1 = new Intent(getApplicationContext(), COASLoginActivity.class);
                    startActivity(intent1);
                    finish();
                } else {
                    bargainId = deepLink.toString().substring(deepLink.toString().lastIndexOf("/") + 1);
                }
            }*/

            if(intent.hasExtra("bargain_id")){
                bargainId= getIntent().getStringExtra("bargain_id");
            }
        }
        new BuyerOrders().execute();
        // row click listener

        mAdapter.setOnItemSelected(new ItemListAdapter.OnItemSelected() {
            @Override
            public void onItemSelected(int position) {

                JSONObject object = list.get(position);



                APPHelper.showLog("Item", "item");
                try {
                    uid = object.getString("bargain_user_id");
                    did = object.getString("bargain_driver_id");
                    bargin_status = object.getString("bargain_status");
     //               Toast.makeText(MyBargainRequests.this, bargin_status, Toast.LENGTH_SHORT).show();

                   /* Toast.makeText(MyBargainRequests.this, uid, Toast.LENGTH_SHORT).show();
                    Toast.makeText(MyBargainRequests.this, did, Toast.LENGTH_SHORT).show();*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
       //         Toast.makeText(MyBargainRequests.this, sharedPreferences.getString("userId", "0"), Toast.LENGTH_SHORT).show();
       //         Toast.makeText(MyBargainRequests.this, did, Toast.LENGTH_SHORT).show();

                if(bargin_status.equals("PaymentPending"))
                {

                    Intent intent = new Intent(MyBargainRequests.this, BargainDetailActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("status", bargin_status);
                    intent.putExtra("payment", "yes");
                    intent.putExtra("did", did);
                    intent.putExtra("details", object.toString());
                    intent.putExtra("role", "driver");
                    startActivityForResult(intent, 99);

                }
                else
                {
                    Intent intent = new Intent(MyBargainRequests.this, BargainDetailActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("status", bargin_status);
                    intent.putExtra("payment", "no");
                    intent.putExtra("did", did);
                    intent.putExtra("details", object.toString());
                    intent.putExtra("role", "driver");
                    startActivityForResult(intent, 99);
                }



            }
        });

        mAdapter.setOnImageSelected(new ItemListAdapter.OnImageSelected() {
            @Override
            public void onClick(int position, String s) {
                JSONObject object = list.get(position);
                try {
                    String tripId = object.getString("bargain_id");
                    String driverID = object.getString("bargain_driver_id");
                    new UpdateTrip().execute(tripId, driverID, s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ((SwipeRefreshLayout) findViewById(R.id.swipe)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new BuyerOrders().execute();
            }
        });

        prepareMovieData();


       APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {

            }

            @Override
            public void taskEnd(String type, String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (type.equalsIgnoreCase("cancel")) {
                        if (object.getString("response_code").equalsIgnoreCase("1")) {
                            APPHelper.showToast(getApplicationContext(),"You have cancelled the trip");
                            new BuyerOrders().execute();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
     APIService   apiService = new APIService(apiCallbacks, getApplicationContext());

        mAdapter.setOnAdapterViewsClicked(new ItemListAdapter.OnAdapterViewsClicked() {
            @Override
            public void onCancelClicked(int i) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked


                                try {
                                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                                    String userId = sharedPreferences.getString("userId", "0");
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("bargain_id", list.get(i).getString("bargain_id"));
                                    map.put("user_id", userId);
                                    apiService.callAPI(map, MAIN_URL + "cancel_bargain.php", "cancel");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                AlertDialog.Builder builder = new AlertDialog.Builder(MyBargainRequests.this);
                                builder.setMessage("Please contact COASAPP").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                        .show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MyBargainRequests.this);
                builder.setMessage("Reviewed client’s cancellation regulation in the USER TERMS & CONDITIONS?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    private void prepareMovieData() {
        mAdapter.notifyDataSetChanged();
    }

    class BuyerOrders extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* Toast.makeText(MyBargainRequests.this, userId, Toast.LENGTH_SHORT).show();
            Toast.makeText(MyBargainRequests.this, bargainId , Toast.LENGTH_SHORT).show();*/

            ((SwipeRefreshLayout) findViewById(R.id.swipe)).setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String url = MAIN_URL + "get_my_bargains.php";

            APPHelper.showLog("Url", url);
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("bargain_id", bargainId);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((SwipeRefreshLayout) findViewById(R.id.swipe)).setRefreshing(false);
            try {
           //     Toast.makeText(MyBargainRequests.this, s, Toast.LENGTH_SHORT).show();
                list.clear();
                mAdapter.notifyDataSetChanged();
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.add(object);
                }
                mAdapter.notifyDataSetChanged();
                if(jsonArray.length()>0){
                    if(!bargainId.equals("0")){
                        mAdapter.onItemSelected.onItemSelected(0);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 99)
                new BuyerOrders().execute();

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    class UpdateTrip extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((SwipeRefreshLayout) findViewById(R.id.swipe)).setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            String url = MAIN_URL + "update_trip_status.php";
            map.put("bargain_id", strings[0]);
            map.put("driver_id", strings[1]);
            map.put("status", strings[2]);
            APPHelper.showLog("Url", url);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((SwipeRefreshLayout) findViewById(R.id.swipe)).setRefreshing(false);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    new BuyerOrders().execute();
                }
                /*JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.add(object);
                }
                mAdapter.notifyDataSetChanged();*/
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