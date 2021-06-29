package com.coasapp.coas.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

public class PostRequestAsyncTask extends AsyncTask<String, Void, String> {

    Context context;
    HashMap<String, String> map;
    String type = "";
    APICallbacks apiCallbacks;




    public PostRequestAsyncTask( Context context,APICallbacks apiCallbacks, HashMap<String, String> map, String type) {
        this.apiCallbacks = apiCallbacks;
        this.context = context;
        this.map = map;
        this.type = type;
    }

    public PostRequestAsyncTask(Context context, HashMap<String, String> map, String type, APICallbacks apiCallbacks) {
        this.context = context;
        this.map = map;
        this.type = type;
        this.apiCallbacks = apiCallbacks;
    }

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
        return new RequestHandler().sendPostRequest(strings[0], map);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("Task", "Cancelled");
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        apiCallbacks.taskEnd(getType(), s.replace("null","\"\""));
    }
}
