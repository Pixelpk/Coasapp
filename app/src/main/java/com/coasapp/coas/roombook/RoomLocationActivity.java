package com.coasapp.coas.roombook;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class RoomLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    String address;
    double roomLat, roomLng;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_location);
        getSupportActionBar().hide();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        String details = getIntent().getStringExtra("details");
        try {
            JSONObject object = new JSONObject(details);
            roomLat = Double.parseDouble(object.getString("room_latitude"));
            roomLng = Double.parseDouble(object.getString("room_longitude"));
            address = object.getString("room_street");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        APPHelper.setMapStyle(this, googleMap);
        mMap.addMarker(new MarkerOptions().position(new LatLng(roomLat, roomLng)).title(address).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue)));

    }
}
