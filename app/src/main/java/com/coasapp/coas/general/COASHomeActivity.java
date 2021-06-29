package com.coasapp.coas.general;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.coasapp.coas.BuildConfig;
import com.coasapp.coas.ContactSyncService;
import com.coasapp.coas.R;
import com.coasapp.coas.bargain.BargainFragment;
import com.coasapp.coas.bargain.BargainHistoryActivity;
import com.coasapp.coas.bargain.MyBargainRequests;
import com.coasapp.coas.bargain.MyVehiclesActivity;
import com.coasapp.coas.roombook.BookingHistoryActivity;
import com.coasapp.coas.roombook.MyBookingsActivity;
import com.coasapp.coas.roombook.MyRoomsActivity;
import com.coasapp.coas.roombook.RoomsActivity;
import com.coasapp.coas.roombook.RoomsFragment;
import com.coasapp.coas.shopping.AddressesActivity;
import com.coasapp.coas.shopping.BuyerOrdersActivity;
import com.coasapp.coas.shopping.CartActivity;
import com.coasapp.coas.shopping.MyProductsActivity;
import com.coasapp.coas.shopping.ProductCategoriesFragment;
import com.coasapp.coas.shopping.ProductsActivity;
import com.coasapp.coas.shopping.SellerOrdersActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.AuthenticationUtils;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.LocationHelper;
import com.coasapp.coas.utils.PrefUtils;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.StaticValues;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.ChatConnectionManager;
import com.connectycube.messenger.ChatDialogActivity;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.helpers.RTCSessionManager;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.connectycube.pushnotifications.services.SubscribeService;
import com.connectycube.users.ConnectycubeUsers;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT_ID;

/*
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
*/


public class COASHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, APPConstants {

