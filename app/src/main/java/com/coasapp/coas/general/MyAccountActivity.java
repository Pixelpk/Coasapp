package com.coasapp.coas.general;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.coasapp.coas.BuildConfig;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.GetAge;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.PostRequestAsyncTask;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.webservices.UploadMultipart;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAccountActivity extends MyAppCompatActivity implements APPConstants {

    CircleImageView imageViewProfile;
    String name, lastName, email, phone, password, confirmPassword, userId, countryId, currency, dob, dob2;
    LinearLayout layoutProgress;
    SharedPreferences sharedPreferences;

    String file = "", image = "", coasId = "", countryCode = "+1";
    int age = 1;
    File img1;
    Spinner spinnerCountry, spinnerCurrency;
    ArrayAdapter<String> adapterCountries, adapterCurrency;
    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayList<String> arrayListCurrencySpinner = new ArrayList<>();
    TextView textViewDob, textViewCoasId;

    PostRequestAsyncTask asyncTask;
    EditText editTextName, editTextLastName, editTextEmail, editTextPhone;
    String token;
    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {

        }

        @Override
        public void taskEnd(String type, String response) {
            try {
                JSONObject object = new JSONObject(response);
                if (type.equalsIgnoreCase("login")) {
                    if (object.getString("response_code").equals("1")) {
                        final JSONObject object1 = object.getJSONObject("UserInfo");
                        final String userId = object1.getString("user_id");
                        final String fName = object1.getString("first_name");
                        final String lName = object1.getString("last_name");
                        final String email = object1.getString("email");
                        final String countryCode = object1.getString("std_code");
                        final String phone = object1.getString("phone");
                        final String image = object1.getString("image");
                        final String coasId = object1.getString("coas_id");
                        final int unread = object1.getInt("unread");


                        editTextName.setText(fName);
                        editTextEmail.setText(email);
                        editTextLastName.setText(lName);
                        editTextPhone.setText(phone);
                        textViewCoasId.setText(coasId);
                        Glide.with(getApplicationContext()).load(HOST + image).into(imageViewProfile);
                    } else {
                        Toast.makeText(MyAccountActivity.this, object.getString("response"), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        ((TextView) findViewById(R.id.textViewVersion)).setText("Version " + BuildConfig.VERSION_NAME);
        checkPermissions();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
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
        userId = sharedPreferences.getString("userId", "0");
        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        spinnerCountry.setClickable(false);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        textViewDob = findViewById(R.id.tvDob);
        textViewCoasId = findViewById(R.id.textViewCoasId);
        coasId = sharedPreferences.getString("coasId", "");
        textViewCoasId.setText(sharedPreferences.getString("coasId", ""));
        editTextName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        email = sharedPreferences.getString("email", "");
        editTextName.setText(sharedPreferences.getString("firstName", ""));
        editTextLastName.setText(sharedPreferences.getString("lastName", ""));
        editTextPhone.setText(sharedPreferences.getString("phone", ""));
        editTextEmail.setText(sharedPreferences.getString("email", ""));
        currency = sharedPreferences.getString("currency", "USD");

        dob2 = sharedPreferences.getString("dob", "2000-01-01");

        String[] stringsDob = dob2.split("-");
        int dayOfMonth = Integer.parseInt(stringsDob[2]);
        int month = Integer.parseInt(stringsDob[1]);
        int year = Integer.parseInt(stringsDob[0]);
        age = GetAge.getAge(year, month, dayOfMonth);
        try {
            Date date = sdfDatabaseDate.parse(dob2);
            dob = sdfNativeDate.format(date);
            textViewDob.setText(dob);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        textViewDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        Button buttonSave = findViewById(R.id.buttonSave);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        ImageView imageViewEdit = findViewById(R.id.imageViewEdit);
        APPHelper.showLog("image", sharedPreferences.getString("image", ""));
        adapterCountries = new ArrayAdapter<String>(MyAccountActivity.this, android.R.layout.simple_spinner_item, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(R.layout.spinner_item);
        spinnerCountry.setAdapter(adapterCountries);
        adapterCurrency = new ArrayAdapter<String>(MyAccountActivity.this, android.R.layout.simple_spinner_item, arrayListCurrencySpinner);
        adapterCurrency.setDropDownViewResource(R.layout.spinner_item);
        spinnerCurrency.setAdapter(adapterCurrency);
        try {
            JSONArray arrayCountries = new JSONArray(sharedPreferences.getString("countries", "[]"));
            APPHelper.showLog("Tag", arrayCountries.toString());
            for (int i = 0; i < arrayCountries.length(); i++) {
                JSONObject object = arrayCountries.getJSONObject(i);
                arrayListCountries.add(object);
                arrayListCountriesSpinner.add(object.getString("country_name"));
                //arrayListCurrencySpinner.add(object.getString("currency"));
            }
            arrayListCurrencySpinner.add("USD");
            adapterCurrency.notifyDataSetChanged();
            adapterCountries.notifyDataSetChanged();
            spinnerCountry.setSelection(Integer.parseInt(sharedPreferences.getString("country", "0")));
            spinnerCurrency.setSelection(adapterCurrency.getPosition(sharedPreferences.getString("currency", sharedPreferences.getString("currency", "USD"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (APPHelper.checkPermissionStorage(getApplicationContext())) {
                    showPopUp(v);
                } else {
                    int PERMISSION_ALL = 1;
                    String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


                    ActivityCompat.requestPermissions(MyAccountActivity.this, PERMISSIONS, PERMISSION_ALL);

                }
                /*Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, 1);*/
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = editTextName.getText().toString().trim();
                lastName = editTextLastName.getText().toString().trim();
                email = editTextEmail.getText().toString();
                phone = editTextPhone.getText().toString();
                try {
                    countryCode = arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                countryId = String.valueOf(spinnerCountry.getSelectedItemPosition());
                currency = arrayListCurrencySpinner.get(spinnerCurrency.getSelectedItemPosition());
                APPHelper.showLog("currency", currency);
                if (name.equals("") || phone.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else if (age < 13) {
                    dateAlert();
                } else if (spinnerCountry.getSelectedItemPosition() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Select Country");
                } else if (!InputValidator.isValidEmail(email)) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Email");
                } else if (!InputValidator.isValidMobile(phone)) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Phone");
                } else {
                    layoutProgress.setVisibility(View.VISIBLE);
                    //new Register().execute();
                    new CheckProfile().execute();
                }
            }
        });
        new GetCountries().execute();
        image = sharedPreferences.getString("image", "");
        Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + sharedPreferences.getString("image", "")).into(imageViewProfile);
    }

    private void datePicker() {

        // Get Current Date

        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int year = c.get(Calendar.YEAR - 13);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.YEAR, year);
                        String dob1 = sdfNativeDate.format(c.getTime());
                        dob2 = sdfDatabaseDate.format(c.getTime());
                        int age = GetAge.getAge(year, month, dayOfMonth);
                        if (age < 13) {
                            dateAlert();
                            dob2 = "";
                            dob1 = "";
                        }
                        textViewDob.setText(dob1);

                        //*************Call Time Picker Here ********************
                    }
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        datePickerDialog.show();
    }

    void dateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccountActivity.this);

        builder.setMessage("You must be 13 or older to use messaging function.");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void checkPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!hasPermissions(MyAccountActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                Uri picUri = data.getData();
                file = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), picUri));
                /*Glide.with(this).load(filePath)
                        //.apply(bitmapTransform(new BlurTransformation(10)))
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into((ImageView) getActivity().findViewById(R.id.screen_my_profile_blur_image_IMG));*/
                //imageViewProfile.setImageURI(picUri);
            } else if (requestCode == 0) {
                file = ResizeImage.getResizedImage(img1.getAbsolutePath());

            }

            Glide.with(getApplicationContext()).load(file).into(imageViewProfile);
        }
    }

    private String getPath(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    class Register extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            HashMap<String, String> map = new HashMap<>();
            map.put("first_name", name);
            map.put("last_name", "");
            map.put("currency", currency);
            map.put("country", countryId);
            map.put("country_code", countryCode);
            map.put("dob", dob2);
            map.put("email", email);
            map.put("phone", phone);
            map.put("user_id", userId);
            APPHelper.showLog("account", String.valueOf(map));
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_profile.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    /*String userId = jsonObject.getString("user_id");
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.putString("name", name);
                    editor.putString("phone", phone);
                    editor.putString("userId", userId);
                    editor.apply();
                    if (!file.equals("")) {
                        new UploadImage().execute(userId);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MessengerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }*/
                    APPHelper.showToast(getApplicationContext(), "Profile Updated");

                    if (file.length() > 0) {
                        new UploadImage().execute(userId);
                    } else {


                        alertLogout();

                    }

                    //editor.putString("email", email);
                } else {

                    APPHelper.showToast(getApplicationContext(), jsonObject.getString("response"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            layoutProgress.setVisibility(View.GONE);
        }
    }


    class UploadImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = MAIN_URL + "profile_picture.php";
            //UploadMultipart multipart = new UploadMultipart();
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", params[0]);
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            return multipart.multipartRequest(url, map, file, "ProfilePicture", "image/*");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    image = jsonObject.getString("response");
                    alertLogout();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            layoutProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_account, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_deactivate:
                AlertDialog.Builder builder = new AlertDialog.Builder(MyAccountActivity.this);

                builder.setMessage("Are you sure you want to deactivate your account");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        new Deactivate().execute();

                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                break;

            case R.id.action_invite:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Checkout COASAPP. https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);

                break;

            case R.id.action_refresh:
                callLogin();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void callLogin() {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", sharedPreferences.getString("password", ""));
        map.put("token", token);
        map.put("device", "android");

        asyncTask = new PostRequestAsyncTask(getApplicationContext(), map, "login", apiCallbacks);
        asyncTask.execute(MAIN_URL + "login.php");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    class CheckProfile extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("phone", countryCode + phone);
            map.put("email", email);
            return new RequestHandler().sendPostRequest(MAIN_URL + "check_available_update_profile.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    //APPHelper.showToast(getApplicationContext(), "OK");
                    new Register().execute();
                } else {
                    APPHelper.showToast(getApplicationContext(), "Phone Number already taken");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class Deactivate extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);

            map.put("email", email);
            return new RequestHandler().sendPostRequest(MAIN_URL + "deactivate_account.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    //APPHelper.showToast(getApplicationContext(), "OK");
                    logout();
                } else {
                    APPHelper.showToast(getApplicationContext(), "Failed");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void logout() {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", false);
        editor.apply();
        //Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), COASLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        /*UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

            @Override
            public void onSuccess(Context context) {
                userLogoutTask = null;
                SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("loggedIn", false);
                editor.apply();
                //Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, COASLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception exception) {
                userLogoutTask = null;
                AlertDialog alertDialog = new AlertDialog.Builder(MyAccountActivity.this).create();
                alertDialog.setTitle(getString(R.string.text_alert));
                alertDialog.setMessage(exception.toString());
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

        userLogoutTask = new UserLogoutTask(userLogoutTaskListener, MyAccountActivity.this);
        userLogoutTask.execute((Void) null);*/
    }


    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(MyAccountActivity.this, v);
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
                            img1 = GetPath.createImageFile(MyAccountActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(MyAccountActivity.this, getPackageName() + ".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);


                            startActivityForResult(pictureIntent,
                                    0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                       /* Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);*/
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                       /* switch (v.getId()) {
                            case R.id.buttonCarReg:
                                code[0] = 5;
                                break;
                        }*/
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                        break;

                }

                return true;
            }
        });

        popupMenu.show();
    }


    public void connectApplozic() {
        layoutProgress.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("firstName", name);
        editor.putString("lastName", "");
        editor.putString("image", image);
        editor.putString("country", countryId);
        try {
            editor.putString("std_code", arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString("dob", dob);
        editor.putString("currency", currency);
        editor.apply();
        Intent intent = new Intent();
        intent.putExtra("action", 1);
        setResult(RESULT_OK, intent);
        finish();
        /*Applozic.init(getApplicationContext(), getString(R.string.application_key));
        UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                mAuthTask = null;
                layoutProgress.setVisibility(View.GONE);


                //Basic settings...

                ApplozicClient.getInstance(context).setContextBasedChat(true).setHandleDial(true);

                Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, MyAccountActivity.class.getName());
                MobiComUserPreference.getInstance(context).setUserRoleType(registrationResponse.getRoleType());
                ApplozicClient.getInstance(context).setHandleDial(true).setIPCallEnabled(true);
                activityCallbacks.put(ApplozicSetting.RequestCode.AUDIO_CALL, AudioCallActivityV2.class.getName());
                activityCallbacks.put(ApplozicSetting.RequestCode.VIDEO_CALL, VideoActivity.class.getName());
                ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);
                Intent intent = new Intent();
                intent.putExtra("action", 1);
                setResult(RESULT_OK, intent);
                finish();

                //Set activity callbacks
                    *//*Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.MESSAGE_TAP, MainActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);*//*

                //Start GCM registration....

                PushNotificationTask.TaskListener pushNotificationTaskListener = new PushNotificationTask.TaskListener() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {


                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                    }
                };
                PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(), pushNotificationTaskListener, context);
                pushNotificationTask.execute((Void) null);

                //buildContactData(userId);
                //showRunTimePermission();
                //starting main MainActivity
                            *//*Intent mainActvity = new Intent(context, MainActivity.class);
                            startActivity(mainActvity);*//*

            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                mAuthTask = null;
                layoutProgress.setVisibility(View.GONE);
                AlertDialog alertDialog = new AlertDialog.Builder(MyAccountActivity.this).create();
                alertDialog.setTitle(getString(R.string.text_alert));
                alertDialog.setMessage(exception.toString());
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

        User user = new User();
        user.setUserId(sharedPreferences.getString("coasId", "0"));
        user.setDisplayName(name);
        user.setEmail(email);
        user.setContactNumber("+" + sharedPreferences.getString("std_code", "1") + phone);
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());
        user.setPassword("123456");

        user.setImageLink(MAIN_URL_IMAGE + image);
        Log.i("UserImage", MAIN_URL_IMAGE + image);
        mAuthTask = new UserLoginTask(user, listener, MyAccountActivity.this);
        mAuthTask.execute((Void) null);

        List<String> featureList = new ArrayList<>();
                    *//*featureList.add(User.Features.IP_AUDIO_CALL.getValue());// FOR AUDIO
                    featureList.add(User.Features.IP_VIDEO_CALL.getValue());// FOR VIDEO*//*
        user.setFeatures(featureList); // ADD FEATURES*/

    }

    void alertLogout() {
        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        ConnectycubeUser user = new ConnectycubeUser();
        user.setLogin(coasId);
        user.setEmail(email);
        user.setFullName(name);
        //user.setPassword(ChatPass);
        user.setPhone(countryCode + phone);
        user.setAvatar(MAIN_URL_IMAGE + image);

        ConnectycubeUsers.updateUser(user).performAsync(new EntityCallback<ConnectycubeUser>() {
            @Override
            public void onSuccess(ConnectycubeUser user, Bundle args) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                Log.i("Avatar", new Gson().toJson(user));
                SharedPreferencesManager.Companion.getInstance(getApplicationContext()).saveCurrentUser(user);
                setResult(RESULT_OK);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userId", userId);
                editor.putString("firstName", name);
                editor.putString("lastName", "");
                editor.putString("image", image);
                editor.putString("country", countryId);
                try {
                    editor.putString("std_code", arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                editor.putString("dob", dob);
                editor.putString("currency", currency);
                editor.apply();

                new SignUpSendBird().execute(countryCode.replace("+","")+phone);
            }

            @Override
            public void onError(ResponseException error) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccountActivity.this);

        builder.setMessage("Please sign in again to update your profile");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                /*UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

                    @Override
                    public void onSuccess(Context context) {

                       *//* SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("loggedIn", false);
                        editor.apply();
                        Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, COASLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();*//*
connectApplozic();

                    }

                    @Override
                    public void onFailure(Exception exception) {
                        userLogoutTask = null;
                        AlertDialog alertDialog = new AlertDialog.Builder(MyAccountActivity.this).create();
                        alertDialog.setTitle(getString(R.string.text_alert));
                        alertDialog.setMessage(exception.toString());
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

                userLogoutTask = new UserLogoutTask(userLogoutTaskListener, MyAccountActivity.this);
                userLogoutTask.execute((Void) null);*/
            }
        });

        builder.setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        //alert.show();
/*
        UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

            @Override
            public void onSuccess(Context context) {

                       *//* SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("loggedIn", false);
                        editor.apply();
                        Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, COASLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();*//*
                connectApplozic();

            }

            @Override
            public void onFailure(Exception exception) {
                userLogoutTask = null;
                AlertDialog alertDialog = new AlertDialog.Builder(MyAccountActivity.this).create();
                alertDialog.setTitle(getString(R.string.text_alert));
                alertDialog.setMessage(exception.toString());
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

        userLogoutTask = new UserLogoutTask(userLogoutTaskListener, MyAccountActivity.this);
        userLogoutTask.execute((Void) null);*/
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
            try {
                /*JSONArray arrayCountries = new JSONArray(sharedPreferences.getString("countries", "[]"));
                APPHelper.showLog("Tag", arrayCountries.toString());*/

                JSONArray arrayCountries = new JSONArray(s);
                for (int i = 0; i < arrayCountries.length(); i++) {
                    JSONObject object = arrayCountries.getJSONObject(i);
                    arrayListCountries.add(object);
                    arrayListCountriesSpinner.add(object.getString("country_name"));
                    //arrayListCurrencySpinner.add(object.getString("currency"));
                }
                arrayListCurrencySpinner.add("USD");
                adapterCurrency.notifyDataSetChanged();
                adapterCountries.notifyDataSetChanged();
                spinnerCountry.setSelection(Integer.parseInt(sharedPreferences.getString("country", "0")));
                spinnerCurrency.setSelection(adapterCurrency.getPosition(sharedPreferences.getString("currency", sharedPreferences.getString("currency", "USD"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                map.put("profile_url",MAIN_URL_IMAGE+ sharedPreferences.getString("image", ""));
                JSONObject object = new JSONObject();

                object.put("Phone", sharedPreferences.getString("std_code", "") + sharedPreferences.getString("phone", ""));
                object.put("Email", sharedPreferences.getString("email", ""));
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
                    Toast.makeText(MyAccountActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    //gotoHome();
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

            }
        }
    }

}
