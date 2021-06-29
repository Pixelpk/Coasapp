package com.coasapp.coas.shopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EditAddressActivity extends AppCompatActivity implements APPConstants {

    String address, city, state, pin, country, addId, name, phone;
    LinearLayout layoutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editTextAddress = findViewById(R.id.editTextAddress);
        final EditText editTextCity = findViewById(R.id.editTextCity);
        final EditText editTextState = findViewById(R.id.editTextState);
        final EditText editTextCountry = findViewById(R.id.editTextCountry);
        final EditText editTextZip = findViewById(R.id.editTextPinCode);
        final EditText editTextCon = findViewById(R.id.editTextContact);
        final EditText editTextPhone = findViewById(R.id.editTextConNum);
        try {
            JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            editTextAddress.setText(object.getString("add_street"));
            editTextCity.setText(object.getString("add_city"));
            editTextState.setText(object.getString("add_state"));
            editTextCountry.setText(object.getString("add_country"));
            editTextCon.setText(object.getString("add_contact_name"));
            editTextPhone.setText(object.getString("add_phone"));
            editTextZip.setText(object.getString("add_zipcode"));
            addId = object.getString("add_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        layoutProgress = findViewById(R.id.layoutProgress);
        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = editTextAddress.getText().toString().trim();
                city = editTextCity.getText().toString().trim();
                state = editTextState.getText().toString().trim();
                country = editTextCountry.getText().toString().trim();
                pin = editTextZip.getText().toString().trim();
                name = editTextCon.getText().toString().trim();
                phone = editTextPhone.getText().toString().trim();
                if (address.equalsIgnoreCase("") ||
                        city.equalsIgnoreCase("") ||
                        state.equalsIgnoreCase("") ||
                        pin.equalsIgnoreCase("") ||
                        country.equalsIgnoreCase("") ||
                        name.equalsIgnoreCase("") ||
                        phone.equalsIgnoreCase("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else {
                    new AddAddress().execute();

                }
            }
        });
    }

    class AddAddress extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("address", address);
            map.put("city", city);
            map.put("state", state);
            map.put("country", country);
            map.put("name", name);
            map.put("phone", phone);
            map.put("zip", pin);
            map.put("option", "edit");
            map.put("address_id", addId);
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "edit_address.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
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
