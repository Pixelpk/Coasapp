package com.coasapp.coas.bargain;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.GetRequestAsyncTask;
import com.coasapp.coas.utils.RequestHandler;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class RequestActivity extends AppCompatActivity implements APPConstants {
    //WebService webService;
    EditText ed_time, ed_date, editTextSource, editTextDest, editTextMembers, editTextRate;
    String time = "", date = "", date2 = "", source = "", destination = "", members = "", rate = "";
    Button bt_submit;
    private int Hour, Minute;
    private int Year, Month, Day;
    double lat1, lng1, lat2, lng2;
    ConnectivityManager ConnectionManager;
    ProgressDialog progressDialog;
    LinearLayout layoutProgress;
    NetworkInfo networkInfo;
    String driverId = "";
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    String timezoneFrom = "GMT";
    APICallbacks apiCallbacks;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    String incoming_userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        sp = getSharedPreferences(APP_PREF,MODE_PRIVATE);

        incoming_userid = sp.getString(APPConstants.incoming_user_id,null);
        Toast.makeText(this, incoming_userid, Toast.LENGTH_SHORT).show();

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //time = ed_time.getText().toString();
                //date = ed_date.getText().toString();
                source = editTextSource.getText().toString().trim();
                destination = editTextDest.getText().toString().trim();
                members = "0";
                rate = editTextRate.getText().toString();
                if (
                        time.length() == 0
                                || date2.length() == 0
                                || source.length() == 0
                                || destination.length() == 0
                                || rate.length() == 0
                ) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else if (APPHelper.getUnixTime(date + SPACE + time) < APPHelper.getUnixTime(sdfDatabaseDateTime.format(Calendar.getInstance().getTime()))) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Time");
                } else {
                    internet_check();
                }
            }
        });

        apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void taskEnd(String type, String response) {
                layoutProgress.setVisibility(View.GONE);
                try {
                    JSONObject object = new JSONObject(response);
                    //Toast.makeText(RequestActivity.this, type, Toast.LENGTH_SHORT).show();
                    if (type.equals("timezoneFrom")) {
                        timezoneFrom = object.getString("timezoneId");

                        Toast.makeText(RequestActivity.this, timezoneFrom, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        ed_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker();

            }
        });
        ed_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //timePicker();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Place place = PlaceAutocomplete.getPlace(this, data);
            if (requestCode == 1) {

                /*List<Address> addresses = null;
                lat1 = place.getLatLng().latitude;
                lng1 = place.getLatLng().longitude;
                editTextSource.setText(place.getAddress());*/
                lat1 = data.getDoubleExtra("latitude", 0);
                lng1 = data.getDoubleExtra("longitude", 0);
                editTextSource.setText(data.getStringExtra("address"));

                GetRequestAsyncTask getRequestAsyncTask = new GetRequestAsyncTask(getApplicationContext(), apiCallbacks);
                getRequestAsyncTask.setType("timezoneFrom");
                getRequestAsyncTask.execute(APPHelper.getTimeZoneUrl(lat1, lng1));

            } else if (requestCode == 2) {
                lat2 = data.getDoubleExtra("latitude", 0);
                lng2 = data.getDoubleExtra("longitude", 0);
                editTextDest.setText(data.getStringExtra("address"));


               /* List<Address> addresses = null;
                lat2 = place.getLatLng().latitude;
                lng2 = place.getLatLng().longitude;
                editTextDest.setText(place.getAddress());*/
            }
        }
    }

    //Date picker fragment
    private void datePicker() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone(timezoneFrom));
        Year = c.get(Calendar.YEAR);
        Month = c.get(Calendar.MONTH);
        Day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.YEAR, year);
                        date2 = sdf.format(c.getTime());
                        date = sdf2.format(c.getTime());


                        //*************Call Time Picker Here ********************
                        timePicker();
                    }
                }, Year, Month, Day);
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //Time picker fragment
    private void timePicker() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
