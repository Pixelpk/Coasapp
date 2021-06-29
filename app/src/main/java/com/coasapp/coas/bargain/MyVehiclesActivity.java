package com.coasapp.coas.bargain;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.coasapp.coas.utils.APPHelper.isLastItemDisplaying;

public class MyVehiclesActivity extends MyAppCompatActivity implements APPConstants {

    APICallbacks apiCallbacks;

    VehicleListAdapter vehicleListAdapter;
    ArrayList<JSONObject> arrayListVehicles = new ArrayList<>();
    ArrayList<JSONObject> arrayListVehiclesSearch = new ArrayList<>();
    int start = 0;
    APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_vehicles);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGrid);
        vehicleListAdapter = new VehicleListAdapter(arrayListVehiclesSearch, this, getApplicationContext());
        recyclerView.setAdapter(vehicleListAdapter);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        FrameLayout layoutMore = findViewById(R.id.layoutMore);
        EditText editTextSearch=findViewById(R.id.editTextSearch);
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
                    if (type.equalsIgnoreCase("vehicles")) {
                        JSONArray array = object.getJSONArray("vehicles");
                        if (array.length() > 0) {
                            arrayListVehiclesSearch.clear();
                            arrayListVehicles.clear();
                            arrayListVehicles.addAll(APPHelper.getJSONObjectsList(array));
                            arrayListVehiclesSearch.addAll(APPHelper.getJSONObjectsList(array));
                            vehicleListAdapter.notifyDataSetChanged();
                            filter(editTextSearch.getText().toString());
                        }
                        else {
                            startActivityForResult(new Intent(getApplicationContext(), ManageVehicleActivity.class), 1);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
         apiService = new APIService(apiCallbacks, this);
        findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(getApplicationContext(), ManageVehicleActivity.class), 1);
            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
filter(s.toString());
            }
        });
/*
        try {
            JSONObject object = new JSONObject();
            object.put("vehicle_name", "Innova");
            object.put("vehicle_brand", "Toyota");
            object.put("vehicle_price", "20000");
            object.put("vehicle_image", "https://cdn.drivemag.net/media/default/0001/79/CUPRA-e-Racer-001H-1846-8583-default-large.jpeg");
            object.put("name", "test");
            object.put("image", "http://easemypay.in/coas/profile_pictures/user.png");
            arrayListVehiclesSearch.add(object);
            object = new JSONObject();
            object.put("vehicle_name", "Innova");
            object.put("vehicle_brand", "Toyota");
            object.put("vehicle_price", "20000");
            object.put("vehicle_image", "https://cdn.drivemag.net/media/default/0001/79/CUPRA-e-Racer-001H-1846-8583-default-large.jpeg");
            object.put("name", "test");
            object.put("image", "http://easemypay.in/coas/profile_pictures/user.png");
            arrayListVehiclesSearch.add(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        callAPI(apiService);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI(apiService);
            }
        });

        vehicleListAdapter.setOnItemClick(new VehicleListAdapter.OnItemClick() {
            @Override
            public void onItemClick(int i) {
                JSONObject object=arrayListVehiclesSearch.get(i);
                Intent intent=new Intent(getApplicationContext(),ManageVehicleActivity.class);
                try {
                    intent.putExtra("vehicle_id",object.getString("vehicle_id"));
                    startActivityForResult(intent,1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLastItemDisplaying(recyclerView)) {
                    //Calling the method getdata again
                    //getData();


                }
            }
        });
    }


    void callAPI(APIService apiService) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", getSharedPreferences(APP_PREF, 0).getString("userId", "0"));
        //map.put("index", String.valueOf(start));
        apiService.callAPI(map, MAIN_URL + "my_vehicles.php", "vehicles");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1){
                callAPI(apiService);
            }
        }
    }

    void filter(String search) {
        arrayListVehiclesSearch.clear();
        for (int i = 0; i < arrayListVehicles.size(); i++) {
            JSONObject object = arrayListVehicles.get(i);
            try {
                if (object.getString("brand_name").toLowerCase().contains(search.toLowerCase()) || object.getString("model_name").toLowerCase().contains(search.toLowerCase())) {
                    arrayListVehiclesSearch.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        vehicleListAdapter.notifyDataSetChanged();
    }
}