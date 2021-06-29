package com.coasapp.coas.connectycube;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.coasapp.coas.R;
import com.coasapp.coas.call.HistoryFragment;
import com.coasapp.coas.connectycube.adapters.CallHistoryAdapter;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.BroadcastUtils;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.OnItemClick;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.google.gson.Gson;
import com.sendbird.calls.DirectCallLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryActivity extends MyAppCompatActivity {
    List<JSONObject> list = new ArrayList<>();
    CallHistoryAdapter historyAdapter;

    private SwipeRefreshLayout swipe;
    private RecyclerView recyclerView;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-01-23 16:25:51 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);
        findViews();
        historyAdapter = new CallHistoryAdapter(getApplicationContext(), this, list);
        historyAdapter.setOnItemClick(new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                startActivityForResult(new Intent(getApplicationContext(), CallDetailsActivity.class).putExtra("details", list.get(position).toString()), 1);
            }
        });
        recyclerView.setAdapter(historyAdapter);
        getHistory();


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHistory();
            }
        });



    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_history, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_all) {
            DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
            SQLiteDatabase database = databaseHandler.getWritableDatabase();
            database.execSQL("delete from call_history");
            getHistory();
        }
        return super.onOptionsItemSelected(item);
    }

    void getHistory() {
        Fragment fragment = new HistoryFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_history,fragment).commit();

        list.clear();
        historyAdapter.notifyDataSetChanged();
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        SQLiteDatabase database = databaseHandler.getWritableDatabase();
        String sql = "select * from call_history where call_user_id = ? order by call_time desc";
        Cursor cursor = database.rawQuery(sql, new String[]{"" + SharedPreferencesManager.Companion.getInstance(getApplicationContext()).getCurrentUser().getId()});
        while (cursor.moveToNext()) {
            //Log.i("CallLog", new Gson().toJson(cursor));
            JSONObject object = new JSONObject();
            try {
                object.put("session_id", cursor.getString(cursor.getColumnIndex("session_id")));
                object.put("call_time", cursor.getString(cursor.getColumnIndex("call_time")));
                object.put("call_incoming_status", cursor.getString(cursor.getColumnIndex("call_incoming_status")));
                object.put("call_direction", cursor.getString(cursor.getColumnIndex("call_direction")));
                object.put("call_type", cursor.getInt(cursor.getColumnIndex("call_type")));
                object.put("call_users_data", cursor.getString(cursor.getColumnIndex("call_users_data")));
                list.add(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("CallLog", object.toString());

        }
        cursor.close();
        database.close();
        if (swipe.isRefreshing())
            swipe.setRefreshing(false);
        historyAdapter.notifyDataSetChanged();

    }
    private BroadcastReceiver mReceiver;
    private void registerReceiver() {
        if (mReceiver != null) {
            return;
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DirectCallLog callLog = (DirectCallLog)intent.getSerializableExtra(BroadcastUtils.INTENT_EXTRA_CALL_LOG);
                if (callLog != null) {
                    HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().findFragmentById(R.id.frame_history);
                    historyFragment.addLatestCallLog(callLog);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastUtils.INTENT_ACTION_ADD_CALL_LOG);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                getHistory();
            }
        }
    }
}
