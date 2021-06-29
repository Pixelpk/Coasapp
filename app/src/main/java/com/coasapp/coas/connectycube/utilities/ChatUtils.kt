package com.coasapp.coas.connectycube.utilities

import android.util.Base64
import org.json.JSONObject

class ChatUtils {

    fun getPhoneObj(data: String): JSONObject {
        var data1 = data
        if (!data1.contains("\"name\"") || !data1.contains("\"phone\"")) {
            data1 = String(Base64.decode(data, Base64.NO_WRAP));
        }
        val obj = JSONObject(data1);
        return obj
    }


    fun getLocObj(data: String): JSONObject {
        var data1 = data
        if (!data1.contains("\"location\"")) {
            data1 = String(Base64.decode(data, Base64.NO_WRAP));
        }
        val obj = JSONObject(data1);
        return obj
    }
}