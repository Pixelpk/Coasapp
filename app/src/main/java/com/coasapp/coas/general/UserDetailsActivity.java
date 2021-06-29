package com.coasapp.coas.general;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.PostRequestAsyncTask;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.connectycube.messenger.ChatDialogDetailsActivityKt.EXTRA_CHAT_DIALOG_ID;

public class UserDetailsActivity extends MyAppCompatActivity implements APPConstants {

    String image = "";

    PostRequestAsyncTask asyncTaskUsers;

    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void taskEnd(String type, String response) {
            swipeRefreshLayout.setRefreshing(false);
            if (type.equalsIgnoreCase("users")) {
                try {
                    JSONObject objectRes = new JSONObject(response);
                    JSONArray arrayUser = objectRes.getJSONArray("user");
                    if (arrayUser.length() > 0) {
                        JSONObject object = arrayUser.getJSONObject(0);
                        image = object.getString("image");
                        Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageViewProfile);
                        editTextName.setText(APPHelper.getContactName(getApplicationContext(), object.getString("country_code") + object.getString("phone"), object.getString("name")));
                        editTextCountry.setText(object.getString("cname"));
                        textViewCoasId.setText(object.getString("coas_id"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private ImageView imageView4;
    private TextView textView3;
    private TextView textViewCoasId;
    private TextInputEditText editTextName;
    private TextInputEditText editTextCountry;
    private Button buttonChat;
    CircleImageView imageViewProfile;
    SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-09-19 10:38:48 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        imageView4 = (ImageView) findViewById(R.id.imageView4);
        textView3 = (TextView) findViewById(R.id.textView3);
        textViewCoasId = (TextView) findViewById(R.id.textViewCoasId);
        editTextName = (TextInputEditText) findViewById(R.id.editTextName);
        editTextCountry = (TextInputEditText) findViewById(R.id.editTextCountry);
        buttonChat = (Button) findViewById(R.id.buttonChat);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        swipeRefreshLayout = findViewById(R.id.swipe);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        getSupportActionBar().setTitle("User Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();

        Switch switchNotifications = findViewById(R.id.switchNotifications);

        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isPressed()) {
                    ProgressDialog progressDialog = APPHelper.createProgressDialog(UserDetailsActivity.this, "Please Wait", false);
                    progressDialog.show();

                    ConnectycubeRestChatService.updateDialogNotificationSending(getIntent().getStringExtra(EXTRA_CHAT_DIALOG_ID), isChecked).performAsync(new EntityCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean, Bundle bundle) {
                            progressDialog.dismiss();
                            if (aBoolean != null) {
                                Toast.makeText(UserDetailsActivity.this, "Setting Updated", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(ResponseException e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        ProgressDialog progressDialog = APPHelper.createProgressDialog(UserDetailsActivity.this, "Loading", false);
        progressDialog.show();
        Log.i("DialogId", getIntent().getStringExtra(EXTRA_CHAT_DIALOG_ID));
        ConnectycubeRestChatService.checkIsDialogNotificationEnabled(getIntent().getStringExtra(EXTRA_CHAT_DIALOG_ID)).performAsync(new EntityCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean, Bundle bundle) {
                progressDialog.dismiss();
                switchNotifications.setChecked(aBoolean);
            }

            @Override
            public void onError(ResponseException e) {
                progressDialog.dismiss();
                Toast.makeText(UserDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });


        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                intent.putExtra("url", MAIN_URL_IMAGE + image);
                startActivity(intent);

            }
        });
        getUser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void getUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        HashMap<String, String> map = new HashMap<>();
        map.put("coas_id", getIntent().getStringExtra("coas_id").toUpperCase());
        map.put("self", sharedPreferences.getString("coasId", ""));
        asyncTaskUsers = new PostRequestAsyncTask(getApplicationContext(), map, "users", apiCallbacks);
        asyncTaskUsers.execute(MAIN_URL + "find_user.php");
    }

}
