package com.coasapp.coas.general;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.coasapp.coas.R;

import java.util.ArrayList;
import java.util.List;

public class PayoutHistoryActivity extends AppCompatActivity {

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = findViewById(R.id.viewPagerPayout);
        TabLayout tabLayout = findViewById(R.id.tabPayout);
        tabLayout.setupWithViewPager(viewPager, true);
        setupViewPager();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Fragment fragment = new RentPayoutFragment();
        Bundle bundle = new Bundle();
        bundle.putString("cat", "rent");
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "Renting");

        fragment = new RentPayoutFragment();
        bundle = new Bundle();
        bundle.putString("cat", "shop");
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "Shopping");

        fragment = new RentPayoutFragment();
        bundle = new Bundle();
        bundle.putString("cat", "bargain");
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "Bargain");

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_payout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_payout_info){
            startActivityForResult(new Intent(getApplicationContext(),PayoutActivity.class),1);
        }
        else if(id==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
