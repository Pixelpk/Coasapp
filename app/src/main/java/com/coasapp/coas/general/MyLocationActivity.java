package com.coasapp.coas.general;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.LocationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MyLocationActivity extends MyAppCompatActivity implements OnMapReadyCallback, APPConstants {

    String[] permissions = new String[]{ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    double lat, lng;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            Location location = locationResult.getLastLocation();
            lat = (location.getLatitude());
            lng = (location.getLongitude());
            SharedPreferences.Editor editor = getSharedPreferences(APP_PREF, 0).edit();
            editor.putString("lat", String.valueOf(lat));
            editor.putString("lng", String.valueOf(lng));
            editor.apply();
            mMap.clear();
            LatLng sydney = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(sydney).title(LocationHelper.getCurrentLocationString(MyLocationActivity.this)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f));
            findViewById(R.id.buttonOK).setVisibility(View.VISIBLE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationHelper.getLocationRequest();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.buttonOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();
                try {
                    String latLngStr = LocationHelper.getCurrentLocationLatLng(MyLocationActivity.this);
                    String locationAddress = LocationHelper.getCurrentLocationString(MyLocationActivity.this);
                    String image = "https://maps.googleapis.com/maps/api/staticmap?center" + locationAddress.replace(" ", "+").replace(", ", ",") + "&zoom=15&size=600x300&maptype=roadmap&markers=color:green%7C" + latLngStr + "&key=" + getString(R.string.google_maps_key);
                    object.put("location_lat_lng", LocationHelper.getCurrentLocationLatLng(MyLocationActivity.this));
                    object.put("location_address", LocationHelper.getCurrentLocationString(MyLocationActivity.this));
                    object.put("location_image", image);
                    Intent intent = new Intent();
                    intent.putExtra("location", object.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        APPHelper.setMapStyle(this, googleMap);
        mMap = googleMap;
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);

        // Add a marker in Sydney and move the camera
        lat = Double.valueOf(sharedPreferences.getString("lat", "0"));
        lng = Double.valueOf(sharedPreferences.getString("lng", "0"));
        LatLng sydney = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getLoc();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            getLoc();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            if (LocationHelper.checkGPS(getApplicationContext())) {
                getLoc();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    void getLoc() {

        if (APPHelper.checkPermissionsGranted(getApplicationContext(), new String[]{ACCESS_FINE_LOCATION})) {
            if (LocationHelper.checkGPS(getApplicationContext())) {
                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
                LocationHelper.goToLocationSettings(MyLocationActivity.this);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, 99);
        }
    }
}
