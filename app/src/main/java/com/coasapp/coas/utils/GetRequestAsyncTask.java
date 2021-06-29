package com.coasapp.coas.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

public class GetRequestAsyncTask extends AsyncTask<String, Void, String> {

    Context context;

    public GetRequestAsyncTask(Context context, APICallbacks apiCallbacks) {
        this.context = context;
        this.apiCallbacks = apiCallbacks;
    }

    APICallbacks apiCallbacks;
    HashMap<String, String> map;

    public void setMapAndType(HashMap<String, String> map, String type) {
        setMap(map);
        setType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type = "";

    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        apiCallbacks.taskStart();
    }

    @Override
    protected String doInBackground(String... strings) {
        //Log.i("Map", map.toString());
        return new RequestHandler().sendGetRequest(strings[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("Task", "Cancelled");
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        apiCallbacks.taskEnd(getType(), s);
    }
}
