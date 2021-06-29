package com.coasapp.coas.roombook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.coasapp.coas.R;
import com.coasapp.coas.general.COASLoginActivity;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NegotiableRentActivity extends MyAppCompatActivity implements APPConstants {
    SharedPreferences sharedPreferences;
    String userId, bargainId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negotiable_rent);
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "0");
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
                    bargainId = deepLink.toString().substring(deepLink.toString().lastIndexOf("/") + 1);
                }
            }
        }

        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            }

            @Override
            public void taskEnd(String type, String response) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                try {
                    JSONObject object = new JSONObject(response);
                    if (type.equalsIgnoreCase("book")) {
                        JSONArray array = object.getJSONArray("booking");
                        Fragment fragment;
                        Bundle bundle = new Bundle();

                        if (array.length() > 0) {
                            JSONObject object1 = array.getJSONObject(0);
                            if(object1.getString("paid").equalsIgnoreCase("1")){
                                APPHelper.showToast(getApplicationContext(),"Already Booked");
                            }
                            else {
                                if (object1.getString("book_type").equalsIgnoreCase("hour")) {
                                    fragment = new HourlyRoomBookFragment();
                                } else {
                                    fragment = new NightRoomBookingFragment();
                                }
                                //fragment = new NightRoomBookingFragment();
                                bundle.putString("from", "link");
                                bundle.putInt("pkg", getIntent().getIntExtra("pkg", 1));
                                if (bargainId.contains("0"))
                                    bundle.putString("book_id", bargainId.substring(bargainId.lastIndexOf("0") + 1));
                                else {
                                    bundle.putString("book_id", bargainId.substring(bargainId.lastIndexOf("R") + 1));
                                }
                                bundle.putString("book_id",bargainId);
                                bundle.putString("details", object1.toString());
                                bundle.putString("unitprice", object1.getString("book_amount"));
                                bundle.putString("room_id", object1.getString("room_id"));
                                bundle.putString("rules", object1.getString("room_rules"));
                                fragment.setArguments(bundle);

                                getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                            }



                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("book_id", bargainId);
        apiService.callAPI(map, APPConstants.MAIN_URL + "get_negotiable_details.php", "book");
    }
}
