package com.coasapp.coas.general;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.VoiceRecordListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity implements OnMapReadyCallback, APPConstants {

    private GoogleMap mMap;
    Geocoder geocoder;
    List<Address> addresses = new ArrayList<>();
    String address = "";
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    public static String TAG = "Address";
    Double lat, lng;
    AutocompleteSupportFragment autocompleteFragment;

    VoiceRecordListener voiceRecordListener=new VoiceRecordListener() {
        @Override
        public void recordingStart() {

        }

        @Override
        public void recordingStop(String file) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        Places.initialize(getApplicationContext(),  getString(R.string.google_maps_key));

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        geocoder = new Geocoder(this);
        Toast.makeText(this, "Long Press to select location", Toast.LENGTH_LONG).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.buttonSelect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("address", address);
                intent.putExtra("latitude", lat);
                intent.putExtra("longitude", lng);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
                getCurrentAddress();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(address)).showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                findViewById(R.id.buttonSelect).setVisibility(View.VISIBLE);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        } else {
            //googleMap.setMyLocationEnabled(true);

        }
        // Add a marker in Sydney and move the camera
     /*   LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

       /* mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                double lat1 = point.latitude;
                double lng1 = point.longitude;
                getCurrentAddress(lat1, lng1);
                //mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(latLngS).title(editTextSource.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue)));

            }
        });*/
        String la = getSharedPreferences(APP_PREF, 0).getString("lat", "0.0");
        String ln = getSharedPreferences(APP_PREF, 0).getString("lng", "0.0");
        LatLng sydney = new LatLng(Double.parseDouble(la), Double.parseDouble(ln));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10.0f));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                lat = latLng.latitude;
                lng = latLng.longitude;
                getCurrentAddress();
                autocompleteFragment.setText(address);
                mMap.addMarker(new MarkerOptions().position(latLng).title(address)).showInfoWindow();
                // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                findViewById(R.id.buttonSelect).setVisibility(View.VISIBLE);
            }
        });


        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

            }
        });

    }

    public void getCurrentAddress() {
        try {

            addresses = geocoder.getFromLocation(lat, lng, 1);

            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

               /* Intent intent = new Intent();
                intent.putExtra("address", address);
                setResult(RESULT_OK, intent);
                finish();*/
            }

        } catch (IOException e) {
        }

    }
}
