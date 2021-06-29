package com.coasapp.coas.roombook;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.OnDeleteClick;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyRoomsActivity extends AppCompatActivity implements APPConstants {
    ArrayList<JSONObject> arrayListRooms = new ArrayList<>();
    ArrayList<JSONObject> arrayListRooms2 = new ArrayList<>();
    MyRoomsAdapter adapter;
    String userId;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rooms);
        swipeRefreshLayout = findViewById(R.id.swipe);
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRooms);

        findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddRoomActivity.class);
                startActivityForResult(intent, 1);
                SharedPreferences sharedPreferences = getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("mode", "add");
                editor.apply();
            }
        });


        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString();
                arrayListRooms2.clear();
                adapter.notifyDataSetChanged();
                for (int i = 0; i < arrayListRooms.size(); i++) {
                    JSONObject jsonObject = arrayListRooms.get(i);
                    try {
                        if (jsonObject.getString("room_title").toLowerCase().contains(search) || jsonObject.getString("room_street").contains(search)) {
                            arrayListRooms2.add(jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        adapter = new MyRoomsAdapter(arrayListRooms2, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
        adapter.setOnDeleteClick(new OnDeleteClick() {
            @Override
            public void onDeleteClick(int i) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                JSONObject object = arrayListRooms2.get(i);
                                try {
                                    new Delete().execute(object.getString("room_id"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MyRoomsActivity.this);
                builder.setMessage("Delete this Room?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                arrayListRooms2.clear();
                arrayListRooms.clear();
                adapter.notifyDataSetChanged();
                new GetMyRooms().execute(getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));

            }
        });
        userId = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0");
        new GetMyRooms().execute();
        /*JSONObject object = new JSONObject();
        try {
            object.put("name", "Casino");
            object.put("address", "Downtown, NYC");
            object.put("image", "https://skift.com/wp-content/uploads/2017/09/oyo-rooms-1-1.jpg");
            object.put("price", "$5000");
            arrayListRooms.add(object);
            object = new JSONObject();
            object.put("name", "Casino");
            object.put("address", "Downtown, NYC");
            object.put("image", "https://skift.com/wp-content/uploads/2017/09/oyo-rooms-1-1.jpg");
            object.put("price", "$5000");
            arrayListRooms.add(object);
        } catch (JSONException e) {

        }
*/

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*arrayListRooms.clear();
        adapter.notifyDataSetChanged();
        new GetMyRooms().execute(getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                arrayListRooms.clear();
                arrayListRooms2.clear();
                adapter.notifyDataSetChanged();
                new GetMyRooms().execute(getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));
            }
        }
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

    class GetMyRooms extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String,String> map=new HashMap<>();
            map.put("user_id", getSharedPreferences(APP_PREF,0).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "my_rooms.php",map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);

            try {
                arrayListRooms.clear();
                adapter.notifyDataSetChanged();
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayListRooms.add(jsonArray.getJSONObject(i));
                    arrayListRooms2.add(jsonArray.getJSONObject(i));
                    APPHelper.showLog("Room", jsonArray.getJSONObject(i).toString());
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class Delete extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("room_id", strings[0]);
            String url = MAIN_URL + "delete_room.php";
            return new RequestHandler().sendPostRequest(url, map);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    arrayListRooms2.clear();
                    arrayListRooms.clear();
                    adapter.notifyDataSetChanged();
                    new GetMyRooms().execute();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
