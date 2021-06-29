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
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SellerOrdersActivity extends AppCompatActivity implements APPConstants {
    String courier, track, estDate, orderId;

    String orderStatus = "To Ship";
    ViewPager viewPager;
    TabLayout tabLayout;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList1 = new ArrayList<>();

    SellerOrdersAdapter sellerOrdersAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout layoutProgress;
    int viewPos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        layoutProgress = findViewById(R.id.layoutProgress);

        swipeRefreshLayout = findViewById(R.id.swipe);
        tabLayout = findViewById(R.id.tabLayoutBuyerOrders);
        viewPager = findViewById(R.id.pager_orders);
        tabLayout.setupWithViewPager(viewPager,true);
        setupViewPager();
        /*tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        orderStatus = "To Ship";
                        break;
                    case 1:
                        orderStatus = "Shipped";
                        break;
                    case 2:
                        orderStatus = "Delivered";
                        break;
                    case 3:
                        orderStatus = "Cancelled";
                        break;
                    case 4:
                        orderStatus = "Returned";
                        break;
                }
                APPHelper.showLog("Status", orderStatus);
                new BuyerOrders().execute();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/


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

    /* @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == RESULT_OK) {
                    if (requestCode == 10) {
                        //new BuyerOrders().execute();
                    }
                }
            }*/
   private void setupViewPager() {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
       adapter.addFragment(new SellerActiveFragment(), "To Ship");
       adapter.addFragment(new SellerShippedFragment(), "Shipped");
       adapter.addFragment(new SellerDeliveredFragment(), "Delivered/Completed");
       adapter.addFragment(new SellerReportedFragment(), "Reported");
       adapter.addFragment(new SellerCancelledFragment(), "Cancelled");
       viewPager.setAdapter(adapter);
       viewPager.setCurrentItem(viewPos);
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
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
        //super.onBackPressed();

    }

    class BuyerOrders extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