    List<ViewPagerFragments> fragmentsList = new ArrayList<>();
    NonSwipeableViewPager viewPager;
    //private UserLogoutTask userLogoutTask;
    private FusedLocationProviderClient mFusedLocationClient;
    String lat = "0", lng = "0", place = "";
    private GoogleApiClient googleApiClient;
    BottomNavigationView bottomNavigationView;
    TextView textViewCart;
    ImageButton imageButtonCart;
    int value = 0;
    int menuvalue = 0;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    TextView textViewName, textViewPhone;
    CircleImageView imageViewProfile;
    SharedPreferences sharedPreferences, sharedPreferencesCar;
    NavigationView navigationView;
    LocationRequest locationRequest;
    boolean selected = false;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ContactScan", "Received");
            /*  ((MessagingFragment) FragmentHelper.getFragmentFromActivity(COASHomeActivity.this, R.id.content)).getUsers();*//*addToContacts(
                    intent.getStringArrayListExtra("contacts"))*/
            ;


        }
    };


    public void startContact(ArrayList<String> list) {
        Intent intent = new Intent(getApplicationContext(), ContactSyncService.class);
        intent.putStringArrayListExtra("contacts", list);
        startService(intent);
    }

    void loginAgain() {
        if (BuildConfig.VERSION_CODE == 10) {
            if (PrefUtils.getFirstLogin(getApplicationContext())) {
                logout();
            }
        }
    }

    private void autoAuthenticate() {
        AuthenticationUtils.autoAuthenticate(getApplicationContext(), (userId, user) -> {
            /*if (user != null) {
                if (user.getNickname().equalsIgnoreCase("")) {
                    logout();
                    Toast.makeText(this, "Session Expired", Toast.LENGTH_SHORT).show();
                }
            }*/
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lat", lat);
            editor.putString("lng", lng);

            editor.apply();

            String msg = "Updated Location: " +
                    lat + "," +
                    lng;
            APPHelper.showLog("Location", msg);
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            new UpdateLoc().execute();
            //LinkedHashMap<String, String> message = getNewLocationMessage(location.getLatitude(), location.getLongitude());
                        /*COASHomeActivity.pubnub.publish()
                                .message(message)
                                .channel(sharedPreferences.getString("coasId", ""))
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
    };


    // public static PubNub pubnub; // Pubnub instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coaswelcome);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("contacts"));
        Log.i("ContactScan", "Registered" +
                "");
        APPHelper.showLog("CoasHome", "onCreate");

        Bundle extras = getIntent().getExtras();

        if(extras!=null) {
            String val = extras.getString(Intent.EXTRA_TEXT);

            if (val != null) {
                Toast.makeText(this, val, Toast.LENGTH_LONG).show();
            }
        }
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(this);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Switch switchAct = findViewById(R.id.switchOnline);
        autoAuthenticate();
        if (getIntent().hasExtra("call")) {
            finish();
        }

        viewPager = findViewById(R.id.viewPagerHome);


        textViewCart = findViewById(R.id.textViewCart);
        imageButtonCart = findViewById(R.id.button_Cart);
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.buttonRoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RoomsActivity.class));
            }
        });
        findViewById(R.id.button_Cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                startActivity(intent);*/
                new GetAddress().execute();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                APPHelper.showLog("Code", "Slide" + value);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                APPHelper.showLog("Code", "Open" + value);
                selected = false;

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                APPHelper.showLog("Select", String.valueOf(selected));
               /* if (!selected) {
                    if (bottomNavigationView.getMenu().findItem(R.id.navigation_messsenger).isChecked()) {
                        bottomNavigationView.setSelectedItemId(R.id.navigation_messsenger);
                    }
                } else {
                    if (bottomNavigationView.getMenu().findItem(R.id.navigation_messsenger).isChecked()) {
                        bottomNavigationView.setSelectedItemId(R.id.navigation_messsenger);
                    }
                }*/

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View viewNav = navigationView.getHeaderView(0);
        viewNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawers();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selected = true;
                        value = 6;
                        menuvalue = 6;
                        if (bottomNavigationView.getMenu().findItem(R.id.navigation_messsenger).isChecked()) {
                            menuvalue = 1;
                        }
                        Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }, 220);
            }
        });
        textViewName = viewNav.findViewById(R.id.textViewName);
        textViewPhone = viewNav.findViewById(R.id.textViewPhone);
        imageViewProfile = viewNav.findViewById(R.id.imageViewProfile);
        //disableShiftMode();
        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {

            }

            @Override
            public void taskEnd(String type, String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                    if (type.equalsIgnoreCase("driver")) {
                        if (object.getString("response_code").equalsIgnoreCase("1")) {
                            if (object.getString("is_driver").equalsIgnoreCase("1")) {
                                switchAct.setVisibility(View.VISIBLE);
                                if (object.getString("driver_online").equalsIgnoreCase("1")) {
                                    switchAct.setChecked(true);
                                } else {
                                    switchAct.setChecked(false);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        switchAct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String status = "";
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        status = "1";
                        if (ActivityCompat.checkSelfPermission(COASHomeActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(COASHomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            switchAct.setChecked(false);
                            status = "0";
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(COASHomeActivity.this);
                            alertDialogBuilder.setMessage("Allow Location Permission");

                            alertDialogBuilder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    });


                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        } else if (!LocationHelper.checkGPS(getApplicationContext())) {
                            switchAct.setChecked(false);
                            status = "0";
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(COASHomeActivity.this);
                            alertDialogBuilder.setMessage("Turn on GPS");

                            alertDialogBuilder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(myIntent, 500);
                                        }
                                    });


                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }
                    } else {
                        status = "0";
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put("status", status);
                    map.put("user_id", sharedPreferences.getString("userId", "0"));
                    apiService.callAPI(map, APPConstants.MAIN_URL + "update_driver_status.php", "driver_status");
                }
            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewPager.setCurrentItem(viewPager.getCurrentItem());
                return true;
            }
        });
        viewPager.setPagingEnabled(false);
        viewPager.setOffscreenPageLimit(4);

        Fragment fragment = new MessagingFragment();
        Bundle bundle = new Bundle();
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(EXTRA_CHAT_ID))
                bundle.putString(EXTRA_CHAT_ID, getIntent().getStringExtra(EXTRA_CHAT_ID));
        }
        fragment.setArguments(bundle);
        fragmentsList.add(new ViewPagerFragments("Messenger", fragment));

        fragment = new RoomsFragment();
        fragmentsList.add(new ViewPagerFragments("Rooms for Rent", fragment));

        fragment = new ProductCategoriesFragment();
        bundle = new Bundle();
        bundle.putString("cat_id", "0");
        bundle.putString("sub_cat_id", "0");
        bundle.putString("cat_name", "");
        fragment.setArguments(bundle);
        fragmentsList.add(new ViewPagerFragments("Shopping", fragment));

        fragment = new BargainFragment();
        bundle = new Bundle();
        fragment.setArguments(bundle);
        fragmentsList.add(new ViewPagerFragments("Shopping", fragment));


     /*   ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentsList);
        viewPager.setAdapter(pagerAdapter);*/

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switchAct.setVisibility(View.GONE);
                findViewById(R.id.content).setVisibility(View.GONE);
                getSupportFragmentManager() .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                viewPager.setVisibility(View.VISIBLE);
                APPHelper.showLog("Bnv", "1");

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content);
                switch (item.getItemId()) {
                    case R.id.navigation_messsenger:
                        getSupportActionBar().setTitle("Messenger");

                        menuvalue = 1;
                        value = 1;
                        textViewCart.setVisibility(View.GONE);
                        imageButtonCart.setVisibility(View.GONE);
                        /*getSupportActionBar().setTitle("Messenger");
                        Intent intent = new Intent(getApplicationContext(), ChatDialogActivity.class);
                        startActivity(intent);*/
                        /*Intent intent = new Intent(COASHomeActivity.this, ConversationActivity.class);
                        if (ApplozicClient.getInstance(getApplicationContext()).isContextBasedChat()) {
                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                        }
                        startActivityForResult(intent, 1);*/
                       /* Fragment fragment = new MessagingFragment();
                        Bundle bundle = new Bundle();
                        if (getIntent().getExtras() != null) {
                            if (getIntent().getExtras().containsKey(EXTRA_CHAT_ID))
                                bundle.putString(EXTRA_CHAT_ID, getIntent().getStringExtra(EXTRA_CHAT_ID));
                        }
                        fragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();*/
                        viewPager.setCurrentItem(0);

                        break;
                    case R.id.navigation_renting:
                        getSupportActionBar().setTitle("Rooms for Rent");


                        menuvalue = 2;
                        value = 2;
                        textViewCart.setVisibility(View.GONE);
                        imageButtonCart.setVisibility(View.GONE);
                        getSupportActionBar().setTitle("Rooms for Rent");
                        viewPager.setCurrentItem(1);
                        //getSupportFragmentManager().beginTransaction().replace(R.id.content, new RoomsFragment()).commit();
                        //startActivity(new Intent(getApplicationContext(), RoomsActivity.class));


                        break;

                    case R.id.navigation_shopping:

                        getSupportActionBar().setTitle("Buy & Sell");

                        findViewById(R.id.content).setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        menuvalue = 3;
                        value = 3;
                        textViewCart.setVisibility(View.VISIBLE);
                        imageButtonCart.setVisibility(View.VISIBLE);
                        getSupportActionBar().setTitle("Buy & Sell");
                        if (!(currentFragment instanceof ProductCategoriesFragment)) {
                            Fragment fragment = new ProductCategoriesFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("cat_id", "0");
                            bundle.putString("sub_cat_id", "0");
                            bundle.putString("cat_name", "");
                            fragment.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                        }
                        viewPager.setCurrentItem(2);
                        break;

                    case R.id.navigation_bargain:

                        HashMap<String, String> map = new HashMap<>();
                        map.put("coas_id", sharedPreferences.getString("coasId", ""));

                        apiService.callAPI(map, APPConstants.MAIN_URL + "check_user_active.php", "driver");


                        getSupportActionBar().setTitle("Bargain to Driver");

                        //value = 0;
                        textViewCart.setVisibility(View.GONE);
                        imageButtonCart.setVisibility(View.GONE);
                       /* intent = new Intent(getApplicationContext(), VehicleMapsActivity.class);
                        intent.putExtra("action", menuvalue);
                        startActivityForResult(intent, 1);*/
                       /* fragment = new BargainFragment();
                        bundle = new Bundle();

                        fragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();*/
                        viewPager.setCurrentItem(3);
                        break;
                }
                return true;
            }
        });

        if (sharedPreferences.getString("isDriver", "0").

                equalsIgnoreCase("0")) {
            navigationView.getMenu().findItem(R.id.nav_manage_vehicles).setTitle("BECOME A DRIVER");
            navigationView.getMenu().findItem(R.id.nav_my_bargains).setVisible(false);
        }

        if (sharedPreferences.getString("isSeller", "0").

                equalsIgnoreCase("0")) {
            navigationView.getMenu().findItem(R.id.nav_seller_orders).setVisible(false);
        }
        if (sharedPreferences.getString("isHoster", "0").

                equalsIgnoreCase("0")) {
            navigationView.getMenu().findItem(R.id.nav_manage_bookings).setVisible(false);
        }
       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onItemSelected(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


*/
        sharedPreferencesCar =

                getSharedPreferences(CAR_DETAILS, Context.MODE_PRIVATE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 5 second delay between each request
        locationRequest.setFastestInterval(5000); // 5 seconds fastest time in between each request
        locationRequest.setSmallestDisplacement(0); // 10 meters minimum displacement for new location request
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }
       *//* mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(COASHomeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            lat = String.valueOf(location.getLatitude());
                            lng = String.valueOf(location.getLongitude());
                            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("lat", lat);
                            editor.putString("lng", lng);
                            editor.apply();
                            String msg = "Updated Location: " +
                                    Double.toString(location.getLatitude()) + "," +
                                    Double.toString(location.getLongitude());
                            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            APPHelper.showLog("Location", msg);
                            List<Address> addresses = null;


                        } else {
                            APPHelper.showLog("Location", "Not found");
                        }
                    }
                });
