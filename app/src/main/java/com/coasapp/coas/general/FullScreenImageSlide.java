package com.coasapp.coas.general;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.coasapp.coas.R;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageSlide extends AppCompatActivity {

    List<String> listImages = new ArrayList<>();
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_slide);
        listImages = getIntent().getStringArrayListExtra("images");
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new MyAdapter(getApplicationContext(), listImages));
        mPager.setCurrentItem(getIntent().getIntExtra("position", 0));
    }
}
