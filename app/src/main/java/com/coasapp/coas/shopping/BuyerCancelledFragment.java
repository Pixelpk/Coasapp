package com.coasapp.coas.shopping;


import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class BuyerCancelledFragment extends Fragment implements APPConstants {

    String orderStatus = "Cancelled";
    TabLayout tabLayout;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList1 = new ArrayList<>();
    BuyerOrdersAdapter buyerOrdersAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    public BuyerCancelledFragment() {
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
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        buyerOrdersAdapter = new BuyerOrdersAdapter(jsonObjectArrayList1, getContext(), getActivity());
        recyclerViewOrders.setAdapter(buyerOrdersAdapter);
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
                buyerOrdersAdapter.notifyDataSetChanged();
                String search = s.toString();
                for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                    JSONObject jsonObject = jsonObjectArrayList.get(i);
                    try {
                        if (jsonObject.getString("pro_name").toLowerCase().contains(search) || jsonObject.getString("order_track_id").toLowerCase().contains(search)) {
                            jsonObjectArrayList1.add(jsonObject);
                        }
                        buyerOrdersAdapter.notifyDataSetChanged();
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

        buyerOrdersAdapter.setOnConfirmClick(new BuyerOrdersAdapter.OnConfirmClick() {
            @Override
            public void onConfirmClick(int position) {
                JSONObject jsonObject = jsonObjectArrayList.get(position);
                try {
                    String orderId = jsonObject.getString("order_id");
                    new ConfirmDelivery(position).execute(orderId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        buyerOrdersAdapter.setOnAdapterViewsClicked(new BuyerOrdersAdapter.OnAdapterViewsClicked() {
            @Override
            public void onCancelClicked(int i) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked


                                JSONObject jsonObject = jsonObjectArrayList.get(i);
                                try {
                                    String orderId = jsonObject.getString("order_id");
                                    new Cancel(i).execute(orderId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Reviewed client’s cancellation regulation in the USER TERMS & CONDITIONS?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });
    }

    class Cancel extends AsyncTask<String, Void, String> {
        int pos;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        public Cancel(int pos) {
            this.pos = pos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);

        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("order_id", strings[0]);
            //map.put("points", String.valueOf(newPoint));
            map.put("order_status", "1");
            return new RequestHandler().sendPostRequest(MAIN_URL + "cancel_delivery.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {

                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    JSONObject object = jsonObjectArrayList.get(pos);
                    object.put("order_status", "Cancelled");

                }

                buyerOrdersAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
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
            buyerOrdersAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //map.put("points", String.valueOf(newPoint));
            map.put("order_status", orderStatus);
            return new RequestHandler().sendPostRequest(MAIN_URL + "buyer_orders.php", map);
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
                buyerOrdersAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class ConfirmDelivery extends AsyncTask<String, Void, String> {
        int pos;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        public ConfirmDelivery(int pos) {
            this.pos = pos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);

        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("order_id", strings[0]);
            //map.put("points", String.valueOf(newPoint));
            map.put("order_status", "1");
            return new RequestHandler().sendPostRequest(MAIN_URL + "confirm_delivery.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {

                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    JSONObject object = jsonObjectArrayList.get(pos);
                    object.put("order_approved", "1");

                }

                buyerOrdersAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
