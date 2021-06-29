package com.coasapp.coas.utils;

public interface DownloadCallbacks {
    void downloadStart();

    void downloadProgressUpdate(int progress);

    void downloadComplete(String requestCode, String file);
}
