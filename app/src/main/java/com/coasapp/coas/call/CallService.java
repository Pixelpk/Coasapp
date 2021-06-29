package com.coasapp.coas.call;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.AuthenticationUtils;
import com.coasapp.coas.utils.UserInfoUtils;
import com.connectycube.messenger.helpers.RingtoneManager;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;


public class CallService extends Service {

    private static final String TAG = "CallService";
    private static final int NOTIFICATION_ID = 1;

    public static final String EXTRA_REMOTE_NICKNAME_OR_USER_ID = "remote_user_id";
    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_CALLER_ID = "caller_id";
    public static final String EXTRA_CALLEE_ID = "callee_id";
    public static final String EXTRA_IS_VIDEO_CALL = "is_video_call";
    public static final String EXTRA_IS_HEADS_UP_NOTIFICATION = "is_heads_up_notification";
    public static final String EXTRA_IS_END = "is_end";

    private Context mContext;

    private String mRemoteNicknameOrUserId;
    private String mCallId;
    private String mCalleeId;
    private boolean mIsVideoCall;
    RingtoneManager ringtoneManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        Intent intentRingtone = new Intent("ringtone");
        intentRingtone.putExtra("play", true);
        sendBroadcast(intentRingtone);
        ringtoneManager = new RingtoneManager(getApplicationContext());
        ringtoneManager.start(true, true);
        mRemoteNicknameOrUserId = intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID);
        mCallId = intent.getStringExtra(EXTRA_CALL_ID);
        mCalleeId = intent.getStringExtra(EXTRA_CALLEE_ID);
        mIsVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false);
        boolean isHeadsUpNotification = intent.getBooleanExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, false);

        Notification notification = getNotification(isHeadsUpNotification, mRemoteNicknameOrUserId, true, mCallId, mCalleeId, intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID), mIsVideoCall);
        startForeground(NOTIFICATION_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ringtoneManager != null) {
            ringtoneManager.stop();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved()");

        Notification notification = getNotification(true, mRemoteNicknameOrUserId, false, mCallId, mCalleeId, "", mIsVideoCall);
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification getNotification(boolean isHeadsUpNotification, String remoteNicknameOrUserId, boolean hasCallButton,
                                         String callId, String calleeId, String name, boolean isVideoCall) {

        String content = "Incoming Call";
        if (isVideoCall) {
            content = mContext.getString(R.string.calls_notification_video_calling_content, mContext.getString(R.string.app_name));
            content = "Video Call " + APPHelper.getContactName(mContext, "" + SendBirdCall.getCall(callId).getCaller().getUserId(), name);
        } else {
            content = mContext.getString(R.string.calls_notification_voice_calling_content, mContext.getString(R.string.app_name));
            content = "Audio Call " + APPHelper.getContactName(mContext, "" + SendBirdCall.getCall(callId).getCaller().getUserId(), name);

        }

        final int currentTime = (int) System.currentTimeMillis();
        final String channelId = mContext.getPackageName() + currentTime;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = mContext.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    isHeadsUpNotification ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent callIntent = getCallIntent(mContext, callId, calleeId, name, isVideoCall, false);
        Intent endIntent = getCallIntent(mContext, callId, calleeId, name, isVideoCall, true);
        PendingIntent callPendingIntent = PendingIntent.getActivity(mContext, (currentTime + 1), callIntent, 0);
        PendingIntent endPendingIntent = PendingIntent.getActivity(mContext, (currentTime + 2), endIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);
        builder.setContentTitle(remoteNicknameOrUserId)
                .setContentText(content)
                .setSmallIcon(R.mipmap.coas_icon192)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.coas_icon192))
                .setPriority(isHeadsUpNotification ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_LOW)
                .addAction(new NotificationCompat.Action(0, mContext.getString(R.string.calls_notification_end), endPendingIntent));

        if (hasCallButton) {
            builder.addAction(new NotificationCompat.Action(0, mContext.getString(R.string.calls_notification_call), callPendingIntent));
        }
        Intent fullScreenIntent = getCallIntent1(
                mContext,
                callId,
                calleeId,
                name,
                isVideoCall,
                false
        );
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
        return builder.build();
    }

    private static Intent getCallIntent(Context context, String callId, String calleeId, String name, boolean isVideoCall, boolean isEnd) {
        final Intent intent;
        if (isVideoCall) {
            intent = new Intent(context, VideoCallActivity.class);
        } else {
            intent = new Intent(context, VoiceCallActivity.class);
        }

        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, name);
        intent.putExtra(EXTRA_CALLEE_ID, calleeId);
        intent.putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall);
        intent.putExtra(EXTRA_IS_END, isEnd);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    public static Intent getCallIntent1(Context context, String callId, String calleeId, String name, boolean isVideoCall, boolean isEnd) {
        final Intent intent;
       /* if (isVideoCall) {
            intent = new Intent(context, VideoCallActivity.class);
        } else {
            intent = new Intent(context, VoiceCallActivity.class);
        }*/
        intent = new Intent(context, IncomingCallRingActivity.class);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, name);
        intent.putExtra(EXTRA_CALLEE_ID, calleeId);
        intent.putExtra(EXTRA_CALLER_ID, SendBirdCall.getCall(callId).getCaller().getUserId());
        intent.putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall);
        intent.putExtra(EXTRA_IS_END, isEnd);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    public static void startCallActivity(Context context, String calleeId, String name, boolean isVideoCall) {
     //   Log.d(TAG, "startCallActivity()");
        Intent intent = getCallIntent(context, null, calleeId, name, isVideoCall, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        context.startActivity(intent);
    }

    public static void startService(Context context, DirectCall call, boolean isHeadsUpNotification) {
        Log.d(TAG, "startService()");

      /*  if (context != null) {
            Intent intent = new Intent(context, CallService.class);
            intent.putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, APPHelper.getContactName(
                    context, call.getCaller().getUserId(), call.getCaller().getNickname()
            ));
            intent.putExtra(EXTRA_CALL_ID, call.getCallId());
            intent.putExtra(EXTRA_CALLEE_ID, call.getCallee().getUserId());
            intent.putExtra(EXTRA_IS_VIDEO_CALL, call.isVideoCall());
            intent.putExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, isHeadsUpNotification);

            context.startService(intent);
        }*/


        if (SendBirdCall.getCurrentUser() == null) {
            AuthenticationUtils.autoAuthenticate(context, (userId, user) -> {
                if (userId == null) {
                    return;
                }
                startServiceA(context,call,isHeadsUpNotification);
            });
        } else {
            startServiceA(context,call,isHeadsUpNotification);
        }
    }


    public static void startServiceA(Context context, DirectCall call, boolean isHeadsUpNotification) {
        Log.d(TAG, "startService()");

        if (context != null) {
            Intent intent = new Intent(context, CallService.class);
            intent.putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, APPHelper.getContactName(
                    context, call.getCaller().getUserId(), call.getCaller().getNickname()
            ));
            intent.putExtra(EXTRA_CALL_ID, call.getCallId());
            intent.putExtra(EXTRA_CALLEE_ID, call.getCallee().getUserId());
            intent.putExtra(EXTRA_IS_VIDEO_CALL, call.isVideoCall());
            intent.putExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, isHeadsUpNotification);

            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Log.d(TAG, "stopService()");

        if (context != null) {
            Intent intent = new Intent(context, CallService.class);
            context.stopService(intent);
        }
    }
}
