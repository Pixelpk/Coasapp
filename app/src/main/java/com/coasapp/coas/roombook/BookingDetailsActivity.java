package com.coasapp.coas.roombook;

import android.content.Intent;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.bargain.VehicleDetailsActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.utils.RequestHandler;

import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookingDetailsActivity extends AppCompatActivity implements APPConstants {

    GuestsAdapter guestsAdapter;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String checkout = "", timeZone = "", checkin = "";
    long unixTime1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Booking Details");
        TextView textViewAddress = findViewById(R.id.textViewRoomAddress);
        TextView textViewRoom = findViewById(R.id.textViewRoom);
        TextView textViewCustomer = findViewById(R.id.textViewCustomer);
        TextView textViewHost = findViewById(R.id.textViewHost);
        TextView textViewBookId = findViewById(R.id.textViewBookID);
        TextView textViewCheckIn = findViewById(R.id.textViewCheckIn);
        TextView textViewCheckout = findViewById(R.id.textViewCheckOut);
        TextView textViewAmount = findViewById(R.id.textViewAmount);
        TextView textViewStatus = findViewById(R.id.textViewStatus);
        TextView textViewEmC = findViewById(R.id.textViewEmContact);
        TextView textViewAdults = findViewById(R.id.textViewAdults);
        TextView textViewChildren = findViewById(R.id.textViewChildren);
        TextView textViewInfants = findViewById(R.id.textViewInfants);
        CircleImageView imageView = findViewById(R.id.imageViewCustomer);
        CircleImageView imageView2 = findViewById(R.id.imageViewHosted);
        Button buttonCheckout = findViewById(R.id.buttonCheckout);
        Button buttonCheckIn = findViewById(R.id.buttonCheckIn);
        RecyclerView recyclerViewGuests = findViewById(R.id.recyclerViewGuests);
        LinearLayout linearLayoutBuyer = findViewById(R.id.buyer);
        LinearLayout linearLayoutSeller = findViewById(R.id.seller);
        guestsAdapter = new GuestsAdapter(jsonObjectArrayList, this, getApplicationContext());
        recyclerViewGuests.setAdapter(guestsAdapter);
        try {
            final JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            timeZone = object.getString("room_timezone");
            TimeZone.setDefault(TimeZone.getTimeZone(timeZone));

            APPHelper.showLog("Book", object.toString());
            textViewBookId.setText("Booking ID: " + object.getString("book_ref"));
            textViewRoom.setText(object.getString("room_title"));
            if (getIntent().getStringExtra("role").equals("buyer")) {
                linearLayoutBuyer.setVisibility(View.GONE);
                linearLayoutSeller.setVisibility(View.VISIBLE);
                textViewHost.setText(object.getString("name"));
                Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageView2);
                APPHelper.showLog("Book", object.getString("name"));
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);

                        try {
                            intent.putExtra("takeOrder", true);

                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);

                            intent.putExtra(ConversationUIService.USER_ID, object.getString("coas_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);*/



                        try {
                            if (!object.getString("coas_id").equals(new MyPrefs(getApplicationContext(), APP_PREF).getString("coasId"))) {
                                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                                new LaunchChatUtils(getApplicationContext(), BookingDetailsActivity.this, new LaunchChatCallbacks() {
                                    @Override
                                    public void onChatCreatedSuccess(Intent intent) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                                LaunchChatUtils.launchChatMessageActivity(BookingDetailsActivity.this,intent);

                                            }
                                        });

                                    }

                                    @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                                        }
                                    });
                                }
                                }).createChatDialog(object.getString("coas_id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            } else {
                linearLayoutBuyer.setVisibility(View.VISIBLE);
                linearLayoutSeller.setVisibility(View.GONE);
                textViewCustomer.setText(object.getString("name"));
                Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            if (!object.getString("coas_id").equals(new MyPrefs(getApplicationContext(), APP_PREF).getString("coasId"))) {
                                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                                new LaunchChatUtils(getApplicationContext(), BookingDetailsActivity.this, new LaunchChatCallbacks() {
                                    @Override
                                    public void onChatCreatedSuccess(Intent intent) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                                LaunchChatUtils.launchChatMessageActivity(BookingDetailsActivity.this,intent);

                                            }
                                        });
                                    }

                                    @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                                        }
                                    });
                                }
                                }).createChatDialog(object.getString("coas_id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                APPHelper.showLog("Book", object.getString("name"));

            }
            textViewRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RoomDetailsActivity.class);

                    intent.putExtra("details", object.toString());
                    startActivity(intent);

                }
            });
            textViewAddress.setText(object.getString("room_street"));
            textViewAmount.setText(formatter.format(Double.valueOf(object.getString("book_amount"))));
            textViewStatus.setText(object.getString("book_status"));
            textViewEmC.setText(object.getString("book_em_name") + " - " + object.getString("book_em_phone"));
            textViewAdults.setText(object.getString("book_adults"));
            textViewChildren.setText(object.getString("book_children"));
            textViewInfants.setText(object.getString("book_infant"));
            textViewRoom.setText(object.getString("room_title"));
            Date date1 = new Date(Long.parseLong(object.getString("book_checkin")) * 1000L);
            Date date2 = new Date(Long.parseLong(object.getString("book_checkout")) * 1000L);

