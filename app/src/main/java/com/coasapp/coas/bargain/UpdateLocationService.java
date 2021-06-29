package com.coasapp.coas.bargain;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import java.util.LinkedHashMap;

public class UpdateLocationService extends Service implements APPConstants {

    private FusedLocationProviderClient mFusedLocationClient; // Object used to receive location updates
    private LocationRequest locationRequest; // Object that defines important parameters regarding location request.

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        APPHelper.showLog("Location","Create");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 second delay between each request
        locationRequest.setFastestInterval(5000); // 5 seconds fastest time in between each request
        locationRequest.setSmallestDisplacement(1); // 10 meters minimum displacement for new location request
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // enables GPS high accuracy location requests
        sendUpdatedLocationMessage();
    }

    private void sendUpdatedLocationMessage() {
        final SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        APPHelper.showLog("Location","Start");
        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Location location = locationResult.getLastLocation();
                    LinkedHashMap<String, String> message = getNewLocationMessage(location.getLatitude(), location.getLongitude());
                    /*COASHomeActivity.pubnub.publish()
                            .message(message)
                            .channel(sharedPreferences.getString("coasId",""))
                            .async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult result, PNStatus status) {
                                    // handle publish result, status always present, result if successful
                                    // status.isError() to see if error happened
                                    if (!status.isError()) {
                                        System.out.println("pub timetoken: " + result.getTimetoken());
                                    }
                                    System.out.println("pub status code: " + status.getStatusCode());
                                }
                            });*/
                }
            }, Looper.myLooper());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private LinkedHashMap<String, String> getNewLocationMessage(double lat, double lng) {
        SharedPreferences sharedPreferences = getSharedPreferences(CAR_DETAILS, Context.MODE_PRIVATE);
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("lat", String.valueOf(lat));
        map.put("lng", String.valueOf(lng));
        map.put("veh_id", sharedPreferences.getString("vehicle_id", "0"));
        map.put("veh_name", sharedPreferences.getString("vehicle_name", ""));
        map.put("veh_num", sharedPreferences.getString("vehicle_num", "0"));
        map.put("veh_desc", sharedPreferences.getString("vehicle_desc", "0"));
        map.put("seats", sharedPreferences.getString("seats", "4"));
        map.put("veh_cat", sharedPreferences.getString("category", ""));
        map.put("veh_image", sharedPreferences.getString("vehicle_image", ""));
        APPHelper.showLog("CarDetails", map.toString());
        return map;
    }
}
