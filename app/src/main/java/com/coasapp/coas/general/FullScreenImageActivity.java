package com.coasapp.coas.general;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenImageActivity extends MyAppCompatActivity {
    PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        photoView = findViewById(R.id.imageView);
        String url = getIntent().getStringExtra("url");
        Glide.with(getApplicationContext()).load(url).into(photoView);
    }
}
