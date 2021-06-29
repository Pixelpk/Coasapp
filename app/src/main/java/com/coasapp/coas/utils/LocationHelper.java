package com.coasapp.coas.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationHelper implements APPConstants {
    public static String getCurrentLocationString(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(APP_PREF, 0);
        String lat = sharedPreferences.getString("lat", "0");
        String lng = sharedPreferences.getString("lng", "0");
        return getAddress(activity, Double.parseDouble(lat), Double.parseDouble(lng));
        //return lat + "," + lng;

    }

    public static String getCurrentLocationLatLng(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(APP_PREF, 0);
        String lat = sharedPreferences.getString("lat", "0");
        String lng = sharedPreferences.getString("lng", "0");
        //return getAddress(activity,Double.parseDouble(lat),Double.parseDouble(lng));
        return lat + "," + lng;

    }

    public static String getAddress(Context activity, double lat1, double lng1) {
        Geocoder geocoder = new Geocoder(activity);
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    lat1, lng1,
                    // In this sample, get just a single address.
                    1);

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
            } else
                return lat1 + "," + lng1;
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e("Location", ioException.getMessage());
            return lat1 + "," + lng1;
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            return lat1 + "," + lng1;
        }
    }

    public static void openDirections(Context activity, String address) {
        Log.i("Address", address);
        Uri gmmIntentUri = Uri.parse("geo:" + address);
        Uri uri = Uri.parse("google.navigation:q=" + address.replace(" ", "+"));
        ;
        Intent intent = new Intent(Intent.ACTION_VIEW,
                uri);
        activity.startActivity(intent);
    }

    public static boolean checkGPS(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        APPHelper.showLog("LocationG", "" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        APPHelper.showLog("LocationN", "" + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        if (Build.VERSION.SDK_INT >= 28)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        else
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    public static boolean checkLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    public static boolean checkPermissionLocation(Context context) {
        boolean granted = false;
        granted = ContextCompat.checkSelfPermission(context
                ,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return granted;
    }

    public static void goToLocationSettings(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage("Turn on Location Services");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(myIntent, 500);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    public static LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 5 second delay between each request
        locationRequest.setFastestInterval(5000); // 5 seconds fastest time in between each request
        locationRequest.setSmallestDisplacement(0); // 10 meters minimum displacement for new location request
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
