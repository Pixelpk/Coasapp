package com.coasapp.coas.shopping;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SellerCancelledFragment extends Fragment implements APPConstants {
    String courier, track, estDate, orderId;

    String orderStatus = "Cancelled";
    TabLayout tabLayout;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList1 = new ArrayList<>();

    SellerOrdersAdapter sellerOrdersAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout layoutProgress;


    public SellerCancelledFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buyer_active, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerViewOrders = view.findViewById(R.id.recyclerView);
        layoutProgress = view.findViewById(R.id.layoutProgress);

        swipeRefreshLayout = view.findViewById(R.id.swipe);
        tabLayout = view.findViewById(R.id.tabLayoutBuyerOrders);
        sellerOrdersAdapter = new SellerOrdersAdapter(jsonObjectArrayList1, getActivity().getApplicationContext(), getActivity());
        recyclerViewOrders.setAdapter(sellerOrdersAdapter);
        new BuyerOrders().execute();
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                jsonObjectArrayList1.clear();
                sellerOrdersAdapter.notifyDataSetChanged();
                String search = s.toString();
                for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                    JSONObject jsonObject = jsonObjectArrayList.get(i);
                    try {
                        if (jsonObject.getString("pro_name").toLowerCase().contains(search) || jsonObject.getString("order_track_id").toLowerCase().contains(search)) {
                            jsonObjectArrayList1.add(jsonObject);
                        }
                        sellerOrdersAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new BuyerOrders().execute();

            }
        });

        sellerOrdersAdapter.setOnDelClick(new SellerOrdersAdapter.OnDelClick() {
            @Override
            public void onDelClick(int position) {

                JSONObject jsonObject = jsonObjectArrayList.get(position);
                try {
                    estDate = jsonObject.getString("order_est");
                    courier = jsonObject.getString("order_courier");
                    track = jsonObject.getString("order_courier_track");
                    orderId = jsonObject.getString("order_id");
                    new UpdateTrack().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                new BuyerOrders().execute();
            }
        }
    }

    class BuyerOrders extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            jsonObjectArrayList.clear();
            jsonObjectArrayList1.clear();
            sellerOrdersAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //map.put("points", String.valueOf(newPoint));
            map.put("order_status", orderStatus);
            return new RequestHandler().sendPostRequest(MAIN_URL + "seller_orders.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {

                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    jsonObjectArrayList.add(object);
                    jsonObjectArrayList1.add(object);
                }

                sellerOrdersAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
            map.put("status", "Delivered");
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_order.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    new BuyerOrders().execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

