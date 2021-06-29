package com.coasapp.coas.shopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.coasapp.coas.R;
import com.coasapp.coas.general.COASHomeActivity;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyerOrdersActivity extends AppCompatActivity implements APPConstants {

    String orderStatus = "To Ship";
    TabLayout tabLayout;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList1 = new ArrayList<>();
    BuyerOrdersAdapter buyerOrdersAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ViewPager viewPager;
    int viewPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_buyer_orders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.pager_orders);
        tabLayout = findViewById(R.id.tabLayoutBuyerOrders);
        tabLayout.setupWithViewPager(viewPager, true);
        setupViewPager();



    }
    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BuyerActiveFragment(), "To Ship");
        adapter.addFragment(new BuyerShippedFragment(), "Shipped");
        adapter.addFragment(new BuyerDeliveredFragment(), "Delivered/Completed");
        adapter.addFragment(new BuyerReportedFragment(), "Reported");
        adapter.addFragment(new BuyerCancelledFragment(), "Cancelled");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
             mFragmentList.clear();
             mFragmentTitleList.clear();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("checkout", false)) {
            Intent intent = new Intent(getApplicationContext(), COASHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("action", getIntent().getIntExtra("action", 0));
            setResult(RESULT_OK, intent);
            finish();
            //super.onBackPressed();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupViewPager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewPos = viewPager.getCurrentItem();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    class BuyerOrders extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

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
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

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
