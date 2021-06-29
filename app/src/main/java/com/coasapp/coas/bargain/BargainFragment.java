package com.coasapp.coas.bargain;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.shopping.SellerCategoriesActivity;
import com.coasapp.coas.utils.DirectionFinderListener;
import com.coasapp.coas.utils.DirectionsJSONParser;
import com.coasapp.coas.utils.Route;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class BargainFragment extends Fragment implements OnMapReadyCallback, DirectionFinderListener, APPConstants {
    private GoogleMap mMap;
    VehicleCategoryAdapter vehicleCategoryAdapter;
    ArrayList<JSONObject> arrayListVehCat = new ArrayList<>();
    ArrayList<JSONObject> arrayListVehicles = new ArrayList<>();
    ArrayList<Map<String, String>> mapArrayListDriver = new ArrayList<>();
    Marker driverMarker;
    double lat, lng, lat1, lng1, lat2, lng2;
    public static int PERMISSION_ALL = 0;
    SharedPreferences sharedPreferences;
    EditText editTextSource, editTextDest;
    LatLng latLngS, latLngD;
    List<Address> addresses = new ArrayList<>();
    String address = "";
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    ArrayList<String> arrayListUsers = new ArrayList<>();
    String category = "Cars";
    Geocoder geocoder;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    List<Marker> markerList = new ArrayList<>();
    APICallbacks apiCallbacks;
    APIService apiService;

    public BargainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getActivity() fragment
        return inflater.inflate(R.layout.fragment_bargain, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        geocoder = new Geocoder(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        editTextDest = view.findViewById(R.id.editTextDestination);
        editTextSource = view.findViewById(R.id.editTextSource);
        apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {

            }

            @Override
            public void taskEnd(String type, String response) {

            }
        };
        apiService = new APIService(apiCallbacks, getContext());
        Switch switchActive = view.findViewById(R.id.switchStatus);
        try {
            JSONArray arrayUsers = new JSONArray(sharedPreferences.getString("Users", "[]"));
            for (int i = 0; i < arrayUsers.length(); i++) {
                arrayListUsers.add(arrayUsers.getJSONObject(i).getString("coas_id"));
            }
            APPHelper.showLog("Users", String.valueOf(arrayListUsers));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editTextSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  try {

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(getActivity());
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/

                List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);

// Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getContext());
                startActivityForResult(intent, 1);

            }
        });
        view.findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ManageVehicleActivity.class);
                intent.putExtra("mode", "add");
                startActivityForResult(intent, 99);
            }
        });
        editTextDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(getActivity());
                    startActivityForResult(intent, 2);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/

                List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);

// Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getContext());
                startActivityForResult(intent, 2);

            }
        });

        lat = Double.parseDouble(sharedPreferences.getString("lat", "0.0"));
        lng = Double.parseDouble(sharedPreferences.getString("lng", "0.0"));
        lat1 = lat;
        lng1 = lng;

        latLngS = new LatLng(lat1, lng1);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        getCurrentAddress();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);

        vehicleCategoryAdapter = new VehicleCategoryAdapter(arrayListVehCat, getActivity(), getContext());
        recyclerView.setAdapter(vehicleCategoryAdapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        try {
            JSONObject object = new JSONObject();
            object.put("veh_cat", "Cars");
            object.put("image", R.mipmap.hatchback);
            arrayListVehCat.add(object);
            object = new JSONObject();
            object.put("veh_cat", "Van/SUV");
            object.put("image", R.mipmap.van);
            arrayListVehCat.add(object);
            object = new JSONObject();
            object.put("veh_cat", "Pickup Truck");
            object.put("image", R.mipmap.pickup_64);
            arrayListVehCat.add(object);
            object = new JSONObject();
            object.put("veh_cat", "Box Truck");
            object.put("image", R.mipmap.box_truck_64);
            arrayListVehCat.add(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vehicleCategoryAdapter.notifyDataSetChanged();
        vehicleCategoryAdapter.setOnItemClick(new VehicleCategoryAdapter.OnItemClick() {
            @Override
            public void onItemClick(int i) {
                JSONObject object = arrayListVehCat.get(i);
                try {
                    markerList.clear();
                    category = object.getString("veh_cat");
                    arrayListVehicles.clear();
                    mMap.clear();
                    new GetVehicles().execute();

                } catch (JSONException e) {
                    e.printStackTrace();
                }//addMarkers();
            }
        });
    }

    @Override
    public void onDirectionFinderStart() {
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {
        APPHelper.showLog("Dir", "Success" + route.size());
        for (Route routes : route) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.startLocation, 16));
            /*((TextView) findViewById(R.vehicleId.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.vehicleId.tvDistance)).setText(route.distance.text);*/

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue))
                    .title(routes.startAddress)
                    .position(routes.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_green))
                    .title(routes.endAddress)
                    .position(routes.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < routes.points.size(); i++)
                polylineOptions.add(routes.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }


    public void getCurrentAddress() {
        try {
            addresses = geocoder.getFromLocation(lat1, lng1, 1);

            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                APPHelper.showLog("Address", address);
                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                editTextSource.setText(address);
            }

        } catch (IOException e) {
            APPHelper.showLog("Address", e.getMessage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {


            if (requestCode == 1) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                List<Address> addresses = null;
                lat1 = place.getLatLng().latitude;
                lng1 = place.getLatLng().longitude;
                APPHelper.showLog("Source", "" + lat1 + lng1);
                latLngS = new LatLng(lat1, lng1);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngS, 16.0f));
                editTextSource.setText(place.getAddress());
                arrayListVehicles.clear();
                mMap.clear();
                new GetVehicles().execute();
                try {
                    addresses = geocoder.getFromLocation(
                            place.getLatLng().latitude, place.getLatLng().longitude,
                            // In this sample, get just a single address.
                            1);

                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e("Location", ioException.getMessage());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.

                }


                // Handle case where no address was found.
                if (addresses == null || addresses.size() == 0) {

                } else {

                }
            } else if (requestCode == 2) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                List<Address> addresses = null;
                lat2 = place.getLatLng().latitude;
                lng2 = place.getLatLng().longitude;
                editTextDest.setText(place.getAddress());
                latLngD = new LatLng(lat2, lng2);
                arrayListVehicles.clear();
                mMap.clear();
                new GetVehicles().execute();
                try {
                    addresses = geocoder.getFromLocation(
                            place.getLatLng().latitude, place.getLatLng().longitude,
                            // In this sample, get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e("Location", ioException.getMessage());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.

                }

                String uri = "http://maps.google.com/maps?saddr=" + lat1 + "," + lng1 + "&daddr=" + lat2 + "," + lng2;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.SelectAddressActivity");
                //startActivity(intent);
                // Handle case where no address was found.
                String url = getDirectionsUrl(latLngS, latLngD);
                APPHelper.showLog("Route", url);

                DownloadTask downloadTask = new DownloadTask();
                String origin = editTextSource.getText().toString();
                String destination = editTextDest.getText().toString();
                if (origin.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter origin address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (destination.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.addMarker(new MarkerOptions().position(latLngD).title(String.valueOf(place.getAddress())).icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_green)));

               /* try {
                    new DirectionFinder(this, origin, destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
                if (addresses == null || addresses.size() == 0) {

                } else {

                }
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = new RequestHandler().sendGetRequest(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObjectDirections = new JSONObject(result);
                JSONArray jsonArrayRoutes = jsonObjectDirections.getJSONArray("routes");
                if (jsonArrayRoutes.length() > 0) {
                    ParserTask parserTask = new ParserTask();

                    parserTask.execute(result);
                } else {
                    APPHelper.showToast(getContext(), jsonObjectDirections.getString("status"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    /*private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        APPHelper.showLog("Route", data);
        return data;
    }*/

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = new ArrayList<>();

            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
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
        int currentNightMode = APPHelper.getNightModeSetting(getContext());
        APPHelper.setMapStyle(getActivity(), googleMap);
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {
            mMap.setMyLocationEnabled(true);

        }


        // Add a marker in Sydney and move the camera

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngS, 16.0f));
        mMap.addMarker(new MarkerOptions().position(latLngS).title(editTextSource.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue)));
        new GetVehicles().execute();
        /*mMap.addMarker(new MarkerOptions().position(latLngCurrent).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngCurrent));*/
        //addMarkers();
        /*try {
            COASHomeActivity.pubnub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pub, PNStatus status) {

                }

                @Override
                public void message(PubNub pub, final PNMessageResult message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Map<String, String> newLocation = JsonUtil.fromJson(message.getMessage().toString(), LinkedHashMap.class);

                                mapArrayListDriver.add(newLocation);

                                for (int i = 0; i < mapArrayListDriver.size(); i++) {
                                    APPHelper.showLog("Car", newLocation.toString());
                                    updateUI(newLocation);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void presence(PubNub pub, PNPresenceEventResult presence) {

                }
            });
            COASHomeActivity.pubnub.subscribe()
                    .channels(arrayListUsers) // subscribe to channels
                    .execute();
        } catch (Exception e) {

        }*/
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                lat1 = point.latitude;
                lng1 = point.longitude;
                getCurrentAddress();
                latLngS = new LatLng(lat1, lng1);
                mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(latLngS).title(editTextSource.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue)));
                new GetVehicles().execute();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                boolean permissionToWrite = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!permissionToWrite) {
                   /* APPHelper.showToast(getApplicationContext(), "Allow Storage permission");
                    finish();*/

                } else {
                    getCurrentAddress();

                }
                break;
        }

    }

   /* void addMarkers() {

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);

        try {

            JSONObject object = new JSONObject();
            object.put("veh_id", "1");
            object.put("veh_cat", "Hatchback");
            object.put("veh_lat", 8.222);
            object.put("veh_lng", 77.123456);
            LatLng latLng = new LatLng(Double.valueOf(object.getString("veh_lat")), Double.valueOf(object.getString("veh_lng")));
            if (object.getString("veh_cat").equalsIgnoreCase("Hatchback")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);
            } else if (object.getString("veh_cat").equalsIgnoreCase("Sedan")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.sedan);
            } else if (object.getString("veh_cat").equalsIgnoreCase("SUV")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.car);
            } else if (object.getString("veh_cat").equalsIgnoreCase("Van")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.van);
            }
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Car").icon(descriptor));
            arrayListVehicles.add(object);
            object = new JSONObject();
            object.put("veh_id", "1");
            object.put("veh_cat", "Sedan");
            object.put("veh_lat", 9.222);
            object.put("veh_lng", 77.123456);
            arrayListVehicles.add(object);
            if (object.getString("veh_cat").equalsIgnoreCase("Hatchback")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);
                APPHelper.showLog("Car", "H");
            } else if (object.getString("veh_cat").equalsIgnoreCase("Sedan")) {
                APPHelper.showLog("Car", "S");
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.sedan);
            } else if (object.getString("veh_cat").equalsIgnoreCase("SUV")) {
                APPHelper.showLog("Car", "SV");
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.car);
            } else if (object.getString("veh_cat").equalsIgnoreCase("Van")) {
                APPHelper.showLog("Car", "V");
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.van);
            }
            latLng = new LatLng(Double.valueOf(object.getString("veh_lat")), Double.valueOf(object.getString("veh_lng")));
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Car").icon(descriptor));
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    APPHelper.showToast(getApplicationContext(), "" + marker.getId());
                    return true;
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    class GetVehicles extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arrayListVehicles.clear();

        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            map.put("category", category);
            map.put("lat", String.valueOf(lat1));
            map.put("lng", String.valueOf(lng1));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_vehicles.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);

            try {
                JSONArray arrayVehicles = new JSONArray(s);
                for (int i = 0; i < arrayVehicles.length(); i++) {
                    JSONObject objectVehicle = arrayVehicles.getJSONObject(i);
                    LatLng latLng = new LatLng(Double.valueOf(objectVehicle.getString("vehicle_current_lat")), Double.valueOf(objectVehicle.getString("vehicle_current_lng")));
                    if (objectVehicle.getString("vehicle_cat").equalsIgnoreCase("Cars")) {
                        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);
                    } else if (objectVehicle.getString("vehicle_cat").equalsIgnoreCase("Van/SUV")) {
                        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.van);
                    } else if (objectVehicle.getString("vehicle_cat").equalsIgnoreCase("Pickup Truck")) {
                        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pickup_64);
                    } else if (objectVehicle.getString("vehicle_cat").equalsIgnoreCase("Box Truck")) {
                        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.box_truck_64);
                    }
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(objectVehicle.getString("vehicle_current_lat")), Double.parseDouble(objectVehicle.getString("vehicle_current_lng"))))
                            .title(objectVehicle.getString("model_name") + " - " + objectVehicle.getString("name"))
                            //.snippet(checkinStatus + "Guys:" + guys + " and Girls:" + girls)
                            .icon(descriptor);


                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(objectVehicle.toString());
//                Log.e(TAG, "Marker vehicleId '" + marker.getId() + "' added to list.");
                    markerList.add(marker);

                    //Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(objectVehicle.getString("vehicle_name") + " - " + objectVehicle.getString("name")).icon(descriptor));
                    arrayListVehicles.add(objectVehicle);

                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLngS).title(editTextSource.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue));
                Marker marker = mMap.addMarker(markerOptions);
                marker.setTag("Source");
                if (editTextDest.getText().toString().length() > 0) {
                    MarkerOptions markerOptions2 = new MarkerOptions().position(latLngD).title(editTextDest.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue));
                    Marker marker2 = mMap.addMarker(markerOptions);
                    marker2.setTag("Destination");
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.getTag() != null) {
                            if ((marker.getTag().toString().contains("{"))) {
                                //APPHelper.showToast(getApplicationContext(), "" + marker.getTag());
                                Intent intent = new Intent(getContext(), VehicleDetailsActivity.class);
                                intent.putExtra("details", marker.getTag().toString());
                                intent.putExtra("source", editTextSource.getText().toString());
                                intent.putExtra("dest", editTextDest.getText().toString());
                                startActivity(intent);
                            }
                        }


                        return true;
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI(Map<String, String> newLoc) {
        Marker driverMarker = null;
        APPHelper.showLog("Car", newLoc.toString());
        BitmapDescriptor descriptor = null;

        LatLng newLocation = new LatLng(Double.valueOf(newLoc.get("lat")), Double.valueOf(newLoc.get("lng")));
        if (driverMarker != null) {
            animateCar(newLocation, driverMarker);
            boolean contains = mMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds
                    .contains(newLocation);
            if (!contains) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            }
        } else {
            if (newLoc.get("veh_cat").equalsIgnoreCase("Hatchback")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.hatchback);
            } else if (newLoc.get("veh_cat").equalsIgnoreCase("Sedan")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.sedan);
            } else if (newLoc.get("veh_cat").equalsIgnoreCase("SUV")) {

                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.car);
            } else if (newLoc.get("veh_cat").equalsIgnoreCase("Van")) {
                descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.van);
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    newLocation, 15.5f));

            driverMarker = mMap.addMarker(new MarkerOptions().position(newLocation).
                    icon(descriptor));
        }
    }

    /*
        Animates car by moving it by fractions of the full path and finally moving it to its
        destination in a duration of 5 seconds.
     */
    private void animateCar(final LatLng destination, final Marker driverMarker) {
        final LatLng startPosition = driverMarker.getPosition();
        final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
        final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(5000); // duration 5 seconds
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    driverMarker.setPosition(newPosition);
                } catch (Exception ex) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        valueAnimator.start();
    }

    /*
        This interface defines the interpolate method that allows us to get LatLng coordinates for
        a location a fraction of the way between two points. It also utilizes a Linear method, so
        that paths are linear, as they should be in most streets.
     */
    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }
}
