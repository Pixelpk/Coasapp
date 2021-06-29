package com.coasapp.coas.utils;

import android.view.View;

import com.connectycube.chat.model.ConnectycubeChatMessage;

public interface ChatMessageLongClick {
    void onLongClick(View view, ConnectycubeChatMessage chatMessage);
}