// the format of your date
            textViewCheckIn.setText(sdfNativeDateTime.format(date1));
            textViewCheckout.setText(sdfNativeDateTime.format(date2));
            JSONArray jsonArray = new JSONArray(object.getString("guests"));
            APPHelper.showLog("Guests", jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObjectArrayList.add(jsonArray.getJSONObject(i));
            }
            guestsAdapter.notifyDataSetChanged();
            Calendar calendar = Calendar.getInstance();
            String currentDate = sdfDatabaseDateTime.format(calendar.getTime());
            checkin = String.valueOf(APPHelper.getUnixTimeZone(currentDate, timeZone));
            checkout = String.valueOf(APPHelper.getUnixTimeZone(currentDate, timeZone));
            /*if (Long.parseLong(checkout) < Long.parseLong(object.getString("book_checkin"))) {
                buttonCheckout.setVisibility(View.GONE);
            }*/
            buttonCheckout.setVisibility(View.GONE);
            buttonCheckIn.setVisibility(View.GONE);
            if (object.getString("book_status").equalsIgnoreCase("booked")) {

                String currentDateLoc = APPHelper.getCurrentTimeLocation(timeZone).split(" ")[0];
                if (currentDateLoc.equalsIgnoreCase(sdfDatabaseDate.format(date1))) {
                    buttonCheckIn.setVisibility(View.VISIBLE);
                    buttonCheckout.setVisibility(View.GONE);
                }
            }
            if (object.getString("book_status").equalsIgnoreCase("checkin")) {
                buttonCheckIn.setVisibility(View.GONE);
                buttonCheckout.setVisibility(View.VISIBLE);
            }
            if (object.getString("book_status").equalsIgnoreCase("checkout")) {
                buttonCheckIn.setVisibility(View.GONE);
                buttonCheckout.setVisibility(View.GONE);
            }
            buttonCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        String currentDate = sdfDatabaseDateTime.format(calendar.getTime());
                        checkout = String.valueOf(APPHelper.getUnixTimeZone(currentDate, timeZone));
                        new UpdateCheckout().execute(object.getString("book_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            buttonCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        String currentDate = sdfDatabaseDateTime.format(calendar.getTime());
                        checkin = String.valueOf(APPHelper.getUnixTimeZone(currentDate, timeZone));
                        new UpdateCheckout2().execute(object.getString("book_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            textViewAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RoomLocationActivity.class);
                    intent.putExtra("details", object.toString());
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class UpdateCheckout extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("book_id", strings[0]);
            map.put("checkout", checkout);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_checkout.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    setResult(RESULT_OK);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class UpdateCheckout2 extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("book_id", strings[0]);
            map.put("checkin", checkin);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_checkin.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    setResult(RESULT_OK);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    String getUnixTime(String date) {

        Date date3 = null;

        try {
            Date date1 = sdfDatabaseDateTime.parse(date);
            unixTime1 = date1.getTime() / 1000;

            APPHelper.showLog("TimeU", String.valueOf(unixTime1));
            date3 = new java.util.Date(unixTime1 * 1000L);
// the format of your date
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            APPHelper.showLog("Time1", sdfDatabaseDateTime.format(date3));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(unixTime1);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TimeZone.setDefault(TimeZone.getDefault());
    }
}
