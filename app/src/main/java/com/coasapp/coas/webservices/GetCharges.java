package com.coasapp.coas.webservices;

import android.os.AsyncTask;

import com.coasapp.coas.utils.ChargeAsyncCallbacks;

import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import java.util.HashMap;

public class GetCharges extends AsyncTask<String, Void, String> implements APPConstants {
    ChargeAsyncCallbacks chargeAsyncCallbacks;
    HashMap<String, String> map;


    public GetCharges(ChargeAsyncCallbacks chargeAsyncCallbacks, HashMap<String, String> map) {
        this.chargeAsyncCallbacks = chargeAsyncCallbacks;
        this.map = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        chargeAsyncCallbacks.onTaskStart();
    }

    @Override
    protected String doInBackground(String... strings) {

        return new RequestHandler().sendPostRequest(MAIN_URL + "get_charges.php", map);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        chargeAsyncCallbacks.onTaskEnd(s);
    }
}
