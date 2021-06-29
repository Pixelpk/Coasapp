package com.coasapp.coas.connectycube;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.coasapp.coas.R;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.connectycube.auth.session.ConnectycubeSettings;
import com.connectycube.pushnotifications.services.ConnectycubePushManager;

public class ChatSettingsActivity extends MyAppCompatActivity {

    String TAG="NotificationSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        Switch switchNotifications = findViewById(R.id.switchNotifications);
        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

        boolean isEnabled = ConnectycubeSettings.getInstance().isEnablePushNotification();
        findViewById(R.id.layoutProgress).setVisibility(View.GONE);

        switchNotifications.setChecked(isEnabled);

        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isPressed()){
                    findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
                    ConnectycubeSettings.getInstance().setEnablePushNotification(isChecked);
                }
            }
        });

        ConnectycubePushManager.getInstance().addListener(new ConnectycubePushManager.SubscribeListener() {
            @Override
            public void onSubscriptionCreated() {
                //findViewById(R.id.layoutProgress).setVisibility(View.GONE);

            }

            @Override
            public void onSubscriptionError(final Exception e, int resultCode) {
                //findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                Log.d(TAG, "onSubscriptionError" + e);
                if (resultCode >= 0) {
                    Log.d(TAG, "Google play service exception" + resultCode);
                }
            }

            @Override
            public void onSubscriptionDeleted(boolean success) {
               // findViewById(R.id.layoutProgress).setVisibility(View.GONE);

            }
        });
    }
}
