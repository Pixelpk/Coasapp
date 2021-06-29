package com.coasapp.coas.general;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.coasapp.coas.BuildConfig;
import com.coasapp.coas.ChatConnectionService;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.GetRequestAsyncTask;
import com.coasapp.coas.utils.RequestHandler;
import com.connectycube.messenger.SendFastReplyMessageService;
import com.connectycube.messenger.fcm.PushListenerService;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.handler.CompletionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * Created by sunil on 21/12/2016.
 */

public class SplashScreenActivity extends AppCompatActivity implements APPConstants {

    LinearLayout layoutProgress;
    String password;
    int forceUpdate = 1;
    AppUpdateManager appUpdateManager;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
  //  PushListenerService pushListenerService;


    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {

        }

        @Override
        public void taskEnd(String type, String response) {

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (type.equalsIgnoreCase("update")) {
                    int currentVersionCode = BuildConfig.VERSION_CODE;
                    String versionName = BuildConfig.VERSION_NAME;

                    int versionCode = Integer.parseInt(jsonObject.getString("version_android"));
                    forceUpdate = Integer.parseInt(jsonObject.getString("force_update_android"));
                    if (versionCode > currentVersionCode) {
                        findViewById(R.id.cardAppUpdate).setVisibility(View.VISIBLE);
                        if (forceUpdate == 1) {
                            ((Button) findViewById(R.id.buttonLater)).setText("Not now");
                        }
                        /*if (checked) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
                            alertDialogBuilder.setMessage("Update Available!");

                            alertDialogBuilder.setPositiveButton("Update Now",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=com.akinfopark.snapay"));
                                            startActivity(intent);
                                        }
                                    });

                            alertDialogBuilder.setNegativeButton("Later",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            boolean loggedIn = sharedPreferences.getBoolean(LOGGEDIN_SHARED_PREF, false);

                                            String email = sharedPreferences.getString(EMAIL_SHARED_PREF, "");
                                            String password = sharedPreferences.getString(PASSWORD_SHARED_PREF, "");
                                            //editTextPhone.setText(email);
                                            //If we will get true
                                            if (loggedIn) {
                                                if (APPNetworkUtil.isInternetOn(SplashActivity.this)) {
                                                    login = new Login();
                                                    login.execute(email, password);
                                                } else {
                                                    Toast.makeText(SplashActivity.this, "Check Internet", Toast.LENGTH_LONG).show();

                                                }


                                            } else {
                                                Intent aIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(aIntent);
                                            }
                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }*/

                    } else {

                        //initLogin();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();

                //showAlert("Snapay under maintenance. Please try after sometime");
                showAlert(response);

            }
        }
    };

