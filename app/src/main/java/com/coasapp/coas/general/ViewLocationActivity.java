package com.coasapp.coas.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DirectionsJSONParser;
import com.coasapp.coas.utils.FindDistance;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.Rounding;

import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public class ViewLocationActivity extends AppCompatActivity implements APPConstants, OnMapReadyCallback {
    private GoogleMap mMap;
    List<String> listLoc = new ArrayList<>();
    String otherLat, otherLang, source = "Source", dest = "Destination", status = "";
    double oLat = 0.0, oLng = 0.0, lat = 0.0, lng = 0.0, dLat, dLng, sLat, sLng;
    SharedPreferences sharedPreferences;
    LatLng latLngSource, latLngOther, latLngDest;
    Handler handler = new Handler();
    Runnable update;
    Timer swipeTimer;
    String id = "0";
    int count = 1;
    boolean stop = false;
    String role = "customer";
    TextView textViewDistance;
    Marker marker;
    GetLoc getLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);
        getSupportActionBar().hide();
        textViewDistance = findViewById(R.id.textViewDistance);
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        update = new Runnable() {
            public void run() {
                lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                if (getIntent().getStringExtra("role").equalsIgnoreCase("driver")) {
                    Log.i("LatLng", lat + SPACE + lng);
                    oLat = lat;
                    oLng = lng;
                    prepareMap();
                } else {
                    new GetLoc().execute();
                }

            }
        };

        //APPHelper.showToast(getApplicationContext(), loc + " " + lat + " " + lng);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        APPHelper.setMapStyle(this, googleMap);
        mMap = googleMap;

        APPHelper.showLog("Route", "Map Ready");
        /*latLngOther = new LatLng(oLat, oLng);

        mMap.addMarker(new MarkerOptions().position(latLngSource).title("You").icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue)));
        mMap.addMarker(new MarkerOptions().position(latLngOther).title("Other").icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_green)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngSource));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLngSource, 20f));
        String url = getDirectionsUrl(latLngSource, latLngOther);
        APPHelper.showLog("Route", url);

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);*/
        //prepareMap();
        if (getIntent().hasExtra("location")) {

            String loc = getIntent().getStringExtra("location");
            APPHelper.showToast(getApplicationContext(), loc + " " + lat + " " + lng);
            listLoc = Arrays.asList(loc.split(","));
            otherLat = listLoc.get(0);
            otherLang = listLoc.get(1);
            oLat = Double.valueOf(otherLat);
            oLng = Double.valueOf(otherLang);
            String url = getDirectionsUrl(new LatLng(lat, lng), new LatLng(oLat, oLng));
            APPHelper.showLog("Route", url);

            DownloadTask downloadTask = new DownloadTask();
            //downloadTask.execute(url);
        }
        if (getIntent().hasExtra("driverId")) {

            try {
                JSONObject object = new JSONObject(getIntent().getStringExtra("detail"));
                source = object.getString("bargain_source");
                dest = object.getString("bargain_dest");
                sLat = Double.valueOf(object.getString("source_lat"));
                sLng = Double.valueOf(object.getString("source_lng"));
                dLat = Double.valueOf(object.getString("dest_lat"));
                dLng = Double.valueOf(object.getString("dest_lng"));
                latLngSource = new LatLng(sLat, sLng);
                latLngDest = new LatLng(dLat, dLng);
                status = object.getString("bargain_status");

                count = 1;
                role = getIntent().getStringExtra("role");
                id = getIntent().getStringExtra("driverId");
                latLngOther = new LatLng(oLat, oLng);
                MarkerOptions markerOptionsS = new MarkerOptions().position(latLngSource).title(source).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue));
                mMap.addMarker(markerOptionsS);
                MarkerOptions markerOptionsD = new MarkerOptions().position(latLngDest).title(dest).icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_green));
                mMap.addMarker(markerOptionsD);
                String url = getDirectionsUrl(latLngSource, latLngDest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
                String url2 = getDirectionsUrl(latLngSource, latLngDest);


                if (status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("requested") || status.equalsIgnoreCase("on the way to pickup") || status.equalsIgnoreCase("pickup")) {
                    handler.postDelayed(update, 5000);
                    /*getLoc = new GetLoc();
                    getLoc.execute();*/
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    void prepareMap() {
        try {

           /* latLngOther = new LatLng(oLat, oLng);
            MarkerOptions markerOptionsS = new MarkerOptions().position(latLngSource).title(source).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue));
            mMap.addMarker(markerOptionsS);*/

            latLngOther = new LatLng(oLat, oLng);

            MarkerOptions markerOptionsO = new MarkerOptions().position(latLngOther).title("Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.sportscar));
            if (marker == null) {
                marker = mMap.addMarker(markerOptionsO);
            } else {
                marker.setPosition(latLngOther);
            }
            double distance1 = Math.round(new FindDistance().distance(sLat, sLng, oLat, oLng) * 100.0) / 100.0;
            double distance2 = Math.round(new FindDistance().distance(dLat, dLng, oLat, oLng) * 100.0) / 100.0;
            // APPHelper.showToast(getApplicationContext(), ""+distance2);

            if (distance2 <= 0.03) {

                APPHelper.showToast(getApplicationContext(), "Near Destination");

                //handler.removeCallbacks(update);
                setResult(RESULT_OK);
                /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Near Destination!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                setResult(RESULT_OK);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
            }
            if (status.equalsIgnoreCase("on the way to pickup") && count == 1) {
                String url2 = getDirectionsUrl(latLngOther, latLngSource);
                DownloadTask downloadTask2 = new DownloadTask();
                downloadTask2.execute(url2);
            }
            textViewDistance.setText("" + Rounding.patchDecimal(distance1) + " miles from Source and " + Rounding.patchDecimal(distance2) + " miles from Destination");
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngOther));
           /* if (role.equals("driver")) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        latLngSource, 16f));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        latLngOther, 16f));
            }*/
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    latLngOther, 16f));
            handler.postDelayed(update, 5000);
            //count++;

        } catch (Exception e) {
            APPHelper.showLog("Route", e.getMessage());

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!id.equals("0")) {
            handler.removeCallbacks(update);
        }

    }

    @Override
    public void onBackPressed() {
        APPHelper.showLog("Route", id);
        if (!id.equals("0")) {
            handler.removeCallbacks(update);
        }
        super.onBackPressed();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" +  getString(R.string.google_maps_key);

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
                    APPHelper.showToast(getApplicationContext(), jsonObjectDirections.getString("status"));
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
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

        }
    }

    class GetLoc extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // mMap.clear();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String url = MAIN_URL + "get_other_location.php?id=" + id;
            return new RequestHandler().sendGetRequest(url);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.i("Count", "" + count);
                JSONObject object = new JSONObject(s);
                oLat = Double.valueOf(object.getString("lat"));
                oLng = Double.valueOf(object.getString("lng"));

                APPHelper.showLog("Route", String.valueOf(count));
                prepareMap();
                count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void cancelLoc() {
        if (getLoc != null) {
            getLoc.cancel(true);
        }
    }

}
