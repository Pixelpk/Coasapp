package com.coasapp.coas.roombook;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;


public class RoomBookingActivity extends AppCompatActivity {

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_room_booking);
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        Bundle bundle = new Bundle();

        if (getIntent().getIntExtra("pkg", 1) == 1) {
            fragment = new HourlyRoomBookFragment();
        } else {
            fragment = new NightRoomBookingFragment();
        }

        //fragment = new NightRoomBookingFragment();
        bundle.putString("from", "direct");
        bundle.putInt("pkg", getIntent().getIntExtra("pkg", 1));
        bundle.putString("details", getIntent().getStringExtra("details"));
        bundle.putString("unitprice", getIntent().getStringExtra("unitprice"));
        bundle.putString("room_id", getIntent().getStringExtra("room_id"));
        APPHelper.showLog("roomId", getIntent().getStringExtra("room_id"));
        bundle.putString("rules", getIntent().getStringExtra("rules"));
        bundle.putString("terms", getIntent().getStringExtra("terms"));
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
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

    @Override
    protected void onStop() {
        super.onStop();
    }
}
