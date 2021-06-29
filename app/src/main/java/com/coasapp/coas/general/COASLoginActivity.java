package com.coasapp.coas.general;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.AuthenticationUtils;
import com.coasapp.coas.utils.GetRequestAsyncTask;
import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.utils.OnClearFromRecentServices;
import com.coasapp.coas.utils.PrefUtils;
import com.coasapp.coas.utils.RequestHandler;
import com.connectycube.auth.session.ConnectycubeSessionManager;
import com.connectycube.chat.ConnectycubeChatService;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.ChatConnectionManager;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.sendbird.calls.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.connectycube.messenger.utilities.SharedPreferencesManagerKt.CUBE_USER_ID;

/*import com.applozic.audiovideo.activity.AudioCallActivityV2;
import com.applozic.audiovideo.activity.VideoActivity;*/

public class COASLoginActivity extends AppCompatActivity implements APPConstants, ActivityCompat.OnRequestPermissionsResultCallback {

    String username, password, countryCode = "+1";
    LinearLayout layoutProgress;
    //flag variable for exiting the application
    private boolean exit = false;
    private boolean isDeviceContactSync = true;
    String userId = "";
    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {
            Manifest.permission.READ_CONTACTS};
    String token = "";


    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayAdapter adapterCountries;

    GetRequestAsyncTask getRequestAsyncTask;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            layoutProgress.setVisibility(View.GONE);
            sendbirdauth();

