package com.coasapp.coas.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class SortAppUser implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        try {
            return o1.getString("name").compareTo(o2.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }
}