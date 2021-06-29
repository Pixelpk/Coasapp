package com.coasapp.coas.utils;

import com.connectycube.chat.model.ConnectycubeChatDialog;

import java.util.Comparator;
import java.util.Map;

public class SortChatDialogs implements Comparator<ConnectycubeChatDialog> {
    @Override
    public int compare(ConnectycubeChatDialog o1, ConnectycubeChatDialog o2) {

        return Long.compare(o2.getLastMessageDateSent(),o1.getLastMessageDateSent());

    }
}