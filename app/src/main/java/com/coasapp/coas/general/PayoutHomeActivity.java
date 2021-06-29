package com.coasapp.coas.general;

import android.content.Intent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PayoutHomeActivity extends AppCompatActivity implements APPConstants {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_home);
        getSupportActionBar().setTitle("Payout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textViewToday = findViewById(R.id.textViewToday);
        TextView textViewWeek = findViewById(R.id.textViewWeek);
        TextView textViewMonth = findViewById(R.id.textViewMonth);
        TextView textViewYear = findViewById(R.id.textViewYear);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        findViewById(R.id.buttonHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PayoutHistoryActivity.class));
            }
        });

        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void taskEnd(String type, String response) {
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject object = new JSONObject(response);
                    if (type.equals("payout")) {
                        textViewWeek.setText(formatter.format(Double.valueOf(object.getString("week"))));
                        textViewMonth.setText(formatter.format(Double.valueOf(object.getString("month"))));
                        textViewToday.setText(formatter.format(Double.valueOf(object.getString("today"))));
                        textViewYear.setText(formatter.format(Double.valueOf(object.getString("year"))));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        callAPI(apiService);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI(apiService);
            }
        });
    }

    void callAPI(APIService apiService) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", getSharedPreferences(APP_PREF, 0).getString("userId", "0"));
        apiService.callAPI(map, MAIN_URL + "payout_home.php", "payout");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

}