*//*
        else {
            sendUpdatedLocationMessage();

        }*/

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!APPHelper.checkPermissionsGranted(getApplicationContext(), permissions) || !Settings.canDrawOverlays(getApplicationContext())) {
                showPermissionsAlert();
            } else {
                startApp();
            }
        } else {
            if (!APPHelper.checkPermissionsGranted(getApplicationContext(), permissions)) {
                showPermissionsAlert();
            } else {
                sendUpdatedLocationMessage();
            }
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions2)) {
                //showPermissionsAlert();showPermissionsAlert();
                startApp();
                //showPermissionsAlert();
            }
            else if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions))
            {
                startApp();
                //showPermissionsAlert();

            }
            else {
                startApp();
                //showPermissionsAlert();
            }
        } else {
            if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions2)) {
                //showPermissionsAlert();showPermissionsAlert();
                startApp();
                //showPermissionsAlert();
            }
            else if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions))
            {
                startApp();
                //showPermissionsAlert();

            }
            else {
                startApp();
                //showPermissionsAlert();
            }
        }
        loginAgain();
        startApp();
    }


    void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_permissions_explanation, null);

        builder.setView(view);

        TextView textViewContactPermission = view.findViewById(R.id.textViewContactPermission);
        TextView textViewStoragePermission = view.findViewById(R.id.textViewStoragePermission);
        TextView textViewLocationPermission = view.findViewById(R.id.textViewLocationPermission);
        TextView textViewAudioPermission = view.findViewById(R.id.textViewAudioPermission);
        TextView textViewCamPermission = view.findViewById(R.id.textViewCamPermission);
        TextView textViewWindowPermission = view.findViewById(R.id.textViewWindowPermission);

        if (!APPHelper.checkPermissionGranted(getApplicationContext(), READ_CONTACTS)) {
            textViewContactPermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
        }
        if (!APPHelper.checkPermissionGranted(getApplicationContext(), WRITE_EXTERNAL_STORAGE)) {
            textViewStoragePermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
        }
        if (!APPHelper.checkPermissionGranted(getApplicationContext(), ACCESS_FINE_LOCATION) || !APPHelper.checkPermissionGranted(getApplicationContext(), ACCESS_BACKGROUND_LOCATION)) {
            textViewLocationPermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
        }
        if (!APPHelper.checkPermissionGranted(getApplicationContext(), RECORD_AUDIO)) {
            textViewAudioPermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
        }
        if (!APPHelper.checkPermissionGranted(getApplicationContext(), CAMERA)) {
            textViewCamPermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                textViewWindowPermission.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_cancel_black_24dp), null);
            }
        }


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions))
                    startApp();
                else {

                    if (!APPHelper.checkPermissionsGranted2(COASHomeActivity.this, APPConstants.permissions)) {
                        APPHelper.goToAppPage(COASHomeActivity.this, "Please allow permissions");
                    } else
                        ActivityCompat.requestPermissions(COASHomeActivity.this, permissions, 50);
                }
