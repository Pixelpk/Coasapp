package com.coasapp.coas.bargain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.coasapp.coas.R;
import com.coasapp.coas.general.COASLoginActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BargainLinkActivity extends AppCompatActivity implements APPConstants {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bargain_link);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");
        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            }

            @Override
            public void taskEnd(String type, String response) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                try {
                    JSONObject objectResponse = new JSONObject(response);
                    if (type.equalsIgnoreCase("bargain")) {
                        JSONArray array = objectResponse.getJSONArray("bargains");
                        if (array.length() > 0) {
                            JSONObject object = array.getJSONObject(0);
                            Intent intent = null;
                            if (userId.equals(object.getString("bargain_user_id"))) {
                                intent = new Intent(getApplicationContext(), BargainHistoryActivity.class);

                            } else if (userId.equals(object.getString("bargain_driver_id"))) {
                                intent = new Intent(getApplicationContext(), MyBargainRequests.class);
                            }

                            intent.putExtra("bargain_id", object.getString("bargain_ref"));
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        Intent intent = getIntent();
        if (intent != null) {
            Uri deepLink = intent.getData();
            //Toast.makeText(getApplicationContext(), deepLink.toString(), Toast.LENGTH_LONG).show();

            if (deepLink != null) {
                if (!sharedPreferences.getBoolean("loggedIn", false)) {
                    Intent intent1 = new Intent(getApplicationContext(), COASLoginActivity.class);
                    startActivity(intent1);
                    finish();
                } else {
                    String bargainId = deepLink.toString().substring(deepLink.toString().lastIndexOf("/") + 1);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id", userId);
                    map.put("bargain_id", bargainId);
                    apiService.callAPI(map, MAIN_URL + "bargain_details.php", "bargain");
                }
            }
        }
    }
}
