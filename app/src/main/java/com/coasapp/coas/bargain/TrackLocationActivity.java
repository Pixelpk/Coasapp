package com.coasapp.coas.bargain;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.general.FullScreenImageSlide;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.shopping.MyProductImagesAdapter;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.webservices.UploadMultipart;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DirectionsJSONParser;
import com.coasapp.coas.utils.FindDistance;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.Rounding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public class TrackLocationActivity extends MyAppCompatActivity implements APPConstants, OnMapReadyCallback {
    private GoogleMap mMap;
    List<String> listLoc = new ArrayList<>();
    String otherLat, otherLang, source = "Source", dest = "Destination", status;
    double oLat = 0.0, oLng = 0.0, lat = 0.0, lng = 0.0, dLat, dLng, sLat, sLng;
    SharedPreferences sharedPreferences;
    LatLng latLngSource, latLngOther, latLngDest, latLngCurrent;
    Handler handler = new Handler();
    Runnable update;
    Timer swipeTimer;
    String driverId = "0", bargainId = "0";
    int count = 1;
    boolean stop = false;
    String role = "customer";
    TextView textViewDistance;
    Marker marker;
    String imgPath;
    GetLoc getLoc;
    File img1;
    private SupportMapFragment map;
    private LinearLayout layoutImages;
    private CardView cardViewAdd;
    private ImageView textView34;
    private RecyclerView recyclerViewImages;
    private LinearLayout tripStatus;
    private Spinner spinnerTrip;
    private Button buttonUpdate;
    private LinearLayout layoutDriver;
    private LinearLayout layoutLoad;
    private ImageView imageViewCarReg;
    private Button buttonCarReg;
    boolean granted = false;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    ArrayList<String> arrayListImages = new ArrayList<>();
    BargainDriverImagesAdapter imagesAdapter;
    LinearLayout linearLayoutProgress;
    int color = Color.BLUE;
    JSONObject object;

    int size;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-07-12 11:45:30 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        textViewDistance = (TextView) findViewById(R.id.textViewDistance);
        linearLayoutProgress = findViewById(R.id.layoutProgress);
        layoutImages = (LinearLayout) findViewById(R.id.layoutImages);
        cardViewAdd = (CardView) findViewById(R.id.cardViewAdd);
        textView34 = (ImageView) findViewById(R.id.textView34);
        recyclerViewImages = (RecyclerView) findViewById(R.id.recyclerViewImages);
        tripStatus = (LinearLayout) findViewById(R.id.tripStatus);
        spinnerTrip = (Spinner) findViewById(R.id.spinnerTrip);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        layoutDriver = (LinearLayout) findViewById(R.id.layoutDriver);
        layoutLoad = (LinearLayout) findViewById(R.id.layoutLoad);
        imageViewCarReg = (ImageView) findViewById(R.id.imageViewCarReg);
        buttonCarReg = (Button) findViewById(R.id.buttonCarReg);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location);

        findViews();
        try {
            object = new JSONObject(getIntent().getStringExtra("detail"));
            status = object.getString("bargain_status");
            if (status.equalsIgnoreCase("accepted") /*|| status.equalsIgnoreCase("pickup")*/) {
                //layoutImages.setVisibility(View.VISIBLE);
                tripStatus.setVisibility(View.GONE);
                layoutImages.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Are you Ready to Go");
                findViewById(R.id.imageViewStart).setVisibility(View.VISIBLE);

            } else if (status.equalsIgnoreCase("On the way to Pickup")) {
                getSupportActionBar().setTitle(status);
                layoutImages.setVisibility(View.GONE);
                tripStatus.setVisibility(View.GONE);

            } else if (status.equalsIgnoreCase("Pickup")) {
                getSupportActionBar().setTitle("In Transit");
                tripStatus.setVisibility(View.GONE);

            }

            imagesAdapter = new BargainDriverImagesAdapter(productImagesArrayList, getApplicationContext());
            recyclerViewImages.setAdapter(imagesAdapter);
            sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            JSONArray jsonArray = new JSONArray(object.getString("images"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ProductImages productImages = new ProductImages();
                productImages.setId(jsonObject.getString("image_id"));
                productImages.setUrlImage(jsonObject.getString("image"));
                productImages.setColor("#fffafafa");
                productImages.setSource("url");
                productImages.setStatus("1");
                productImagesArrayList.add(productImages);
                arrayListImages.add(MAIN_URL_IMAGE + jsonObject.getString("image"));
            }
            if (jsonArray.length() > 0) layoutImages.setVisibility(View.VISIBLE);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            imagesAdapter.notifyDataSetChanged();
            imagesAdapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
                @Override
                public void onClick(int position) {
                    productImagesArrayList.remove(position);
                    arrayListImages.remove(position);
                    imagesAdapter.notifyDataSetChanged();
                }
            });
            imagesAdapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
                @Override
                public void onClick(int position) {

                    Intent intent = new Intent(getApplicationContext(), FullScreenImageSlide.class);
                    intent.putExtra("position", position);
                    intent.putExtra("images", arrayListImages);
                    startActivity(intent);
                }
            });
            spinnerTrip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (position == 1) {
                        status = "Pickup";

                    } else if (position == 2) {
                        status = "Dropoff";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            findViewById(R.id.imageViewExtNav).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = null;
                    if (status.equalsIgnoreCase("on the way to pickup") || status.equalsIgnoreCase("accepted"))

                        gmmIntentUri = Uri.parse("google.navigation:"/*+sLat+","+sLng*/ + "q=" /*+ lat + "," + lng + ";"*/ + sLat + "," + sLng);
                    else {
                        gmmIntentUri = Uri.parse("google.navigation:"/*+sLat+","+sLng*/ + "q=" /*+ sLat + "," + sLng + ";"*/ + dLat + "," + dLng);
                    }
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    //mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(Intent.createChooser(mapIntent, "Select App"));

                }
            });
            findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (APPHelper.checkPermissionStorage(getApplicationContext())) {
                        showPopUp(v);
                    } else {
                        APPHelper.goToAppPage(TrackLocationActivity.this, "Allow Storage Permission");
                    }
                }
            });
            buttonUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productImagesArrayList.size() > 10 || productImagesArrayList.size() < 1)

                        APPHelper.showToast(getApplicationContext(), "upload 5 to 10 images");
                    else if (spinnerTrip.getSelectedItemPosition() == 0) {
                        APPHelper.showToast(getApplicationContext(), "Select Trip Status");

                    } else {
                        boolean nearby = false;
                        double distance1;

                        double lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                        double lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                        try {
                            if (status.equalsIgnoreCase("pickup")) {
                                distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("source_lat")), Double.valueOf(object.getString("source_lng"))) * 100.0) / 100.0;
                                if (distance1 <= 0.01)
                                    nearby = true;

                            } else if (status.equalsIgnoreCase("dropoff")) {
                                distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("dest_lat")), Double.valueOf(object.getString("dest_lng"))) * 100.0) / 100.0;
                                if (distance1 <= 0.01)
                                    nearby = true;
                            }
                            if (!nearby && status.equalsIgnoreCase("pickup")) {
                                APPHelper.showToast(getApplicationContext(), "Not near pickup location");
                            } else if (!nearby && status.equalsIgnoreCase("dropoff")) {
                                APPHelper.showToast(getApplicationContext(), "Not near dropoff location");
                            } else {
                                JSONArray array = new JSONArray("[]");
                                for (int i = 0; i < productImagesArrayList.size(); i++) {
                                    JSONObject object1 = new JSONObject();
                                    object1.put("image", productImagesArrayList.get(i).getUrlImage());
                                    array.put(object1);
                                }

                                new UpdateTrip().execute(status, array.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }
            });

            findViewById(R.id.imageViewPickup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status = "Pickup";
                    if (productImagesArrayList.size() > 10 || productImagesArrayList.size() < 5)

                        APPHelper.showToast(getApplicationContext(), "upload 5 to 10 images");
                    else {
                        try {
                            double lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                            double lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                            double distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("source_lat")), Double.valueOf(object.getString("source_lng"))) * 100.0) / 100.0;
                            if (distance1 > 0.06) {
                                APPHelper.showToast(getApplicationContext(), "Not near pickup location");
                            } else {

                                JSONArray array = new JSONArray("[]");
                                for (int i = 0; i < productImagesArrayList.size(); i++) {
                                    JSONObject object1 = new JSONObject();
                                    object1.put("image", productImagesArrayList.get(i).getUrlImage());
                                    array.put(object1);
                                }

                                new UpdateTrip().execute(status, array.toString());

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            findViewById(R.id.imageViewStart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status = "On the way to Pickup";

                    try {

                        JSONArray array = new JSONArray("[]");

                        new UpdateTrip().execute(status, array.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
            findViewById(R.id.imageViewDropOff).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status = "Dropoff";
                    if (productImagesArrayList.size() > 10 || productImagesArrayList.size() < 5)

                        APPHelper.showToast(getApplicationContext(), "upload 5 to 10 images");
                    else {
                        try {
                            double lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                            double lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                            double distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("dest_lat")), Double.valueOf(object.getString("dest_lng"))) * 100.0) / 100.0;
                            if (distance1 > 0.06) {
                                APPHelper.showToast(getApplicationContext(), "Not near dropoff location");
                            } else {
                                try {
                                    JSONArray array = new JSONArray("[]");
                                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                                        JSONObject object1 = new JSONObject();
                                        object1.put("image", productImagesArrayList.get(i).getUrlImage());
                                        array.put(object1);
                                    }
                                    new UpdateTrip().execute(status, array.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
            lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
            latLngCurrent = new LatLng(lat, lng);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //APPHelper.showToast(getApplicationContext(), loc + " " + lat + " " + lng);

    }

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(TrackLocationActivity.this, v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());

        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:
                        try {
                            Intent pictureIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            img1 = GetPath.createImageFile(TrackLocationActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(TrackLocationActivity.this, getPackageName() + ".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);
                            if (v.getId() == R.id.cardViewAdd) {
                                code[0] = 0;
                            }

                            startActivityForResult(pictureIntent,
                                    code[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                       /* Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);*/
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }
                        intent.setType("image/*");
                        switch (v.getId()) {
                            case R.id.cardViewAdd:
                                code[0] = 5;
                                break;
                        }
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code[0]);
                        break;

                }

                return true;
            }
        });

        popupMenu.show();
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

                source = object.getString("bargain_source");
                dest = object.getString("bargain_dest");
                bargainId = object.getString("bargain_id");
                sLat = Double.valueOf(object.getString("source_lat"));
                sLng = Double.valueOf(object.getString("source_lng"));
                dLat = Double.valueOf(object.getString("dest_lat"));
                dLng = Double.valueOf(object.getString("dest_lng"));
                latLngSource = new LatLng(sLat, sLng);
                latLngDest = new LatLng(dLat, dLng);

                count = 1;
                role = getIntent().getStringExtra("role");
                driverId = getIntent().getStringExtra("driverId");
                latLngOther = new LatLng(oLat, oLng);
                MarkerOptions markerOptionsS = new MarkerOptions().position(latLngSource).title(source).icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_blue));
                mMap.addMarker(markerOptionsS);
                MarkerOptions markerOptionsD = new MarkerOptions().position(latLngDest).title(dest).icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_green));
                mMap.addMarker(markerOptionsD);
                String url = getDirectionsUrl(latLngSource, latLngDest);
                String url2 = getDirectionsUrl(latLngCurrent, latLngSource);

                APPHelper.showLog("Route", url);

                if (getIntent().getStringExtra("role").equalsIgnoreCase("driver")) {
                    if (status.equalsIgnoreCase("requested") || status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("pickup")) {
                        APPHelper.showToast(getApplicationContext(), getIntent().getStringExtra("role"));

                    }


                }

                if (status.equalsIgnoreCase("on the way to pickup") || status.equalsIgnoreCase("accepted")) {
                    DownloadTask downloadTask2 = new DownloadTask();
                    //downloadTask2.setColor(Color.RED);
                    downloadTask2.execute(url2);
                } else {
                    DownloadTask downloadTask = new DownloadTask();

                    downloadTask.execute(url);
                }
                handler.postDelayed(update, 1000);
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
            if (distance1 <= 0.03) {


                if (status.equalsIgnoreCase("on the way to pickup")) {
                    try {
                        //APPHelper.showToast(getApplicationContext(), "Near Pickup");
                        layoutImages.setVisibility(View.VISIBLE);
                        DownloadTask downloadTask = new DownloadTask();

                        downloadTask.execute(getDirectionsUrl(latLngSource, latLngDest));
                        findViewById(R.id.imageViewPickup).setVisibility(View.VISIBLE);
                        findViewById(R.id.imageViewPickup).setEnabled(true);
                        findViewById(R.id.imageViewPickup).setBackgroundResource(R.drawable.circle_pickup);
                        getSupportActionBar().setTitle("Pickup Arrived");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //handler.removeCallbacks(update);
                    setResult(RESULT_OK);
                }
                /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Near Destination!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int driverId) {
                                //do things
                                setResult(RESULT_OK);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
                /*else {
                    findViewById(R.id.imageViewPickup).setVisibility(View.GONE);
                    findViewById(R.id.imageViewPickup).setEnabled(false);
                    findViewById(R.id.imageViewPickup).setBackgroundResource(R.drawable.circle_pickup_disabled);
                }*/
            }/* else {
                findViewById(R.id.imageViewPickup).setVisibility(View.GONE);
                findViewById(R.id.imageViewPickup).setEnabled(false);
                findViewById(R.id.imageViewPickup).setBackgroundResource(R.drawable.circle_pickup_disabled);
            }*/
            if (distance2 <= 0.03) {


                if (status.equalsIgnoreCase("pickup")) {
                    // APPHelper.showToast(getApplicationContext(), "Near Destination");
                    findViewById(R.id.imageViewDropOff).setVisibility(View.VISIBLE);
                    findViewById(R.id.imageViewDropOff).setEnabled(true);
                    findViewById(R.id.imageViewDropOff).setBackgroundResource(R.drawable.circle_dropoff);
                    //handler.removeCallbacks(update);
                    setResult(RESULT_OK);
                /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Near Destination!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int driverId) {
                                //do things
                                setResult(RESULT_OK);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
                } /*else {
                    findViewById(R.id.imageViewDropOff).setVisibility(View.VISIBLE);
                    findViewById(R.id.imageViewDropOff).setEnabled(false);
                    findViewById(R.id.imageViewDropOff).setBackgroundResource(R.drawable.circle_dropoff_disabled);
                }*/
            }/* else {
                findViewById(R.id.imageViewDropOff).setVisibility(View.VISIBLE);
                findViewById(R.id.imageViewDropOff).setEnabled(false);
                findViewById(R.id.imageViewDropOff).setBackgroundResource(R.drawable.circle_dropoff_disabled);
            }*/
            getSupportActionBar().setSubtitle("" + Rounding.patchDecimal(distance1) + " miles from Source and " + Rounding.patchDecimal(distance2) + " miles from Destination");
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                size = productImagesArrayList.size();
                imgPath = ResizeImage.getResizedImage(img1.getAbsolutePath());
                ProductImages productImages = new ProductImages();
                productImages.setImage(imgPath);
                productImages.setStatus("0");
                productImages.setColor("#ff000000");
                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                arrayListImages.add(imgPath);
                new UploadBill(size).execute();
            } else if (requestCode == 5) {
                size = productImagesArrayList.size();
                if (data.getData() != null) {
                    imgPath = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    ProductImages productImages = new ProductImages();
                    productImages.setImage(imgPath);
                    productImages.setStatus("0");
                    productImages.setColor("#ff000000");
                    arrayListImages.add(imgPath);
                    productImagesArrayList.add(productImages);
                    imagesAdapter.notifyDataSetChanged();
                    new UploadBill(size).execute();
                } else if (data.getClipData() != null) {
                    //int size = productImagesArrayList.size();
                    ClipData mClipData = data.getClipData();

                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        imgPath = GetPath.getPath(getApplicationContext(), uri);
                        ProductImages productImages = new ProductImages();
                        productImages.setImage(imgPath);
                        productImages.setStatus("0");
                        productImages.setColor("#ff000000");
                        productImagesArrayList.add(productImages);
                        arrayListImages.add(imgPath);
                        imagesAdapter.notifyDataSetChanged();
                    }
                    /*for (int i = size; i < productImagesArrayList.size(); i++) {
                        Log.i("Image", productImagesArrayList.get(i).getImage());
                        new UploadBill(i).execute();
                    }*/
                    new UploadBill(size).execute();
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
        String key = "key=" +  getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {
        public void setColor(int color) {
            this.color = color;
        }

        int color = Color.BLUE;

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
                    parserTask.setColor(color);
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

        public void setColor(int color) {
            this.color = color;
        }

        int color = Color.BLUE;

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
                lineOptions.color(color);
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
            String url = MAIN_URL + "get_other_location.php?driverId=" + driverId;
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


    class UploadBill extends AsyncTask<Integer, Integer, String> {

        int index;

        public UploadBill(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_bargain_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", String.valueOf(System.currentTimeMillis()));
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, productImagesArrayList.get(size).getImage(), "driver", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    imgPath = jsonObject.getString("response");
                    productImagesArrayList.get(size).setUrlImage(imgPath);
                    size++;
                    if (size < productImagesArrayList.size()) {
                        new UploadBill(size).execute();
                    }
                }

            } catch (JSONException e) {
                Toast.makeText(TrackLocationActivity.this, "Failed to upload!Check Internet", Toast.LENGTH_SHORT).show();
                productImagesArrayList.remove(size);
                e.printStackTrace();
            }

            imagesAdapter.notifyDataSetChanged();

        }
    }

    class UpdateTrip extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            String url = MAIN_URL + "update_trip_status.php";
            map.put("bargain_id", bargainId);
            map.put("driver_id", driverId);
            map.put("status", strings[0]);
            map.put("image", strings[1]);
            APPHelper.showLog("Url", url);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    APPHelper.showToast(getApplicationContext(), "Trip status updated");
                    //new MyBargainRequests.BuyerOrders().execute();
                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                        productImagesArrayList.get(i).setSource("url");
                    }
                    imagesAdapter.notifyDataSetChanged();
                    if (status.equalsIgnoreCase("on the way to pickup")) {
                        getSupportActionBar().setTitle("On the Way to Pickup");
                        findViewById(R.id.imageViewStart).setVisibility(View.GONE);
                        findViewById(R.id.imageViewPickup).setVisibility(View.VISIBLE);
                        findViewById(R.id.imageViewPickup).setEnabled(false);
                        findViewById(R.id.imageViewPickup).setBackgroundResource(R.drawable.circle_pickup_disabled);
                        String url2 = getDirectionsUrl(latLngCurrent, latLngSource);
                        DownloadTask downloadTask2 = new DownloadTask();
                        APPHelper.showLog("Route", url2);
                        downloadTask2.execute(url2);
                    }
                    if (status.equalsIgnoreCase("Pickup")) {
                        DownloadTask downloadTask = new DownloadTask();

                        downloadTask.execute(getDirectionsUrl(latLngSource, latLngDest));

                        findViewById(R.id.imageViewPickup).setVisibility(View.GONE);
                        findViewById(R.id.imageViewPickup).setEnabled(false);
                        findViewById(R.id.imageViewPickup).setBackgroundResource(R.drawable.circle_pickup_disabled);
                        findViewById(R.id.imageViewDropOff).setVisibility(View.VISIBLE);
                        findViewById(R.id.imageViewDropOff).setEnabled(false);
                        findViewById(R.id.imageViewDropOff).setBackgroundResource(R.drawable.circle_dropoff_disabled);
                        getSupportActionBar().setTitle("In Transit");
                        findViewById(R.id.imageViewPickup).setVisibility(View.GONE);

                    }
                    if (status.equalsIgnoreCase("Dropoff")) {
                        setResult(RESULT_OK);
                        finish();

                    }
                }

                /*JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.add(object);
                }
                mAdapter.notifyDataSetChanged();*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!driverId.equals("0")) {
            handler.removeCallbacks(update);
        }

    }

    @Override
    public void onBackPressed() {
        APPHelper.showLog("Route", driverId);
        if (!driverId.equals("0")) {
            handler.removeCallbacks(update);
        }
        setResult(RESULT_OK);
        finish();
        //super.onBackPressed();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nav) {
            Uri gmmIntentUri = null;
            if (status.equalsIgnoreCase("on the way to pickup") || status.equalsIgnoreCase("accepted"))

                gmmIntentUri = Uri.parse("google.navigation:"*//*+sLat+","+sLng*//* + "q=" *//*+ lat + "," + lng + ";"*//* + sLat + "," + sLng);
            else {
                gmmIntentUri = Uri.parse("google.navigation:"*//*+sLat+","+sLng*//* + "q=" *//*+ sLat + "," + sLng + ";"*//* + dLat + "," + dLng);
            }
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            //mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(Intent.createChooser(mapIntent, "Select App"));

        }
        return super.onOptionsItemSelected(item);
    }*/
}
