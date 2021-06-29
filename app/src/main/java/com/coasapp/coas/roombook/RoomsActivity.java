package com.coasapp.coas.roombook;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.coasapp.coas.R;

public class RoomsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new RoomsFragment()).commit();
    }
}
