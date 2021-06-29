package com.coasapp.coas.utils;

public interface APICallbacks {
    void taskStart();
    void taskEnd(String type, String response);
}