c.setTimeZone(TimeZone.getTimeZone(timezoneFrom));
        int minH = c.get(Calendar.HOUR_OF_DAY);

        int minM = c.get(Calendar.MINUTE);
        if (!date.equalsIgnoreCase(sdfDatabaseDate.format(c.getTime()))) {
            minH = 0;
            minM = 0;
        }
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Hour = hourOfDay;
                        Minute = minute;
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);

                        APPHelper.showLog("Time", "" + Hour + " " + Minute);


                        String timeSet = "";

                        if (Hour > 12) {
                            Hour -= 12;
                            timeSet = "PM";
                        } else if (Hour == 0) {
                            Hour += 12;
                            timeSet = "AM";
                        } else if (Hour == 12) {
                            timeSet = "PM";
                        } else {
                            timeSet = "AM";
                        }
                        String h = String.valueOf(Hour);
                        String m = String.valueOf(Minute);
                        APPHelper.showLog("Time", "" + Hour + " " + Minute);
                        if (Hour < 10) {

                            h = "0" + h;
                        }

                        if (Minute < 10) {

                            m = "0" + m;
                        }
                        time = sdf3.format(c.getTime());
                    //    ed_time.setText(h + ":" + m + " " + timeSet);
                        ed_date.setText(date2 + " " + h + ":" + m + " " + timeSet);

                    }
                }, minH, minM, true);
        timePickerDialog.show();
    }

    public void internet_check() {


        ConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = ConnectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() == true) {
            //   Toast.makeText(PhoneNoActivity.this, "Network Available", Toast.LENGTH_LONG).show();
            new CheckAsy().execute();
        } else {
            //Toast.makeText(MainActivity.this, "Network Not Available", Toast.LENGTH_LONG).show();
            showAlertDialog();

        }
    }

    //alert dialog box

    public void showAlertDialog() {
        Toast.makeText(this, "Check Internet", Toast.LENGTH_SHORT).show();
    }

    //check api

    class CheckAsy extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);

        }


        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> map = new HashMap<>();
            map.put("driver_id", driverId);
            map.put("source", source);
            map.put("destination", destination);
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            map.put("time", time);
            map.put("date", APPHelper.getGMTTime(date + APPConstants.SPACE + time, timezoneFrom));
            map.put("amt", rate);
            map.put("source_lat", String.valueOf(lat1));
            map.put("source_lng", String.valueOf(lng1));
            map.put("dest_lat", String.valueOf(lat2));
            map.put("dest_lng", String.valueOf(lng2));
            return new RequestHandler().sendPostRequest(MAIN_URL + "send_bargain_request.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

                    String link = object.getString("link");
                    String name = getIntent().getStringExtra("name");

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("merchantID", sharedPreferences.getString("firstName", "") + SPACE + "sent you a bargain request " + link);
                    clipboard.setPrimaryClip(clip);
                    APPHelper.showToast(getApplicationContext(), "Link copied to clipboard");
                    onBackPressed();
                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //initialization
    public void init() {

        driverId = getIntent().getStringExtra("customer");


        Calendar c;
        ed_date = findViewById(R.id.edit_date);
        layoutProgress = findViewById(R.id.layoutProgress);
        ed_time = findViewById(R.id.edit_time);
        editTextSource = findViewById(R.id.et_source);
        editTextDest = findViewById(R.id.et_dest);
        editTextRate = findViewById(R.id.et_rate);
        bt_submit = (Button) findViewById(R.id.button_submit);
        editTextSource.setText(getIntent().getStringExtra("source"));
        editTextDest.setText(getIntent().getStringExtra("dest"));
        //editTextSource.setText("abcd");
        editTextSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*layoutProgress.setVisibility(View.VISIBLE);
                try {
                    APPHelper.showLog("address", "click");

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(RequestActivity.this);
                    startActivityForResult(intent, 1);
                    layoutProgress.setVisibility(View.GONE);
                } catch (GooglePlayServicesRepairableException e) {
                    APPHelper.showLog("address", e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    APPHelper.showLog("address", e.getMessage());
                }*/
                APPHelper.launchSelectAddressActivity(RequestActivity.this, 1);
            }
        });
        editTextDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //layoutProgress.setVisibility(View.VISIBLE);
               /* try {
                    layoutProgress.setVisibility(View.GONE);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(RequestActivity.this);
                    startActivityForResult(intent, 2);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/
                APPHelper.launchSelectAddressActivity(RequestActivity.this, 2);
            }
        });
    }
}
