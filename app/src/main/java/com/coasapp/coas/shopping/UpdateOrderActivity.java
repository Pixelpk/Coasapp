package com.coasapp.coas.shopping;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class UpdateOrderActivity extends AppCompatActivity implements APPConstants {

    String courier, track, estDate, orderId;
    Calendar calendar;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    LinearLayout layoutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_order);
        calendar = Calendar.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        layoutProgress = findViewById(R.id.layoutProgress);
        final EditText editTextCourier = findViewById(R.id.editTextCourier);
        final EditText editTextCourierTracking = findViewById(R.id.editTextCourierTracking);
        final EditText editTextDate = findViewById(R.id.editTextEstDate);
        try {
            JSONObject objectDetails = new JSONObject(getIntent().getStringExtra("details"));
            editTextCourier.setText(objectDetails.getString("order_courier"));
            editTextCourierTracking.setText(objectDetails.getString("order_courier_track"));
            estDate = objectDetails.getString("order_est");
            if (!objectDetails.getString("order_est").equalsIgnoreCase("0000-00-00"))
                editTextDate.setText(sdf.format(sdf2.parse(estDate)));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                estDate = sdf2.format(calendar.getTime());
                //bookDate1 = sdfNativeDate.format(calendar.getTime());
                editTextDate.setText(sdf.format(calendar.getTime()));

            }
        };
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog aDatePickerDialog = new DatePickerDialog(UpdateOrderActivity.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTime(new Date());
                aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                aDatePickerDialog.show();
            }
        });


        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentFocus().clearFocus();
                courier = editTextCourier.getText().toString();
                track = editTextCourierTracking.getText().toString();
                if (courier.equalsIgnoreCase("") || track.equalsIgnoreCase("") || estDate.equalsIgnoreCase("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all details");
                } else {
                    new UpdateTrack().execute();
                }
            }
        });
    }

    class UpdateTrack extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            //map.put("points", String.valueOf(newPoint));
            map.put("order_id", orderId);
            map.put("est_date", estDate);
            map.put("courier", courier);
            map.put("consignment", track);
            map.put("status", "Shipped");
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_order.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    setResult(RESULT_OK);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