            Log.i("ChatServiceLogin", String.valueOf(ConnectycubeChatService.getInstance().isLoggedIn()));


        }
    };

    void gotoHome() {
        PrefUtils.setFirstLogin(getApplicationContext(), false);
        Intent intent1 = new Intent(getApplicationContext(), COASHomeActivity.class);
        startActivity(intent1);
        finish();
    }

    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        public void taskEnd(String type, String response) {
            arrayListCountries.clear();
            arrayListCountriesSpinner.clear();
            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            try {
                if (type.equalsIgnoreCase("countries")) {
                    JSONArray arrayCountries = new JSONArray(response);
                    for (int i = 0; i < arrayCountries.length(); i++) {
                        JSONObject object = arrayCountries.getJSONObject(i);
                        arrayListCountries.add(object);
                        if (i == 0) {
                            arrayListCountriesSpinner.add(object.getString("country_name"));
                        } else {
                            arrayListCountriesSpinner.add("(" + object.getString("std_code") + ") " + object.getString("country_name"));
                        }
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("countries", response).apply();
                    adapterCountries.notifyDataSetChanged();
                    spinnerCountry.setSelection(231);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error! Please try later", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };

    SharedPreferences sharedPreferences;

    private TextView textViewSignIn;
    private TextView textViewForgot;
    private LinearLayout layoutInput;
    private AppCompatEditText editTextUsername;
    private AppCompatEditText editTextPassword;
    private ImageView buttonLogin;
    private TextView buttonRegister;
    private AppCompatSpinner spinnerCountry;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-02-01 07:22:34 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);
        textViewForgot = (TextView) findViewById(R.id.textViewForgot);
        layoutInput = (LinearLayout) findViewById(R.id.layoutInput);
        editTextUsername = (AppCompatEditText) findViewById(R.id.editTextUsername);
        editTextPassword = (AppCompatEditText) findViewById(R.id.editTextPassword);
        buttonLogin = (ImageView) findViewById(R.id.buttonLogin);
        buttonRegister = (TextView) findViewById(R.id.buttonRegister);
        spinnerCountry = (AppCompatSpinner) findViewById(R.id.spinnerCountry);

        adapterCountries = new ArrayAdapter<>(this, R.layout.spinner_reg, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapterCountries);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coaslogin);

        try {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        findViews();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("ChatLogin"));
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            APPHelper.showLog("Device", task.getException().getMessage());
                            return;
                        }

                        // Get new Instance ID token
                        token = task.getResult().getToken();

                        // Log and toast

                    }
                });
        checkPermission();
        final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left);
        animation.setDuration(1000);
        final Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down1);
        animation1.setDuration(1000);
        TextView textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        final EditText editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        final EditText editTextPass = (EditText) findViewById(R.id.editTextPassword);
        final LinearLayout layoutInput = (LinearLayout) findViewById(R.id.layoutInput);

        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);
        final ImageView buttonLogin = (ImageView) findViewById(R.id.buttonLogin);
        TextView buttonReg = (TextView) findViewById(R.id.buttonRegister);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutInput.setAnimation(animation1);
                layoutInput.setVisibility(View.VISIBLE);

                editTextUsername.setAnimation(animation);
                editTextPass.setAnimation(animation);
                buttonLogin.setAnimation(animation);

            }
        });

        findViewById(R.id.textViewForgot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));

            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = editTextUsername.getText().toString().trim();
                password = editTextPass.getText().toString().trim();
                try {
                    countryCode = arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (username.equals("") || password.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else if (spinnerCountry.getSelectedItemPosition() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Select Country");
                } else {
                    if (!username.contains("@")) {
                        username = countryCode + username;
                    }
                    layoutProgress.setVisibility(View.VISIBLE);
                    LoginTask loginTask = new LoginTask(/*username, password, layoutProgress, LoginActivity.this*/);
                    loginTask.execute();

                   /* Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);*/
                    //new SendOtp().execute();
                }
                /*Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
            }
        });
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("loggedIn", false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), NewRegisterActivity.class));
            }
        });

        getRequestAsyncTask = new GetRequestAsyncTask(getApplicationContext(), apiCallbacks);
        getRequestAsyncTask.setType("countries");
        getRequestAsyncTask.execute(MAIN_URL + "get_countries.php");
    }

    public void checkPermission() {
       /* String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                *//*Manifest.permission.READ_SMS,*//*};*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    permissions2,
                    99);

        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    99);
        }
    }

    public class LoginTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... voids) {

            HashMap<String, String> map = new HashMap<>();
            map.put("email", username);
            map.put("password", password);
            map.put("token", token);
            map.put("device", "android");
            return new RequestHandler().sendPostRequest(MAIN_URL + "login.php", map);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {


                JSONObject jsonObject = new JSONObject(s);
                JSONObject object = jsonObject.getJSONObject("Response");
                if (object.getString("response_code").equals("1")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    final JSONObject object1 = jsonObject.getJSONObject("UserInfo");
                    final String userId = object1.getString("user_id");
                    final String fName = object1.getString("first_name");
                    final String lName = object1.getString("last_name");
                    final String email = object1.getString("email");
                    final String phone = object1.getString("phone");
                    final String image = object1.getString("image");
                    final String coasId = object1.getString("coas_id");
                    final int unread = object1.getInt("unread");


                    editor.putString("userId", userId);
                    editor.putString("firstName", fName);
                    editor.putString("lastName", "");
                    editor.putString("email", email);
                    editor.putString("phone", phone);
                    editor.putString("image", image);
                    editor.putString("coasId", coasId);
                    editor.putString("password", password);
                    editor.putString("country", object1.getString("country"));
                    editor.putString("std_code", object1.getString("std_code"));
                    editor.putString("dob", object1.getString("dob"));
                    editor.putString("currency", object1.getString("currency"));
                    editor.putInt("unread", unread);
                    editor.putString("payment_mode", object1.getString("payment_mode"));
                    editor.putString("isDriver", object1.getString("is_driver"));
                    editor.putString("isSeller", object1.getString("is_seller"));
                    editor.putString("isHoster", object1.getString("is_hoster"));
                    layoutProgress.setVisibility(View.VISIBLE);

                    editor.apply();

                    signInConn();
                  /*  ConnectycubeUsers.signIn(user).performAsync(new EntityCallback<ConnectycubeUser>() {
                        @Override
                        public void onSuccess(ConnectycubeUser user, Bundle args) {
                            //layoutProgress.setVisibility(View.GONE);
                            editor.putBoolean("loggedIn", true);
                            editor.putInt(CUBE_USER_ID, user.getId());
                            editor.apply();
                           *//* if (ApplozicClient.getInstance(COASLoginActivity.this).isContextBasedChat()) {
                                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            }*//*
                            user.setPassword(ChatPass);
                            editor.apply();

                            SharedPreferencesManager.Companion.getInstance(getApplicationContext()).saveCurrentUser(user);
                            ChatConnectionManager.Companion.getInstance().initWith(getApplicationContext());
                            startService(new Intent(getApplicationContext(),OnClearFromRecentServices.class));
                            Log.i("Connectycube", new Gson().toJson(user));
                            Log.i("ChatService", String.valueOf(ConnectycubeChatService.getInstance().isLoggedIn()));
                            Log.i("Connectycube", String.valueOf(ConnectycubeSessionManager.getInstance().getSessionParameters() != null));
                          *//*  Intent intent1 = new Intent(getApplicationContext(), COASHomeActivity.class);

                            startActivity(intent1);
                            finish();*//*
                        }

                        @Override
                        public void onError(ResponseException error) {
                            layoutProgress.setVisibility(View.GONE);
                            String message = error.getMessage();
                            if(message.equalsIgnoreCase("unauthorized")){
                                layoutProgress.setVisibility(View.VISIBLE);
                                final ConnectycubeUser user = new ConnectycubeUser(coasId, ChatPass);
                                user.setLogin(coasId);
                                user.setPassword(ChatPass);
                                user.setEmail(email);
                                user.setFullName(fName);
                                user.setPhone(countryCode+phone);
                                user.setAvatar(MAIN_URL_IMAGE+image);
        *//*StringifyArrayList<String> tags = new StringifyArrayList<String>();
        tags.add("android");
        tags.add("iOS");
        user.setTags(tags);*//*

                                ConnectycubeUsers.signUp(user).performAsync(new EntityCallback<ConnectycubeUser>() {
                                    @Override
                                    public void onSuccess(ConnectycubeUser user, Bundle args) {

                                        Log.i("Connectycube",new Gson().toJson(user));
                                        //layoutProgress.setVisibility(View.GONE);
                                        SharedPreferencesManager.Companion.getInstance(getApplicationContext()).saveCurrentUser(user);
                                        ChatConnectionManager.Companion.getInstance().initWith(getApplicationContext());
                                        startService(new Intent(getApplicationContext(),OnClearFromRecentServices.class));

                                    }

                                    @Override
                                    public void onError(ResponseException error) {
                                        layoutProgress.setVisibility(View.GONE);
                                        Toast.makeText(COASLoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(COASLoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
*/
                } else {

                    Toast.makeText(COASLoginActivity.this, object.getString("response"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void signInConn() {
        final ConnectycubeUser connectycubeUser = new ConnectycubeUser();

        connectycubeUser.setLogin(sharedPreferences.getString("coasId", ""));
        connectycubeUser.setPassword(ChatPass);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ConnectycubeUsers.signIn(connectycubeUser).performAsync(new EntityCallback<ConnectycubeUser>() {
            @Override
            public void onSuccess(ConnectycubeUser user, Bundle args) {
                user.setPassword(connectycubeUser.getPassword());
                //layoutProgress.setVisibility(View.GONE);
                editor.putBoolean("loggedIn", true);
                editor.putInt(CUBE_USER_ID, user.getId());
                editor.apply();
                           /* if (ApplozicClient.getInstance(COASLoginActivity.this).isContextBasedChat()) {
                                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            }*/
                user.setPassword(ChatPass);
                editor.apply();

                SharedPreferencesManager.Companion.getInstance(getApplicationContext()).saveCurrentUser(user);
                ChatConnectionManager.Companion.getInstance().initWith(getApplicationContext());

                startService(new Intent(getApplicationContext(), OnClearFromRecentServices.class));
                Log.i("Connectycube", new Gson().toJson(user));
                Log.i("ChatService", String.valueOf(ConnectycubeChatService.getInstance().isLoggedIn()));
                Log.i("Connectycube", String.valueOf(ConnectycubeSessionManager.getInstance().getSessionParameters() != null));
                          /*  Intent intent1 = new Intent(getApplicationContext(), COASHomeActivity.class);

                            startActivity(intent1);
                            finish();*/


            }

            @Override
            public void onError(ResponseException error) {
                layoutProgress.setVisibility(View.GONE);
                String message = error.getMessage();
                if (message.equalsIgnoreCase("unauthorized")) {
                    layoutProgress.setVisibility(View.VISIBLE);
                    signUpConn();
                } else {
                    Toast.makeText(COASLoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void signUpConn() {
        final ConnectycubeUser user = new ConnectycubeUser();
        user.setLogin(sharedPreferences.getString("coasId", ""));
        user.setPassword(ChatPass);
        user.setEmail(sharedPreferences.getString("email", ""));
        user.setFullName(sharedPreferences.getString("firstName", ""));
        user.setPhone(sharedPreferences.getString("std_code", "") + sharedPreferences.getString("phone", ""));
        user.setAvatar(MAIN_URL_IMAGE + sharedPreferences.getString("image", ""));
        /*StringifyArrayList<String> tags = new StringifyArrayList<String>();
        tags.add("android");
        tags.add("iOS");
        user.setTags(tags);*/

        ConnectycubeUsers.signUp(user).performAsync(new EntityCallback<ConnectycubeUser>() {
            @Override
            public void onSuccess(ConnectycubeUser user, Bundle args) {

                Log.i("Connectycube", new Gson().toJson(user));
                //layoutProgress.setVisibility(View.GONE);
                signInConn();

            }

            @Override
            public void onError(ResponseException error) {
                layoutProgress.setVisibility(View.GONE);
                Toast.makeText(COASLoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void sendbirdauth() {
        layoutProgress.setVisibility(View.VISIBLE);
        AuthenticationUtils.authenticate(getApplicationContext(), sharedPreferences.getString("std_code", "").replace("+", "") +
                sharedPreferences.getString("phone", ""), ApplozicSampleApplication.API_TOKEN, new AuthenticationUtils.AuthenticateHandler() {
            @Override
            public void onResult(boolean isSuccess, User user) {
                Log.i("SendBirdUser", isSuccess + " " + new Gson().toJson(user));
                if (isSuccess) {
                    if (user.getNickname().equalsIgnoreCase("")/* || user.getMetaData() == null*/) {
                        layoutProgress.setVisibility(View.GONE);
                        new SignUpSendBird().execute(user.getUserId());
                    } else {
                        gotoHome();
                    }
                } else {

                }
            }
        });
    }

    /*private void buildContactData(final String userId) {

        Context context = getApplicationContext();
        final AppContactService appContactService = new AppContactService(context);
        // avoid each time update ....
        final List<Contact> contactList = new ArrayList<Contact>();
        class GetUsers extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                return new RequestHandler().sendGetRequest(MAIN_URL + "get_users.php?user_id=" + userId);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                APPHelper.showLog("Users", s);
                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Users", s);
                editor.apply();
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        //Adarsh....
                        Contact contact = new Contact();
                        contact.setUserId(object.getString("coas_id"));
                        contact.setFullName(object.getString("name"));
                        contact.setImageURL(object.getString("image"));
                        contact.setEmailId(object.getString("email"));

                        contactList.add(contact);

                    }
                    appContactService.addAll(contactList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        new GetUsers().execute();


    }

    public void showRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissions();

        } else {
            Intent deviceContactSyncService = new Intent(this, DeviceContactSyncService.class);
            startService(deviceContactSyncService);
            Intent intent = new Intent(getApplicationContext(), COASHomeActivity.class);
                           *//* if (ApplozicClient.getInstance(COASLoginActivity.this).isContextBasedChat()) {
                                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            }*//*
            startActivity(intent);
            finish();
        }
    }

    private void requestContactsPermissions() {

        ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);


    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               /* Intent intent = new Intent(this, DeviceContactSyncService.class);
                startService(intent);*/
                /*Intent intent = new Intent(getApplicationContext(), DeviceContactSyncService.class);
                DeviceContactSyncService.enqueueWork(getApplicationContext(), intent);
                Applozic.getInstance(getApplicationContext()).enableDeviceContactSync(true);
                Intent intent1 = new Intent(getApplicationContext(), COASHomeActivity.class);
                           *//* if (ApplozicClient.getInstance(COASLoginActivity.this).isContextBasedChat()) {
                                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            }*//*
                startActivity(intent1);
                finish();*/


            } else {
                APPHelper.showToast(getApplicationContext(), "Allow Contact Permission to sync users");
            }

        }
    }

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
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

        }
    }

    class SignUpSendBird extends AsyncTask<String, Void, String> {
        String userIdCoas = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            userIdCoas = strings[0];
            try {
                JSONObject map = new JSONObject();
                map.put("user_id", strings[0]);
                map.put("nickname", sharedPreferences.getString("firstName", ""));
                map.put("profile_url", MAIN_URL_IMAGE + sharedPreferences.getString("image", ""));
                JSONObject object = new JSONObject();

                object.put("phone", sharedPreferences.getString("std_code", "") + sharedPreferences.getString("phone", ""));
                object.put("email", sharedPreferences.getString("email", ""));
                map.put("metadata", object);
                return new RequestHandler().sendPutRequest(SENDBIRDURL + "/" + strings[0], map);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.has("user_id")) {
                    gotoHome();
                    //new SignUpMetadata().execute(userIdCoas);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class SignUpMetadata extends AsyncTask<String, Void, String> {
        String sentData = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject map = new JSONObject();
                map.put("user_id", strings[0]);
                //map.put("nickname", sharedPreferences.getString("firstName", ""));
                //map.put("profile_url", sharedPreferences.getString("image", ""));
                JSONObject object = new JSONObject();
                object.put("Phone", sharedPreferences.getString("std_code", "") + sharedPreferences.getString("phone", ""));
                object.put("Email", sharedPreferences.getString("email", ""));
                map.put("metadata", object);
                map.put("upsert", true);
                sentData = object.toString();
                return new RequestHandler().sendPutRequest(SENDBIRDURL + "/" + strings[0] + "/metadata", map);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            if (s.equalsIgnoreCase(sentData)) {
                gotoHome();
            }
        }
    }

}
