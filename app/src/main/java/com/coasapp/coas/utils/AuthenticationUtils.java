package com.coasapp.coas.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coasapp.coas.R;
import com.sendbird.calls.AuthenticateParams;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.User;

import kotlinx.coroutines.channels.Send;


public class AuthenticationUtils {

    private static final String TAG = "AuthenticationUtils";

    public interface AuthenticateHandler {
        void onResult(boolean isSuccess, User user);
    }

    public static void authenticate(Context context, String userId, String accessToken, AuthenticateHandler handler) {
        Log.d(TAG, "authenticate()");

        if (userId == null) {
            Log.d(TAG, "authenticate() => Failed (userId == null)");
            if (handler != null) {
                handler.onResult(false, null);
            }
            return;
        }

        PushUtils.getPushToken(context, (pushToken, e) -> {
            if (e != null) {
                Log.d(TAG, "authenticate() => Failed (e: " + e.getMessage() + ")");
                if (handler != null) {
                    handler.onResult(false, null);
                }
                return;
            }

            final User[] userCurrent = {null};

            Log.d(TAG, "authenticate() => authenticate()");
            SendBirdCall.authenticate(new AuthenticateParams(userId).setAccessToken(accessToken), (user, e1) -> {
                if (e1 != null) {
                    Log.d(TAG, "authenticate() => authenticate() => Failed (e1: " + e1.getMessage() + ")");
                    showToastErrorMessage(context, e1);

                    if (handler != null) {
                        handler.onResult(false,null);
                    }
                    return;
                }
                userCurrent[0] = user;

                Log.d(TAG, "authenticate() => registerPushToken()");
                SendBirdCall.registerPushToken(pushToken, true, e2 -> {
                    if (e2 != null) {
                        Log.d(TAG, "authenticate() => registerPushToken() => Failed (e2: " + e2.getMessage() + ")");
                        showToastErrorMessage(context, e2);

                        if (handler != null) {
                            handler.onResult(false, null);
                        }
                        return;
                    }
                    else{
                        Log.d(TAG, "authenticate() => registerPushToken() => Success "+pushToken);
                    }

                    PrefUtils.setAppId(context, SendBirdCall.getApplicationId());
                    PrefUtils.setUserId(context, userId);
                    PrefUtils.setAccessToken(context, accessToken);
                    PrefUtils.setPushToken(context, pushToken);

                    Log.d(TAG, "authenticate() => authenticate() => OK");
                    if (handler != null) {
                        handler.onResult(true,userCurrent[0]);
                    }
                });
            });
        });
    }

    public interface DeauthenticateHandler {
        void onResult(boolean isSuccess);
    }

    public static void deauthenticate(Context context, DeauthenticateHandler handler) {
        Log.d(TAG, "deauthenticate()");

        String pushToken = PrefUtils.getPushToken(context);
        if (!TextUtils.isEmpty(pushToken)) {
            SendBirdCall.unregisterPushToken(pushToken, e -> {
                if (e != null) {
                    Log.d(TAG, "unregisterPushToken() => Failed (e: " + e.getMessage() + ")");
                    showToastErrorMessage(context, e);
                }

                doDeauthenticate(context, handler);
            });
        } else {
            doDeauthenticate(context, handler);
        }
    }

    private static void doDeauthenticate(Context context, DeauthenticateHandler handler) {
        SendBirdCall.deauthenticate(e -> {
            if (e != null) {
                Log.d(TAG, "deauthenticate() => Failed (e: " + e.getMessage() + ")");
                showToastErrorMessage(context, e);
            } else {
                Log.d(TAG, "deauthenticate() => OK");
            }

            PrefUtils.setUserId(context, null);
            PrefUtils.setAccessToken(context, null);
            PrefUtils.setCalleeId(context, null);
            PrefUtils.setPushToken(context, null);

            if (handler != null) {
                handler.onResult(e == null);
            }
        });
    }

    public interface AutoAuthenticateHandler {
        void onResult(String userId, User user);
    }

    public static void autoAuthenticate(Context context, AutoAuthenticateHandler handler) {
        Log.d(TAG, "autoAuthenticate()");

        if (SendBirdCall.getCurrentUser() != null) {
            Log.d(TAG, "autoAuthenticate() => OK (SendBirdCall.getCurrentUser() != null)");
            if (handler != null) {
                handler.onResult(SendBirdCall.getCurrentUser().getUserId(), SendBirdCall.getCurrentUser());
            }
            return;
        }

        String userId = PrefUtils.getUserId(context);
        String accessToken = PrefUtils.getAccessToken(context);
        String pushToken = PrefUtils.getPushToken(context);
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(pushToken)) {
            Log.d(TAG, "autoAuthenticate() => authenticate()");
            SendBirdCall.authenticate(new AuthenticateParams(userId).setAccessToken(accessToken), (user, e) -> {
                if (e != null) {
                    Log.d(TAG, "autoAuthenticate() => authenticate() => Failed (e: " + e.getMessage() + ")");
                    showToastErrorMessage(context, e);

                    if (handler != null) {
                        handler.onResult(null, user);
                    }
                    return;
                }

                Log.d(TAG, "autoAuthenticate() => registerPushToken()");
                SendBirdCall.registerPushToken(pushToken, true, e1 -> {
                    if (e1 != null) {
                        Log.d(TAG, "autoAuthenticate() => registerPushToken() => Failed (e1: " + e1.getMessage() + ")");
                        showToastErrorMessage(context, e1);

                        if (handler != null) {
                            handler.onResult(null, user);
                        }
                        return;
                    }
                    else{
                        Log.d(TAG, "authenticate() => registerPushToken() => Success "+pushToken);
                    }

                    Log.d(TAG, "autoAuthenticate() => authenticate() => OK (Authenticated)");
                    if (handler != null) {
                        handler.onResult(userId, user);
                    }
                });
            });
        } else {
            Log.d(TAG, "autoAuthenticate() => Failed (No userId and pushToken)");
            if (handler != null) {
                handler.onResult(null, SendBirdCall.getCurrentUser());
            }
        }
    }

    private static void showToastErrorMessage(Context context, SendBirdException e) {
        if (context != null) {
            if (e.getCode() == 1800200) {
                ToastUtils.showToast(context, context.getString(R.string.calls_invalid_notifications_not_active_for_qrcode));
            } else {
                ToastUtils.showToast(context, e.getMessage());
            }
        }
    }
}
