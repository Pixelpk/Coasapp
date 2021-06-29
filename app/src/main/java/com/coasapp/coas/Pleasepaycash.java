package com.coasapp.coas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coasapp.coas.general.COASHomeActivity;

public class Pleasepaycash extends AppCompatActivity {

    TextView totalamount;
    String total;
    Button cash_paid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pleasepaycash);

        Intent intent = getIntent();

        totalamount = findViewById(R.id.totalamount);
        cash_paid = findViewById(R.id.cash_paid);

        total = intent.getStringExtra("totalamount");

        totalamount.setText(total);

        cash_paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Pleasepaycash.this, COASHomeActivity.class);
                startActivity(intent);
            }
        });


    }
}