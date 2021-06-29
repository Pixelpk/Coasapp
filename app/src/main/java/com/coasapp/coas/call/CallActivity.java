package com.coasapp.coas.call;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.R;
import com.coasapp.coas.general.COASHomeActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.AuthenticationUtils;
import com.coasapp.coas.utils.BroadcastUtils;
import com.coasapp.coas.utils.UserInfoUtils;
import com.connectycube.messenger.helpers.RingtoneManager;
import com.google.gson.Gson;
import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallUser;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.User;
import com.sendbird.calls.handler.DirectCallListener;


import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.coasapp.coas.call.CallService.EXTRA_CALLEE_ID;
import static com.coasapp.coas.call.CallService.EXTRA_CALLER_ID;
import static com.coasapp.coas.call.CallService.EXTRA_CALL_ID;
import static com.coasapp.coas.call.CallService.EXTRA_IS_END;
import static com.coasapp.coas.call.CallService.EXTRA_IS_VIDEO_CALL;

public abstract class CallActivity extends AppCompatActivity implements SensorEventListener {

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float dist = event.values[0];
        if (dist == 0) {
            wakeLock.acquire();
        } else {
            if (wakeLock.isHeld())
                wakeLock.release();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static boolean sIsRunning;

    private static final String TAG = "CallActivity";

    static final int ENDING_TIME_MS = 1000;
    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    enum STATE {
        STATE_INCOMING,
        STATE_ACCEPTING,
        STATE_OUTGOING,
        STATE_CONNECTED,
        STATE_ENDING,
        STATE_ENDED
    }

    Context mContext;
    private String mIncomingCallId;
    private boolean mIsEnd;
    private Timer mEndingTimer;

    STATE mState;
    String mCalleeId, mCallerName = "", mCallerId = "";
    boolean mIsVideoCall;
    DirectCall mDirectCall;
    boolean mIsAudioEnabled = true;

    //+ Views
    LinearLayout mLinearLayoutInfo;
    ImageView mImageViewProfile;
    TextView mTextViewUserId;
    TextView mTextViewStatus;

    LinearLayout mLinearLayoutRemoteMute;
    TextView mTextViewRemoteMute;

    RelativeLayout mRelativeLayoutRingingButtons;
    ImageView mImageViewDecline;
    ImageView mImageViewAccept;

    LinearLayout mLinearLayoutConnectingButtons;
    ImageView mImageViewAudioOff;
    ImageView mImageViewBluetooth;
    ImageView mImageViewEnd;
    //- Views

    //+ abstract methods
    protected abstract int getLayoutResourceId();

    protected abstract String[] getMandatoryPermissions();

    protected abstract void audioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices);

    protected abstract void startCall(boolean amICallee);

    SensorManager mSensorManager;
    Sensor mProximity;
    //- abstract methods
    RingtoneManager ringtoneManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       // Log.d(TAG, "onCreate()");

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(field, getLocalClassName());
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }


        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        setContentView(getLayoutResourceId());
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        mContext = this;
        sIsRunning = true;

        initViews();
        setViews();
        init(getIntent());

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_down_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