    public void showAlert(String errorMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashScreenActivity.this);
        alertDialogBuilder.setMessage(errorMsg);

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);

        sharedPreferences = getSharedPreferences("shared",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String value = sharedPreferences.getString("disclosure","0");

    //    Toast.makeText(this, value, Toast.LENGTH_SHORT).show();

        if(value.equals("0"))
        {
            permission_disclouser();
        }
        else
        {
            startService(new Intent(SplashScreenActivity.this, PushListenerService.class));
            startService(new Intent(SplashScreenActivity.this, SendFastReplyMessageService.class));
            startService(new Intent(SplashScreenActivity.this, ChatConnectionService.class));

            try {
                File dir = new File(Environment.getExternalStorageDirectory() + "/DCIM/COASAPP");

                if (dir.exists()) {
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        if (children != null) {
                            for (String child : children) {
                                new File(dir, child).delete();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



            layoutProgress = findViewById(R.id.layoutProgress);
            findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //findViewById(R.id.cardAppUpdate).setVisibility(View.GONE);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                    startActivity(intent);
                }
            });
            findViewById(R.id.buttonLater).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.cardAppUpdate).setVisibility(View.GONE);
                    if (forceUpdate == 1) {

                        finish();
                    } else {

                        //  initLogin();
                    }
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    initLogin();
                /*layoutProgress.setVisibility(View.VISIBLE);

                appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

// Returns an intent object that you use to check for an update.


// Checks that the platform will allow the specified type of update.
                appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                    layoutProgress.setVisibility(View.GONE);
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        *//*&& appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)*//*) {
                        // Request the update.

                        try {
                            Toast.makeText(SplashScreenActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            appUpdateManager.startUpdateFlowForResult(
                                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                    appUpdateInfo,
                                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                    AppUpdateType.IMMEDIATE,
                                    // The current activity making the update request.
                                    SplashScreenActivity.this,
                                    // Include a request code to later monitor this update request.
                                    99);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            layoutProgress.setVisibility(View.GONE);
                            initLogin();
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(e -> {
                    initLogin();
                    Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("Failure", e.getMessage());
                });*/

                    getRequestAsyncTask = new GetRequestAsyncTask(getApplicationContext(), apiCallbacks);
                    getRequestAsyncTask.setType("update");
                    getRequestAsyncTask.execute(MAIN_URL + "check_app_version.php");

                }
            }, SPLASH_DISPLAY_LENGTH);

        }



    }


    GetRequestAsyncTask getRequestAsyncTask;

    @Override
    protected void onRestart() {
        super.onRestart();
        /*if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                layoutProgress.setVisibility(View.GONE);
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    *//*&& appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)*//*) {
                    // Request the update.
                    try {
                        Toast.makeText(SplashScreenActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                SplashScreenActivity.this,
                                // Include a request code to later monitor this update request.
                                99);
                    } catch (IntentSender.SendIntentException e) {
                        layoutProgress.setVisibility(View.GONE);
                        initLogin();
                        e.printStackTrace();
                        Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    initLogin();
                }
            });
        }*/
    }

    public class LoginTask extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /*private String email, password;

        private Activity activity;

        private LinearLayout layoutProgress;

        public LoginTask(String email, String password, LinearLayout layoutProgress, Activity activity) {
            this.email = email;
            this.password = password;
            this.layoutProgress = layoutProgress;
            this.activity = activity;
        }*/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {

            HashMap<String, String> map = new HashMap<>();
            password = sharedPreferences.getString("password", "");
            map.put("email", sharedPreferences.getString("email", ""));
            map.put("password", sharedPreferences.getString("password", ""));
            map.put("token", sharedPreferences.getString("token", "12345"));
            map.put("device", "android");
            return new RequestHandler().sendPostRequest(MAIN_URL + "login.php", map);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                layoutProgress.setVisibility(View.GONE);

                JSONObject jsonObject = new JSONObject(s);
                JSONObject object = jsonObject.getJSONObject("Response");
                if (object.getString("response_code").equals("1")) {
                    final JSONObject object1 = jsonObject.getJSONObject("UserInfo");
                    final String userId = object1.getString("user_id");
                    final String fName = object1.getString("first_name");
                    final String lName = object1.getString("last_name");
                    final String email = object1.getString("email");
                    final String phone = object1.getString("phone");
                    final String image = object1.getString("image");
                    final String coasId = object1.getString("coas_id");
                    final int unread = object1.getInt("unread");
                    try {
                        editor.putBoolean("loggedIn", true);
                        editor.putString("userId", userId);
                        editor.putString("firstName", fName);
                        editor.putString("lastName", lName);
                        editor.putString("email", email);
                        editor.putString("phone", phone);
                        editor.putString("image", image);
                        editor.putString("coasId", coasId);
                        editor.putString("password", password);
                        editor.putString("country", object1.getString("country"));
                        editor.putString("dob", object1.getString("dob"));
                        editor.putString("currency", object1.getString("currency"));
                        editor.putInt("unread", unread);
                        editor.putString("isDriver", object1.getString("is_driver"));
                        editor.putString("isSeller", object1.getString("is_seller"));
                        editor.putString("isHoster", object1.getString("is_hoster"));
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(SplashScreenActivity.this, COASHomeActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("response"));
                    Intent intent = new Intent(SplashScreenActivity.this, COASLoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error in connection", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SplashScreenActivity.this, COASHomeActivity.class);
                startActivity(intent);
                finish();

            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            if (resultCode == RESULT_OK) {
             //   initLogin();
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        } else {
            finish();
        }
    }

    void initLogin() {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        /*SendBirdCall.registerPushToken(sharedPreferences.getString("token", ""), true, e -> {

        });*/

        if (sharedPreferences.getBoolean("loggedIn", false)) {
            new LoginTask().execute();
        } else {
            Intent mainIntent = new Intent(SplashScreenActivity.this, COASLoginActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }




    void permission_disclouser()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.custom_prominent_disclosure, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);
        deleteDialogView.findViewById(R.id.Custom_prominent_disclouser_Yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //your business logic
               deleteDialog.dismiss();

               editor.putString("disclosure","1").apply();
                startService(new Intent(SplashScreenActivity.this, PushListenerService.class));
                startService(new Intent(SplashScreenActivity.this, SendFastReplyMessageService.class));
                startService(new Intent(SplashScreenActivity.this, ChatConnectionService.class));

                try {
                    File dir = new File(Environment.getExternalStorageDirectory() + "/DCIM/COASAPP");

                    if (dir.exists()) {
                        if (dir.isDirectory()) {
                            String[] children = dir.list();
                            if (children != null) {
                                for (String child : children) {
                                    new File(dir, child).delete();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }



                layoutProgress = findViewById(R.id.layoutProgress);
                findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //findViewById(R.id.cardAppUpdate).setVisibility(View.GONE);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                        startActivity(intent);
                    }
                });
                findViewById(R.id.buttonLater).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findViewById(R.id.cardAppUpdate).setVisibility(View.GONE);
                        if (forceUpdate == 1) {

                            finish();
                        } else {

                            //  initLogin();
                        }
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        initLogin();
                /*layoutProgress.setVisibility(View.VISIBLE);

                appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

// Returns an intent object that you use to check for an update.


// Checks that the platform will allow the specified type of update.
                appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                    layoutProgress.setVisibility(View.GONE);
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        *//*&& appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)*//*) {
                        // Request the update.

                        try {
                            Toast.makeText(SplashScreenActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            appUpdateManager.startUpdateFlowForResult(
                                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                    appUpdateInfo,
                                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                    AppUpdateType.IMMEDIATE,
                                    // The current activity making the update request.
                                    SplashScreenActivity.this,
                                    // Include a request code to later monitor this update request.
                                    99);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            layoutProgress.setVisibility(View.GONE);
                            initLogin();
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(e -> {
                    initLogin();
                    Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("Failure", e.getMessage());
                });*/

                        getRequestAsyncTask = new GetRequestAsyncTask(getApplicationContext(), apiCallbacks);
                        getRequestAsyncTask.setType("update");
                        getRequestAsyncTask.execute(MAIN_URL + "check_app_version.php");

                    }
                }, SPLASH_DISPLAY_LENGTH);

            }
        });

        deleteDialog.show();
    }
}
