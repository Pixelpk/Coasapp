package com.coasapp.coas.utils;

import android.util.Log;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.BuildConfig;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by User on 12-07-2017.
 */

public class RequestHandler {
    public static String CONNECT_ERROR = "Unable to connect!";
    public static int TIMEOUT = 30000;

    //Method to send httpPostRequest
    //This method is taking two arguments
    //First argument is the URL of the script to which we will send the request
    //Other is an HashMap with name value pairs containing the data to be send with the request
    public String sendPostRequest(String requestURL,
                                  HashMap<String, String> postDataParams) {
        //Creating a URL
        /*URL url;
        if (BuildConfig.DEBUG)
            Log.i("RequestHandlerAPI", requestURL);
        //StringBuilder object to store the message retrieved from the server
        StringBuilder sb = new StringBuilder();
        try {
            //Initializing Url
            url = new URL(requestURL);

            //Creating an httmlurl connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Configuring connection properties
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            //conn.addRequestProperty("Api-Token", ApplozicSampleApplication.API_TOKEN);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            //Creating an output stream
            OutputStream os = conn.getOutputStream();

            //Writing parameters to the request
            //We are using a method getPostDataString which is defined below
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams, requestURL));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                //Reading server response
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
            }
            if (BuildConfig.DEBUG)
                Log.d("RequestHandlerAPI", sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }

        return sb.toString();


        String errorMsg = "";*/
        String errorMsg = "";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        FormBody.Builder builder = new FormBody.Builder();
        StringBuilder postman = new StringBuilder(), constantDeclaration = new StringBuilder(), paramDeclaration = new StringBuilder();


        //JSONArray arrayParams = jsonObject.names();
        //Log.d("ParamKeys", jsonObject.toString());
        // Log.d("ParamKeys", arrayParams.toString());
        if (BuildConfig.DEBUG)
            Log.d("APIHandler ", requestURL);
        constantDeclaration.append("String ");
        for (Map.Entry<String, String> entry : postDataParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null)
                builder.add(key, value);
            postman.append(key);
            postman.append(":");
            postman.append(value);
            postman.append("\n");
            paramDeclaration.append("params.put(KEY_").append(entry.getKey().toUpperCase()).append(", ").append(entry.getValue()).append(");\n");

            constantDeclaration.append("KEY_").append(entry.getKey().toUpperCase()).append("=").append("\"").append(entry.getKey()).append("\"").append(",\n");
        }
        if (BuildConfig.DEBUG)
            Log.d("APIHandler ", postman.toString());
        //Log.d("APIHandler ", constantDeclaration.toString());
        //Log.d("APIHandler ", paramDeclaration.toString());
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(requestURL)
                .post(requestBody)
                /*.addHeader("Authorization", token)
                .addHeader("username", APP_USERNAME)
                .addHeader("password", APP_PASSWORD)*/
                .build();
        if (BuildConfig.DEBUG)
            Log.d("APIHandler ", postman.toString());
        //Log.d("APIHandler ", new Gson().toJson(requestBody));
        try {
            Response response = client.newCall(request).execute();
            String resp = response.body().string().replace("null", "\"\"");
            //Log.d("APIHandler", strings[1] + "\n" + strings[0] + "\n" + postman.toString() + "\n" + new MyPrefs(context, CommonConstants.SHARED_PREF).getString(UserData.KEY_AUTH));

            if (BuildConfig.DEBUG)
                Log.d("APIHandler ", resp.replace("null", "\"\""));

            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "Error: " + e.getMessage();
            /*WriteToFile.writeAPIResponse(
                    strings[0] + "\n" + postman.toString() + "\n" + errorMsg, context
            );*/
            Log.d("APIHandler ", errorMsg);
            return CONNECT_ERROR;
        }

    }

    public String sendPutRequest(String requestURL,
                                 JSONObject postDataParams) {
        //Creating a URL
        URL url;
        if (BuildConfig.DEBUG)
            Log.i("RequestHandlerAPI", requestURL);
        //StringBuilder object to store the message retrieved from the server
        StringBuilder sb = new StringBuilder();
        try {
            //Initializing Url
            url = new URL(requestURL);

            //Creating an httmlurl connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Configuring connection properties
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            conn.addRequestProperty("Api-Token", ApplozicSampleApplication.API_TOKEN);
            conn.setRequestMethod("PUT");
            conn.addRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            //Creating an output stream
            OutputStream os = conn.getOutputStream();

            //Writing parameters to the request
            //We are using a method getPostDataString which is defined below
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataParams.toString());
            Log.i("RequestHandlerAPI", postDataParams.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                //Reading server response
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
            }
            if (BuildConfig.DEBUG)
                Log.d("RequestHandlerAPI", sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }

        return sb.toString();

    }


    public String sendPostJsonRequest(String requestURL,
                                      JSONObject postDataParams) {
        //Creating a URL
        URL url;
        if (BuildConfig.DEBUG)
            Log.i("RequestHandlerAPI", requestURL);
        //StringBuilder object to store the message retrieved from the server
        StringBuilder sb = new StringBuilder();
        try {
            //Initializing Url
            url = new URL(requestURL);

            //Creating an httmlurl connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Configuring connection properties
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            conn.addRequestProperty("Api-Token", ApplozicSampleApplication.API_TOKEN);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            //Creating an output stream
            OutputStream os = conn.getOutputStream();

            //Writing parameters to the request
            //We are using a method getPostDataString which is defined below
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataParams.toString());
            Log.i("RequestHandlerAPI", postDataParams.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                //Reading server response
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
            }
            if (BuildConfig.DEBUG)
                Log.d("RequestHandlerAPI", sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return CONNECT_ERROR;
        }

        return sb.toString();

    }

    public String sendGetRequest(String requestURL) {
      /*  StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s + "\n");
            }
            APPHelper.writeToFile(requestURL, requestURL.substring(requestURL.lastIndexOf("/") + 1));
            if (BuildConfig.DEBUG)
                Log.d("RequestHandler", url + " " + sb.toString());
        } catch (Exception e) {
            return "Error";
        }
        return sb.toString();*/


        String errorMsg = "";
        Request request = new Request.Builder()
                .url(requestURL)
                /*.addHeader("Authorization", token)
               .addHeader("username", APP_USERNAME)
               .addHeader("password", APP_PASSWORD)*/
                .build();


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        try {

            Response response = client.newCall(request).execute();
            String resp = response.body().string();
            //Log.d("APIHandler", strings[1] + "\n" + strings[0] + "\n" + postman.toString() + "\n" + new MyPrefs(context, CommonConstants.SHARED_PREF).getString(UserData.KEY_AUTH));


            Log.d("APIHandler ", resp.replace("null", "\"\""));

            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "Error: " + e.getMessage();
            /*WriteToFile.writeAPIResponse(
                    strings[0] + "\n" + postman.toString() + "\n" + errorMsg, context
            );*/
            Log.d("APIHandler ", errorMsg);
            return CONNECT_ERROR;
        }
    }

    public String sendGetRequestParam(String requestURL, String data) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(requestURL + data);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s + "\n");
            }
            //Log.d("RequestHandler", sb.toString());
        } catch (Exception e) {
            return sb.toString();
        }
        return sb.toString();
    }

    private String getPostDataString(HashMap<String, String> params, String url) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        StringBuilder postman = new StringBuilder();
        StringBuilder keys = new StringBuilder();
        boolean first = true;
        //Log.d("RequestHandlerAPI", "\n" + params.toString());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            postman.append(entry.getKey());
            postman.append(":");
            postman.append(entry.getValue()).append("\n");
            keys.append(entry.getKey()).append("\n");
            if (first)
                first = false;

            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));

        }
        APPHelper.writeToFile(postman.toString(), url.substring(url.lastIndexOf("/") + 1));
        if (BuildConfig.DEBUG)
            Log.d("RequestHandlerAPI", "\n" + postman.toString());
        //Log.d("RequestHandler", "\n" + keys.toString());
        if (BuildConfig.DEBUG)
            Log.d("RequestHandler", result.toString());

        return result.toString();


    }

}