*/

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    /*if (APPHelper.checkPermissionsGranted2(COASHomeActivity.this, APPConstants.permissions2)) {
                        APPHelper.goToAppPage(COASHomeActivity.this, "Please allow permissions");
                    } else {
                        ActivityCompat.requestPermissions(COASHomeActivity.this, permissions2, 50);
                    }*/
                    ActivityCompat.requestPermissions(COASHomeActivity.this, permissions2, 50);


                } else {
                    ActivityCompat.requestPermissions(COASHomeActivity.this, permissions, 50);

                   /* if (APPHelper.checkPermissionsGranted2(COASHomeActivity.this, APPConstants.permissions)) {
                        APPHelper.goToAppPage(COASHomeActivity.this, "Please allow permissions");
                    } else {
                        ActivityCompat.requestPermissions(COASHomeActivity.this, permissions, 50);
                    }*/
                }

            }
        });
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 50) {
            if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions)) {
                startApp();

               /* Intent deviceContactSyncService = new Intent(this, DeviceContactSyncService.class);
                startService(deviceContactSyncService);
                new GetCountries().execute();*/
            } else {
                showPermissionsAlert();
            }

        }
    }

    void startApp() {
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentsList);
        viewPager.setAdapter(pagerAdapter);
        sendUpdatedLocationMessage();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                askDrawPermission();
            } else {
                sendUpdatedLocationMessage();
            }
        } else {
            sendUpdatedLocationMessage();
        }*/
    }

    void askDrawPermission() {
        Intent myIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (APPHelper.isAndroidGoEdition(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Device Not Supported", Toast.LENGTH_SHORT).show();
            } else {
                myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(myIntent, 80);
                Toast.makeText(getApplicationContext(), "Please find COASAPP & allow the permission", Toast.LENGTH_LONG).show();
            }
        } else {
            sendUpdatedLocationMessage();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {

            sendUpdatedLocationMessage2();
            //new GetCountries().execute();
            launchChats();
        }
        if (requestCode == 900) {

        }

        if (requestCode == 80) {
            startApp();
        }
        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                Log.d("Code", String.valueOf(data.getIntExtra("action", 0)));
                value = data.getIntExtra("action", 1);
                switch (value) {
                    case 5:
                        finish();

                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_messsenger);
                        break;
                    case 2:

                        bottomNavigationView.setSelectedItemId(R.id.navigation_renting);
                        //addSecondView();
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_shopping);
                        //addThirdView();
                        break;
                    case 0:
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        //addSecondView();
                        //bottomNavigationView.setSelectedItemId(R.id.navigation_renting);
                        drawer.openDrawer(GravityCompat.START);
                        break;
                    case 4:
                        menuvalue = 1;
                        bottomNavigationView.setSelectedItemId(R.id.navigation_bargain);
                        break;

                }
            }
        }
    }

    DeleteDb deleteDb;

    class DeleteDb extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(COASHomeActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferencesManager.Companion.getInstance(getApplicationContext()).deleteCurrentUser();
            ChatConnectionManager.Companion.getInstance().terminate();
            SubscribeService.unSubscribeFromPushes(getApplicationContext());

            AppDatabase.Companion.getInstance(getApplicationContext()).clearTablesForLogout();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            AuthenticationUtils.deauthenticate(COASHomeActivity.this, isSuccess -> {
                StaticValues.contactSynced = false;
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                sharedPreferences.edit().putBoolean("calling", false).apply();

                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("loggedIn", false);
                editor.apply();
                SharedPreferences sharedPreferencesCar = getSharedPreferences(CAR_DETAILS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferencesCar.edit();
                editor1.clear();
                Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), COASLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });


        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selected = true;
        // Handle navigation view item clicks here.
        value = 6;
        menuvalue = 6;
        if (bottomNavigationView.getMenu().findItem(R.id.navigation_messsenger).isChecked()) {
            menuvalue = 1;
        }
        APPHelper.showLog("Code", "Nav" + value);

        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            RTCSessionManager rtcSessionManager = RTCSessionManager.Companion.getInstance();
            if (rtcSessionManager != null)
                RTCSessionManager.Companion.getInstance().destroy();
            SharedPreferencesManager.Companion.getInstance(getApplicationContext()).deleteCurrentUser();

            logout();




           /* UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

                @Override
                public void onSuccess(Context context) {
                    userLogoutTask = null;
                    findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("loggedIn", false);
                    editor.apply();
                    SharedPreferences sharedPreferencesCar = getSharedPreferences(CAR_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = sharedPreferencesCar.edit();
                    editor1.clear();
                    Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, COASLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception exception) {
                    findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    userLogoutTask = null;

                    AlertDialog alertDialog = new AlertDialog.Builder(COASHomeActivity.this).create();
                    alertDialog.setTitle(getString(R.string.text_alert));
                    alertDialog.setMessage(exception.getMessage());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_alert),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    if (!isFinishing()) {
                        alertDialog.show();
                    }
                }
            };

            userLogoutTask = new UserLogoutTask(userLogoutTaskListener, this);
            userLogoutTask.execute((Void) null);*/

        } else if (id == R.id.nav_manage_bookings) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MyBookingsActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_my_bargains) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MyBargainRequests.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_change_password) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_booking_history) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), BookingHistoryActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_manage_rooms) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MyRoomsActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_payout) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), PayoutHomeActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_order_history) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), BuyerOrdersActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_manage_products) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MyProductsActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_address) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), AddressesActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_support) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), TicketListActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_seller_orders) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), SellerOrdersActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_notifications) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_manage_vehicles) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MyVehiclesActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_my_trips) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), BargainHistoryActivity.class);
                    intent.putExtra("action", menuvalue);
                    startActivityForResult(intent, 1);
                }
            }, 220);
        } else if (id == R.id.nav_home) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_messsenger);
                }
            }, 220);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
    }

  /*  private void initPubnub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(PUBNUB_SUBSCRIBE_KEY);
        pnConfiguration.setPublishKey(PUBNUB_PUBLISH_KEY);
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        APPHelper.showLog("CoasHome", "Start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        APPHelper.showLog("CoasHome", "Resume");
        int unread = sharedPreferences.getInt("unread", 0);
        textViewName.setText(sharedPreferences.getString("firstName", "") + " " + sharedPreferences.getString("lastName", ""));
        textViewPhone.setText(sharedPreferences.getString("std_code", "+1") + sharedPreferences.getString("phone", "000000000"));
        Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + sharedPreferences.getString("image", "")).into(imageViewProfile);
        String sql = "select * from cart";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        //textViewCart.setText("" + cursor.getCount());
        cursor.close();
        new GetCart().execute();
        setMenuCounter(R.id.nav_notifications, unread);

    }

    class GetCart extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", getSharedPreferences(APP_PREF, 0).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "view_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                JSONArray array = object.getJSONArray("products");

                textViewCart.setText("" + array.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }

    public void launchAddProductsActivity(String categoryId, String subCategoryId, String
            catName, String search) {
        getSupportFragmentManager().popBackStackImmediate();
        Intent intent = new Intent(getApplicationContext(), ProductsActivity.class);
        intent.putExtra("cat_id", categoryId);
        intent.putExtra("sub_cat_id", subCategoryId);
        intent.putExtra("cat", catName);
        intent.putExtra("search", search);

        startActivityForResult(intent, 99);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } /*else if (fragment instanceof RoomsFragment) {
            ((RoomsFragment) fragment).resetFilter();
        }*/ else {
            if (viewPager.getCurrentItem() != 2) {
                finish();
            } else {
                super.onBackPressed();
            }
        }

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.coaswelcome, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    class GetAddress extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));

            // return new RequestHandler().sendGetRequest(MAIN_URL + "get_addresses.php?user_id=" + sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_addresses.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                /*if (jsonArray.length() == 0) {
                    startActivity(new Intent(getApplicationContext(), AddressesActivity.class));
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("address", s);
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                    startActivity(intent);
                }*/
                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("address", s);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendUpdatedLocationMessage() {
        /*final SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        Intent deviceContactSyncService = new Intent(this, DeviceContactSyncService.class);
        startService(deviceContactSyncService);*/
        if (!LocationHelper.checkGPS(getApplicationContext())) {


            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please Enable Location to update your location to get the best out of this app");
            dialog.setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, 500);
                    //new GetCountries().execute();

                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    // new GetCountries().execute();
                    launchChats();

                }
            });
            dialog.show();

        } else {
            APPHelper.showLog("Location", "Start");
            launchChats();
            try {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


    }

    private void sendUpdatedLocationMessage2() {


        if (LocationHelper.checkGPS(getApplicationContext())) {
            try {
                mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Location location = locationResult.getLastLocation();
                        lat = String.valueOf(location.getLatitude());
                        lng = String.valueOf(location.getLongitude());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lat", lat);
                        editor.putString("lng", lng);

                        editor.apply();

                        String msg = "Updated Location: " +
                                lat + "," +
                                lng;
                        APPHelper.showLog("Location", msg);
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        new UpdateLoc().execute();
                        //LinkedHashMap<String, String> message = getNewLocationMessage(location.getLatitude(), location.getLongitude());
                        /*COASHomeActivity.pubnub.publish()
                                .message(message)
                                .channel(sharedPreferences.getString("coasId", ""))
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

    }


    class UpdateLoc extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            map.put("lat", lat);
            map.put("lng", lng);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_location.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            APPHelper.showLog("Loc", s);
        }
    }

    class GetCountries extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_countries.php");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("countries", s).apply();
            bottomNavigationView.setVisibility(View.VISIBLE);
            /*if (getIntent().hasExtra("action")) {
                Log.i("AcIntent", String.valueOf(getIntent().getIntExtra("action", 1)));
                if (getIntent().getIntExtra("action", 1) == 2) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_renting);
                }
                if (getIntent().getIntExtra("action", 1) == 3) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_shopping);
                }
                if (getIntent().getIntExtra("action", 1) == 4) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_bargain);
                }
                return;
            }*/

            launchChats();

        }
    }

    void launchChats() {
        if (getIntent().hasExtra("action")) {
            Log.i("AcIntent", String.valueOf(getIntent().getIntExtra("action", 1)));
            if (getIntent().getIntExtra("action", 1) == 2) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_renting);
            }
            if (getIntent().getIntExtra("action", 1) == 3) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_shopping);
            }
            if (getIntent().getIntExtra("action", 1) == 4) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_bargain);
            }
            return;
        }

        bottomNavigationView.setSelectedItemId(R.id.navigation_messsenger);
    }

    void logout() {
        if (mFusedLocationClient != null) {

        }
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        ConnectycubeUsers.signOut().performAsync(new EntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle params) {


                deleteDb = new DeleteDb();
                deleteDb.execute();


            }

            @Override
            public void onError(ResponseException responseException) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                Toast.makeText(COASHomeActivity.this, responseException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}