/*
        try {
            // Yeah, this is hidden field.
            field = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {
        }*/

        showOnGoingNotification();
    }

    void showOnGoingNotification() {

        Intent intentCall;

        if (mIsVideoCall) {
            intentCall = new Intent(getApplicationContext(), VideoCallActivity.class);
        } else {
            intentCall = new Intent(getApplicationContext(), VoiceCallActivity.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }

        intentCall.putExtra(EXTRA_CALL_ID, mIncomingCallId);
        intentCall.putExtra(EXTRA_CALLER_ID, mCallerId);
        intentCall.putExtra(EXTRA_CALLEE_ID, mCalleeId);
        intentCall.putExtra(EXTRA_IS_VIDEO_CALL, mIsVideoCall);
        intentCall.putExtra(EXTRA_IS_END, intentCall.getBooleanExtra(EXTRA_IS_END, false));
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ConversationActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);*/
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentCall, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.coasapp);
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.coas_icon192)
                .setLargeIcon(icon)

                .setContentTitle("Ongoing call")
                .setContentText("Tap to return")
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent);
//        if (!image.equalsIgnoreCase("")) {
//            notificationBuilder.setStyle(s);
//        }
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel("1", "Call", importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        if (notificationManager != null) {
            notificationManager.notify(12345, notificationBuilder.build());
        }
    }

    NotificationManager notificationManager;

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    private void init(Intent intent) {
        Intent intentRingtone = new Intent("ringtone");
        intentRingtone.putExtra("play", false);
        sendBroadcast(intentRingtone);
        CallService.stopService(getApplicationContext());
        mIncomingCallId = intent.getStringExtra(CallService.EXTRA_CALL_ID);
        mIsEnd = intent.getBooleanExtra(CallService.EXTRA_IS_END, false);

        if (mIsEnd) {
            CallService.stopService(mContext);
        }

        if (mIncomingCallId != null) {  // as callee
            mDirectCall = SendBirdCall.getCall(mIncomingCallId);
            mCalleeId = mDirectCall.getCallee().getUserId();

            mIsVideoCall = mDirectCall.isVideoCall();
            mCallerName = mDirectCall.getCaller().getNickname();
            mCallerId = mDirectCall.getCaller().getUserId();

            setListener(mDirectCall);
            Log.i("DirectCall", "Callee " + mCallerName);
        } else {    // as caller
            mCalleeId = intent.getStringExtra(CallService.EXTRA_CALLEE_ID);
            mCallerName = intent.getStringExtra(CallService.EXTRA_REMOTE_NICKNAME_OR_USER_ID);

            mIsVideoCall = intent.getBooleanExtra(CallService.EXTRA_IS_VIDEO_CALL, false);

            Log.i("DirectCall", "Caller " + mCallerName);
            ringtoneManager = new RingtoneManager(getApplicationContext(), R.raw.ringtone_outgoing);
            ringtoneManager.startOut(true, true);
        }

        if (setInitialState()) {
            checkAuthenticate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");

        mIsEnd = intent.getBooleanExtra(CallService.EXTRA_IS_END, false);
        if (mIsEnd) {
            CallService.stopService(mContext);
            end(mDirectCall);
        }
    }

    protected void initViews() {

        mLinearLayoutInfo = findViewById(R.id.linear_layout_info);
        mImageViewProfile = findViewById(R.id.image_view_profile);
        mTextViewUserId = findViewById(R.id.text_view_user_id);
        mTextViewStatus = findViewById(R.id.text_view_status);

        mLinearLayoutRemoteMute = findViewById(R.id.linear_layout_remote_mute);
        mTextViewRemoteMute = findViewById(R.id.text_view_remote_mute);

        mRelativeLayoutRingingButtons = findViewById(R.id.relative_layout_ringing_buttons);
        mImageViewDecline = findViewById(R.id.image_view_decline);
        mImageViewAccept = findViewById(R.id.image_view_accept);

        mLinearLayoutConnectingButtons = findViewById(R.id.linear_layout_connecting_buttons);
        mImageViewAudioOff = findViewById(R.id.image_view_audio_off);
        mImageViewBluetooth = findViewById(R.id.image_view_bluetooth);
        mImageViewEnd = findViewById(R.id.image_view_end);
    }

    protected void setViews() {
        mImageViewDecline.setOnClickListener(view -> {
            end(mDirectCall);
        });

        mImageViewAccept.setOnClickListener(view -> {
            if (SendBirdCall.getCurrentUser() == null) {
                Log.d(TAG, "mImageViewAccept clicked => (SendBirdCall.getCurrentUser() == null)");
                return;
            }

            if (mState == STATE.STATE_ENDING || mState == STATE.STATE_ENDED) {
                Log.d(TAG, "mImageViewAccept clicked => Already ending call.");
                return;
            }

            if (mState == STATE.STATE_ACCEPTING) {
                Log.d(TAG, "mImageViewAccept clicked => Already accepting call.");
                return;
            }

            setState(STATE.STATE_ACCEPTING, mDirectCall);
        });

        if (mIsAudioEnabled) {
            mImageViewAudioOff.setSelected(false);
        } else {
            mImageViewAudioOff.setSelected(true);
        }
        mImageViewAudioOff.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mIsAudioEnabled) {
                    Log.d(TAG, "mute()");
                    mDirectCall.muteMicrophone();
                    mIsAudioEnabled = false;
                    mImageViewAudioOff.setSelected(true);
                } else {
                    Log.d(TAG, "unmute()");
                    mDirectCall.unmuteMicrophone();
                    mIsAudioEnabled = true;
                    mImageViewAudioOff.setSelected(false);
                }
            }
        });

        mImageViewEnd.setOnClickListener(view -> {
            end(mDirectCall);
        });
    }

    protected void setListener(DirectCall call) {
        Log.d(TAG, "setListener()");

        call.setListener(new DirectCallListener() {
            @Override
            public void onConnected(DirectCall call) {
                Log.d(TAG, "onConnected()");
                if (ringtoneManager != null) {
                    ringtoneManager.stop();
                }
                setState(STATE.STATE_CONNECTED, call);

            }

            @Override
            public void onEnded(DirectCall call) {
                Log.d(TAG, "onEnded()");
                setState(STATE.STATE_ENDED, call);

                BroadcastUtils.sendCallLogBroadcast(mContext, call.getCallLog());
            }

            @Override
            public void onRemoteVideoSettingsChanged(DirectCall call) {
                Log.d(TAG, "onRemoteVideoSettingsChanged()");
            }

            @Override
            public void onRemoteAudioSettingsChanged(DirectCall call) {
                Log.d(TAG, "onRemoteAudioSettingsChanged()");
                setRemoteMuteInfo(call);
            }

            @Override
            public void onAudioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
                Log.d(TAG, "onAudioDeviceChanged(currentAudioDevice: " + currentAudioDevice + ", availableAudioDevices: " + availableAudioDevices + ")");
                audioDeviceChanged(call, currentAudioDevice, availableAudioDevices);
            }
        });
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private boolean setInitialState() {
        if (mIncomingCallId != null) {
            Log.d(TAG, "setInitialState() => (mIncomingCallId != null)");

            if (mDirectCall.isEnded()) {
                Log.d(TAG, "setInitialState() => (mDirectCall.isEnded() == true)");
                setState(STATE.STATE_ENDED, mDirectCall);
                return false;
            }

            //CallService.startService(mContext, mDirectCall, false);

            setState(STATE.STATE_INCOMING, mDirectCall);
        } else {
            setState(STATE.STATE_OUTGOING, mDirectCall);
        }
        return true;
    }

    private void checkAuthenticate() {
        if (SendBirdCall.getCurrentUser() == null) {
            AuthenticationUtils.autoAuthenticate(mContext, (userId, user) -> {
                if (userId == null) {
                    finishWithEnding("autoAuthenticate() failed.");
                    return;
                }
                checkPermissions();
            });
        } else {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : getMandatoryPermissions()) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(deniedPermissions.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
            } else {
                finishWithEnding("Permission denied.");
            }
        } else {
            ready();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allowed = true;

            for (int result : grantResults) {
                allowed = allowed && (result == PackageManager.PERMISSION_GRANTED);
            }

            if (allowed) {
                ready();
            } else {
                finishWithEnding("Permission denied.");
            }
        }
    }

    private void ready() {
        if (mIsEnd) {
            end(mDirectCall);
            return;
        }

        if (mState == STATE.STATE_OUTGOING) {
            startCall(false);
        } else if (mState == STATE.STATE_INCOMING) {
            setState(STATE.STATE_ACCEPTING, mDirectCall);
        }
    }

    protected boolean setState(STATE state, DirectCall call) {
        if (isFinishing()) {
            Log.d(TAG, "setState() => isFinishing()");
            return false;
        }

        mState = state;
        switch (state) {
            case STATE_INCOMING: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.VISIBLE);
                mLinearLayoutConnectingButtons.setVisibility(View.GONE);

                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_incoming_video_call));
                } else {
                    setInfo(call, getString(R.string.calls_incoming_voice_call));
                }

                mImageViewDecline.setBackgroundResource(R.drawable.btn_call_decline);
                break;
            }

            case STATE_ACCEPTING: {
                startCall(true);
                setInfo(call, getString(R.string.calls_connecting_call));
                break;
            }

            case STATE_OUTGOING: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mImageViewProfile.setVisibility(View.GONE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.VISIBLE);

                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_video_calling));
                } else {
                    setInfo(call, getString(R.string.calls_calling));
                }
                break;
            }

            case STATE_CONNECTED: {
                mImageViewProfile.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.VISIBLE);

                setRemoteMuteInfo(call);
                break;
            }

            case STATE_ENDING: {
                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_ending_video_call));
                } else {
                    setInfo(call, getString(R.string.calls_ending_voice_call));
                }
                break;
            }

            case STATE_ENDED: {
                if (ringtoneManager != null) {
                    ringtoneManager.stop();
                }
                setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mImageViewProfile.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.GONE);

                String status = getEndResultString(call);
                setInfo(call, status);
                finishWithEnding(status);
                notificationManager.cancel(12345);
                break;
            }
        }
        return true;
    }

    protected void setInfo(DirectCall call, String status) {
        DirectCallUser remoteUser = (call != null ? call.getRemoteUser() : null);
        Log.i("DirectCall", "" + new Gson().toJson(remoteUser));
        if (remoteUser != null) {
            UserInfoUtils.setProfileImage(mContext, remoteUser, mImageViewProfile);
            // UserInfoUtils.setNicknameOrUserId(remoteUser, mTextViewUserId);
            //mTextViewUserId.setText(APPHelper.getContactName(getApplicationContext(), mCallerId, remoteUser.getNickname()));
        } else {
            //mTextViewUserId.setText(APPHelper.getContactName(getApplicationContext(), mCallerId, mCallerName));
            mTextViewUserId.setText(mCallerName);
        }
        if (mIncomingCallId != null) {
            mTextViewUserId.setText(APPHelper.getContactName(getApplicationContext(), mCallerId, mCallerName));
        } else {
            mTextViewUserId.setText(APPHelper.getContactName(getApplicationContext(), mCalleeId, mCallerName));
        }


        mTextViewStatus.setVisibility(View.VISIBLE);
        if (status != null) {
            mTextViewStatus.setText(status);
        }
    }

    private void setRemoteMuteInfo(DirectCall call) {
        if (call != null && !call.isRemoteAudioEnabled() && call.getRemoteUser() != null) {
            String remoteUserId = call.getRemoteUser().getUserId();
            mTextViewRemoteMute.setText(getString(R.string.calls_muted_this_call, remoteUserId));
            mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutRemoteMute.setVisibility(View.GONE);
        }
    }

    private String getEndResultString(DirectCall call) {
        String endResultString = "";
        if (call != null) {
            switch (call.getEndResult()) {
                case NONE:
                    break;
                case NO_ANSWER:
                    endResultString = getString(R.string.calls_end_result_no_answer);
                    break;
                case CANCELED:
                    endResultString = getString(R.string.calls_end_result_canceled);
                    break;
                case DECLINED:
                    endResultString = getString(R.string.calls_end_result_declined);
                    break;
                case COMPLETED:
                    endResultString = getString(R.string.calls_end_result_completed);
                    break;
                case TIMED_OUT:
                    endResultString = getString(R.string.calls_end_result_timed_out);
                    break;
                case CONNECTION_LOST:
                    endResultString = getString(R.string.calls_end_result_connection_lost);
                    break;
                case UNKNOWN:
                    endResultString = getString(R.string.calls_end_result_unknown);
                    break;
                case DIAL_FAILED:
                    endResultString = getString(R.string.calls_end_result_dial_failed);
                    break;
                case ACCEPT_FAILED:
                    endResultString = getString(R.string.calls_end_result_accept_failed);
                    break;
                case OTHER_DEVICE_ACCEPTED:
                    endResultString = getString(R.string.calls_end_result_other_device_accepted);
                    break;
            }
        }
        return endResultString;
    }

    @Override
    public void onBackPressed() {
    }

    protected void end(DirectCall call) {
        if (call != null) {
            Log.d(TAG, "end(callId: " + call.getCallId() + ")");

            if (mState == STATE.STATE_ENDING || mState == STATE.STATE_ENDED) {
                Log.d(TAG, "Already ending call.");
                return;
            }

            setState(STATE.STATE_ENDING, call);
            call.end();
            /*SendBirdCall.removeAllListeners();

            ((ApplozicSampleApplication) getApplication()).initSendBirdCall(ApplozicSampleApplication.APP_ID1);*/
        }
    }

    protected void finishWithEnding(String log) {
        Log.d(TAG, "finishWithEnding(" + log + ")");
        if (mEndingTimer == null) {
            mEndingTimer = new Timer();
            mEndingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        Log.d(TAG, "finish()");
                        finish();
                        CallService.stopService(mContext);
                    });
                }
            }, ENDING_TIME_MS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        sIsRunning = true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            moveTaskToBack(true);
        }
        return super.onOptionsItemSelected(item);

    }
}
