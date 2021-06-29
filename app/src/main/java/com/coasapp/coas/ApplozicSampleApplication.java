package com.coasapp.coas;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;

import com.coasapp.coas.call.CallActivity;
import com.coasapp.coas.call.CallService;
import com.connectycube.auth.session.ConnectycubeSettings;

import com.connectycube.messenger.utilities.SettingsProvider;
import com.flurry.android.FlurryAgent;
import com.google.android.libraries.places.api.Places;
import com.coasapp.coas.utils.APPConstants;
import com.google.gson.Gson;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.handler.SendBirdCallListener;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import timber.log.Timber;


/**
 * Created by sunil on 21/3/16.
 */
public class ApplozicSampleApplication extends Application implements APPConstants, LifecycleObserver {
    static final String APP_ID = "1330";
    static final String AUTH_KEY = "2ZyeRQr87ru3zdk";
    static final String AUTH_SECRET = "MVZzmFePbTsOh5k";
    static final String ACCOUNT_KEY = "uzx6JASMq-G-3iKNbby1";

    public static final String TAG = "BaseApplication";


    // Refer to "https://github.com/sendbird/quickstart-calls-android".
    //public static final String APP_ID1 = "9A5EB080-89B1-48E8-8DC2-2A36B8D4B0B8", API_TOKEN = "0b39019a1e7a5973cfa59339826f757be992048c";
    public static final String APP_ID1 = "4D06F90E-0CBE-4580-A4F9-7CE278C39543", API_TOKEN = "10c358300af363e4736579390e551a4723a68ae0";

    //
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d("AppController", "Foreground");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {

    }

    public SimpleDateFormat getSdfNativeDevice() {
        return sdfNativeDevice;
    }

    SimpleDateFormat sdfNativeDevice = new SimpleDateFormat("MM-dd-yyyy h:mm a", Locale.getDefault());

    public SimpleDateFormat getSdfDatabaseFormat() {
        sdfDatabaseFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdfDatabaseFormat;
    }

    SimpleDateFormat sdfDatabaseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    public void onCreate() {
        super.onCreate();
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
       /* ConnectycubeSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        ConnectycubeSettings.getInstance().setAccountKey(ACCOUNT_KEY);*/
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));


        SettingsProvider.INSTANCE.initConnectycubeCredentials(this);
        SettingsProvider.INSTANCE.initChatConfiguration();

        //TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "ZVMV327F9HZRN7VT6JHB");

        initSendBirdCall(APP_ID1);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean initSendBirdCall(String appId) {
        Log.d(TAG, "initSendBirdCall(appId: " + appId + ")");
        Context context = getApplicationContext();

        if (TextUtils.isEmpty(appId)) {
            appId = APP_ID1;
        }

        if (SendBirdCall.init(context, appId)) {
            SendBirdCall.removeAllListeners();
            SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
                @Override
                public void onRinging(DirectCall call) {
                    Log.d(TAG, "onRinging() => callId: " + call.getCallId());
                    Log.d(TAG, "onRinging() => callDetails: " + call.getCaller().getUserId()+" "+call.getCallee().getUserId());
                    if (CallActivity.sIsRunning) {
                        call.end();
                        return;
                    }

                    call.setListener(new DirectCallListener() {
                        @Override
                        public void onConnected(DirectCall call) {
                            Log.d(TAG, "onConnected() => callId: " + call.getCallId());
                        }

                        @Override
                        public void onEnded(DirectCall call) {
                            if (!CallActivity.sIsRunning) {
                                Log.d(TAG, "onEnded() => callId: " + call.getCallId());
                                CallService.stopService(context);
                                Intent intent = new Intent("call_end");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            }
                        }
                    });

                    //startActivity(intent);

                    CallService.startService(context, call, true);
                }
            });
            return true;
        }
        return false;
    }

}
