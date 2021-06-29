package com.coasapp.coas.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

public class APIService implements APPConstants {
    APICallbacks apiCallbacks;
    Context context;

    public APIService(APICallbacks apiCallbacks, Context context) {
        this.apiCallbacks = apiCallbacks;
        this.context = context;
    }


    public void callAPI(final HashMap<String, String> map, final String url, final String type) {
        Log.i("Map", map.toString());
        class CallAPITask extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                apiCallbacks.taskStart();
            }

            @Override
            protected String doInBackground(Void... voids) {

                return new RequestHandler().sendPostRequest(url, map);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                apiCallbacks.taskEnd(type, s);
            }


        }

        new CallAPITask().execute();
    }

    public void callAPIGET(final String url, final String type) {
        class CallAPITask extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                apiCallbacks.taskStart();
            }

            @Override
            protected String doInBackground(Void... voids) {

                return new RequestHandler().sendGetRequest(url);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                apiCallbacks.taskEnd(type, s);
            }


        }

        new CallAPITask().execute();
    }

    public void callAPIFile(final HashMap<String, String> map, final String url, final String filePath, final String fileField, final String mimeType, final String type) {
        class CallAPIFile extends AsyncTask<Integer, Integer, String> {

            int index;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                apiCallbacks.taskStart();
            }

            @Override
            protected String doInBackground(Integer... params) {
                Log.d("image", "");
                //URL = "http://192.168.10.208/coas/webservices/upload_test_image.php";
                // URL  = "http://gonextinfo.site/coas/webservices/upload_test_image.php";

                //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
                HashMap<String, String> map = new HashMap<>();
                map.put("username", "job_app");
                map.put("password", "abcd@12345");
                //map.put("file", "");

                UploadMultipart multipart = new UploadMultipart(context);
                String res = multipart.multipartRequest(url, map, filePath, fileField, mimeType);
                //res = new RequestHandler().sendPostRequest(getApplicationContext(), URL, map);
                Log.d("res", res);
                return res;


            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                apiCallbacks.taskEnd(type, s);

            }
        }
        new CallAPIFile().execute();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
