package com.coasapp.coas.bargain;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

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

public class BargainHistoryActivity extends AppCompatActivity implements APPConstants {

    SwipeRefreshLayout swipeRefreshLayout;
    List<JSONObject> listTrips = new ArrayList<>();
    MyTripsAdapter adapter;
    String userId = "0";
    SharedPreferences sharedPreferences;
    String bargainId = "0", driverId;
    APICallbacks apiCallbacks;
    APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bargain_history);
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "0");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Trips");
        swipeRefreshLayout = findViewById(R.id.swipe);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new MyTripsAdapter(listTrips, this, getApplicationContext());
        recyclerView.setAdapter(adapter);
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
            if (intent.hasExtra("bargain_id")) {
                bargainId = getIntent().getStringExtra("bargain_id");
            }
        }

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
                    if (type.equalsIgnoreCase("cancel")) {
                        if (object.getString("response_code").equalsIgnoreCase("1")) {
                            APPHelper.showToast(getApplicationContext(), "You have cancelled the trip");
                            new GetTrips().execute();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        apiService = new APIService(apiCallbacks, getApplicationContext());
        new GetTrips().execute();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetTrips().execute();
            }
        });
        adapter.setOnItemSelected(new MyTripsAdapter.OnItemSelected() {
            @Override
            public void onItemSelected(int position) {
                JSONObject object = listTrips.get(position);
                APPHelper.showLog("Item", "item");
                Intent intent = new Intent(BargainHistoryActivity.this, BargainDetailActivity.class);
                intent.putExtra("details", object.toString());
                intent.putExtra("role", "customer");
                startActivityForResult(intent, 1);
            }
        });

        adapter.setOnTextViewRatingClicked(new MyTripsAdapter.OnTextViewRatingClicked() {
            @Override
            public void onRatingClicked(int i) {
                JSONObject object = listTrips.get(i);
                try {
                    bargainId = object.getString("bargain_id");
                    driverId = object.getString("bargain_driver_id");
                    showRatingDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        adapter.setOnAdapterViewsClicked(new MyTripsAdapter.OnAdapterViewsClicked() {
            @Override
            public void onCancelClicked(int i) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked


                                try {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("bargain_id", listTrips.get(i).getString("bargain_id"));
                                    map.put("user_id", userId);
                                    apiService.callAPI(map, MAIN_URL + "cancel_bargain.php", "cancel");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                AlertDialog.Builder builder = new AlertDialog.Builder(BargainHistoryActivity.this);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(BargainHistoryActivity.this);
                builder.setMessage("Reviewed client’s cancellation regulation in the USER TERMS & CONDITIONS?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
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
        if (resultCode == RESULT_OK && requestCode == 1) {
            new GetTrips().execute();
        }
    }

    class GetTrips extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            listTrips.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = MAIN_URL + "my_trips.php";
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("bargain_id", bargainId);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONArray arrayTrips = new JSONArray(s);
                for (int i = 0; i < arrayTrips.length(); i++) {
                    JSONObject objectTrip = arrayTrips.getJSONObject(i);
                    listTrips.add(objectTrip);
                }
                adapter.notifyDataSetChanged();
                if (arrayTrips.length() > 0) {
                    if (!bargainId.equals("0")) {
                        adapter.onItemSelected.onItemSelected(0);
                    }
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

    public void showRatingDialog() {
        LayoutInflater li = LayoutInflater.from(BargainHistoryActivity.this);
        View confirmDialog = li.inflate(R.layout.dialog_bargain_rating, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box


        //Creating an alertdialog builder
        AlertDialog.Builder alert = new AlertDialog.Builder(BargainHistoryActivity.this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);
        final AlertDialog alertDialog = alert.create();

        alertDialog.setCanceledOnTouchOutside(false);
        //Creating a LayoutInflater object for the dialog box

        final Button buttonConfirm = confirmDialog.findViewById(R.id.buttonSend);
        RatingBar ratingBar = confirmDialog.findViewById(R.id.ratingBarSend);
        //Creating an alertdialog builder

       /* WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        alertDialog.getWindow().setAttributes(lp);*/
        // initiateVerification(false);
        alertDialog.show();
    }
}
