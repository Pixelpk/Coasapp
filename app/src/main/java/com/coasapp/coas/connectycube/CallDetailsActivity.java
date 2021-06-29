package com.coasapp.coas.connectycube;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.connectycube.adapters.CallParticipantsAdapter;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DatabaseHandler;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.request.PagedRequestBuilder;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CallDetailsActivity extends MyAppCompatActivity {

    String sessionId = "";
    List<String> listStartTimes = new ArrayList<>();
    List<String> listEndTimes = new ArrayList<>();
    List<JSONObject> list = new ArrayList<>();
    CallParticipantsAdapter participantsAdapter;

    private ImageView imageViewCallDir;
    private TextView textViewCallDir;
    private TextView textViewCallType;
    private TextView textViewCallDuration;
    private RecyclerView recyclerView;
    SwipeRefreshLayout swipe;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-01-24 09:43:16 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        imageViewCallDir = (ImageView) findViewById(R.id.imageViewCallDir);
        textViewCallType = (TextView) findViewById(R.id.textViewCallType);
        textViewCallDir = (TextView) findViewById(R.id.textViewCallDir);
        textViewCallDuration = (TextView) findViewById(R.id.textViewCallDuration);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipe = findViewById(R.id.swipe);

        int seconds = 0;
        if (seconds>0 && seconds<3) {
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_details);
        findViews();
        participantsAdapter = new CallParticipantsAdapter(getApplicationContext(), this, list);
        recyclerView.setAdapter(participantsAdapter);
        getDetails();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails();
            }
        });

    }

    void getDetails() {
        swipe.setRefreshing(true);
        try {
            listStartTimes.clear();
            listEndTimes.clear();
            list.clear();
            participantsAdapter.notifyDataSetChanged();

            JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            sessionId = object.getString("session_id");
            if (object.getString("call_direction").equalsIgnoreCase("in")) {
                if (object.getString("call_incoming_status").equalsIgnoreCase("missed")) {
                    imageViewCallDir.setImageResource(R.mipmap.missedcall);
                    textViewCallDir.setText("Missed Call");
                } else {
                    imageViewCallDir.setImageResource(R.mipmap.incomingcall);
                    textViewCallDir.setText("Incoming Call");
                }
            } else {
                textViewCallDir.setText("Outgoing Call");
                imageViewCallDir.setImageResource(R.mipmap.outgoingcall);
            }
            if (object.getInt("call_type") == 2) {
                textViewCallType.setText("Voice");
            } else {
                textViewCallType.setText("Video");
            }

            JSONObject objectUsers = new JSONObject(object.getString("call_users_data"));
            List<Integer> integerList = new ArrayList<>();
            Log.i("UsersListC", objectUsers.toString());
            Log.i("UsersListC", objectUsers.names().toString());
            if (objectUsers.names().length() > 0) {
                for (int i = 0; i < objectUsers.names().length(); i++) {
                    integerList.add(Integer.parseInt(objectUsers.names().getString(i)));
                    JSONObject objectData = objectUsers.getJSONObject("" + objectUsers.names().getString(i));
                    Log.i("UsersListD", objectData.toString());
                    if (objectData.has("call_start")) {
                        listStartTimes.add(objectData.getString("call_start"));
                    }
                    if (objectData.has("call_end")) {
                        listEndTimes.add(objectData.getString("call_end"));
                    }
                }

                Log.i("UsersListT", listStartTimes.toString());
                Log.i("UsersListT", listEndTimes.toString());
                Collections.sort(listStartTimes);
                Collections.sort(listEndTimes);


                if (listStartTimes.size() > 0 && listEndTimes.size() > 0) {
                    Date dateStart = sdfDatabaseDateTime.parse(listStartTimes.get(0));
                    Date dateEnd = sdfDatabaseDateTime.parse(listEndTimes.get(listEndTimes.size() - 1));
                    long diff = dateEnd.getTime() - dateStart.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000);
                    String minutes = "" + diffMinutes;
                    if (diffMinutes < 10) minutes = "0" + diffMinutes;
                    String secs = "" + diffSeconds;
                    if (diffSeconds < 10) secs = "0" + diffSeconds;
                    textViewCallDuration.setText(diffHours + ":" + minutes + ":" + secs);
                }

                PagedRequestBuilder pagedRequestBuilder = new PagedRequestBuilder();
                pagedRequestBuilder.setPage(1);
                pagedRequestBuilder.setPerPage(integerList.size());
                Log.i("UsersList", new Gson().toJson(integerList));
                Bundle params = new Bundle();

                ConnectycubeUsers.getUsersByIDs(integerList, pagedRequestBuilder, params).performAsync(new EntityCallback<ArrayList<ConnectycubeUser>>() {
                    @Override
                    public void onSuccess(ArrayList<ConnectycubeUser> connectycubeUsers, Bundle bundle) {
                        swipe.setRefreshing(false);
                        ArrayList<ConnectycubeUser> connectycubeUserArrayList = new ArrayList<>();
                        String names = "";
                        for (int i = 0; i < connectycubeUsers.size(); i++) {
                            names = names + connectycubeUsers.get(i).getFullName() + ",";

                            JSONObject objectParticipants = new JSONObject();
                            try {
                                objectParticipants.put("user_id", connectycubeUsers.get(i).getId());
                                objectParticipants.put("name", APPHelper.getContactName(getApplicationContext(),connectycubeUsers.get(i).getPhone(),connectycubeUsers.get(i).getFullName()));
                                objectParticipants.put("image", connectycubeUsers.get(i).getAvatar());
                                objectParticipants.put("status", "");
                                JSONObject objectData = objectUsers.getJSONObject("" + connectycubeUsers.get(i).getId());
                                if (objectData.has("call_status")) {
                                    if (objectData.getString("call_status").equalsIgnoreCase("Missed")
                                            || objectData.getString("call_status").equalsIgnoreCase("Rejected")
                                            || objectData.getString("call_status").equalsIgnoreCase("Not Answered")) {
                                        objectParticipants.put("status", objectData.getString("call_status"));
                                    }
                                }
                                else {
                                    objectParticipants.put("status", "Not Answered");

                                }
                                Log.i("CallData", objectUsers.names().getString(i));
                                Log.i("CallData", objectData.toString());
                                Log.i("CallData", new Gson().toJson(connectycubeUsers.get(i)));
                                Log.i("CallData", objectParticipants.toString());
                                list.add(objectParticipants);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        participantsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ResponseException e) {
                        swipe.setRefreshing(false);
                        Toast.makeText(CallDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (JSONException e) {
            swipe.setRefreshing(false);
            e.printStackTrace();
        } catch (ParseException e) {
            swipe.setRefreshing(false);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
            SQLiteDatabase sqLiteDatabase = databaseHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("delete from call_history where session_id = '" + sessionId + "'");
            sqLiteDatabase.close();
            setResult(RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
