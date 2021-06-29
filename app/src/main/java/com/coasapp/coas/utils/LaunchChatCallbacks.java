package com.coasapp.coas.utils;

import android.content.Intent;

public interface LaunchChatCallbacks {
    void onChatCreatedSuccess(Intent intent);
    void onChatCreatedError();
}
