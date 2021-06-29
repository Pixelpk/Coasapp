package com.coasapp.coas.call;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.connectycube.messenger.helpers.RingtoneManager;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;

import static com.coasapp.coas.call.CallService.*;

public class IncomingCallRingActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_incoming_call_ring);
        //ringtoneManager = new RingtoneManager(getApplicationContext());
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            // If you want to display the keyguard to prompt the user to unlock the phone:
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }

        Intent intent = getIntent(), intentCall;

        ((TextView) findViewById(R.id.textViewCallerName)).setText(
                APPHelper.getContactName(getApplicationContext(), intent.getStringExtra(
                        EXTRA_CALLER_ID), intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID)));

        boolean isVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false);
        if (isVideoCall) {
            intentCall = new Intent(getApplicationContext(), VideoCallActivity.class);
        } else {
            intentCall = new Intent(getApplicationContext(), VoiceCallActivity.class);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("call_end"));

        intentCall.putExtra(EXTRA_CALL_ID, intent.getStringExtra(EXTRA_CALL_ID));
        intentCall.putExtra(EXTRA_CALLER_ID, intent.getStringExtra(EXTRA_CALLER_ID));
        intentCall.putExtra(EXTRA_CALLEE_ID, intent.getStringExtra(EXTRA_CALLEE_ID));
        intentCall.putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall);
        intentCall.putExtra(EXTRA_IS_END, intent.getBooleanExtra(EXTRA_IS_END, false));
//        ringtoneManager.start(true, true);

        findViewById(R.id.imageViewAccept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                }
                startActivity(intentCall);
                finish();
            }
        });
        findViewById(R.id.imageViewReject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFunctions();

            }
        });
    }

    void stopFunctions(){
        Intent intentRingtone = new Intent("ringtone");
        intentRingtone.putExtra("play", false);
        sendBroadcast(intentRingtone);
        end(SendBirdCall.getCall(getIntent().getStringExtra(EXTRA_CALL_ID)));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ringtoneManager.stop();


    }

    protected void end(DirectCall call) {
        Log.i("NullDirectCall", String.valueOf(call == null));
        if (call != null) {
            call.end();
        }
    }
}