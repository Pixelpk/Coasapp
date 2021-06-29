package com.coasapp.coas.general;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.AppSignatureHelper;
import com.coasapp.coas.utils.GetAge;
import com.coasapp.coas.utils.GetHash;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.MySMSBroadcastReceiver;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.webservices.UploadMultipart;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewRegisterActivity extends AppCompatActivity implements APPConstants {

    LinearLayout layoutPhoneCountry, /*layoutDob, layoutIAgree,*/
            layoutPhoneCorrect, layoutNamePic, /*layoutOTP,*/
            layoutProgress;
    FrameLayout layoutImage;
    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    File img1;
    CircleImageView imageViewProfile;
    ArrayAdapter<String> adapterCountries;
    String name, phone, dob1 = "", dob2 = "", image = "", filepath = "", otp = "", password = "", email = "", countryCode = "+1";
    String countryId = "0";
    AppCompatSpinner spinnerCountry;
    String currency = "USD";
    EditText editTextDob;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    int age = 1;
    String userId = "0", coasId = "COAS0000";
    boolean agree = false, verified = false;

    String entered_otp = "";
    MySMSBroadcastReceiver smsBroadcastReceiver;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_register);
        layoutPhoneCountry = findViewById(R.id.layoutP1);
        layoutProgress = findViewById(R.id.layoutProgress);
        //layoutDob = findViewById(R.id.layoutP2);
        //layoutIAgree = findViewById(R.id.layoutP3);
        layoutPhoneCorrect = findViewById(R.id.layoutP4);
        layoutNamePic = findViewById(R.id.layoutP5);
        //layoutOTP = findViewById(R.id.layoutP6);
        layoutImage = findViewById(R.id.layout_image);

        spinnerCountry = findViewById(R.id.spinnerCountry);
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        ImageView buttonNext1 = findViewById(R.id.buttonNext);

        editTextDob = findViewById(R.id.editTextDob);
        TextView textViewNotNow = findViewById(R.id.textViewNotNow);
        //ImageView buttonNext2 = findViewById(R.id.buttonNext2);

        RadioButton buttonAgree = findViewById(R.id.radioButtonAgree);
        RadioButton buttonDisagree = findViewById(R.id.radioButtonDisagree);

        RadioButton buttonYes = findViewById(R.id.radioButtonYes);
        RadioButton buttonNo = findViewById(R.id.radioButtonNo);

        imageViewProfile = findViewById(R.id.imageViewProfile);
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextOTP = findViewById(R.id.editTextOTP);
        EditText editTextPass = findViewById(R.id.editTextPassword);
        ViewCompat.setBackgroundTintList(spinnerCountry, ColorStateList.valueOf(Color.WHITE));
        adapterCountries = new ArrayAdapter<>(this, R.layout.spinner_reg, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapterCountries);
        new GetCountries().execute();
        /*arrayListCountriesSpinner.add("Select Country");
        arrayListCountriesSpinner.add("USA");
        arrayListCountriesSpinner.add("India");

        adapterCountries.notifyDataSetChanged();*/

        Glide.with(getApplicationContext()).load(R.mipmap.profile_picture).into(imageViewProfile);

        buttonNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPhone.clearFocus();
                APPHelper.hideKeyboard(NewRegisterActivity.this);
                countryId = String.valueOf(spinnerCountry.getSelectedItemPosition());

                try {
                    countryCode = arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code");
                    currency = arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("currency");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                phone = editTextPhone.getText().toString();
                email = editTextEmail.getText().toString();
                if (phone.length() < 8) {
                    APPHelper.showToast(getApplicationContext(), "Enter valid phone");
                } else if (!InputValidator.isValidEmail(email)) {
                    APPHelper.showToast(getApplicationContext(), "Enter valid email");
                } else if (countryId.equals("0")) {
                    APPHelper.showToast(getApplicationContext(), "Choose country");
                } else if (dob2.length() == 0) {
                    dateAlert();
                } else if (!agree) {
                    APPHelper.showToast(getApplicationContext(), "Agree to terms & conditions");
                } else {
                    layoutPhoneCountry.setVisibility(View.GONE);
                    //layoutIAgree.setVisibility(View.GONE);
                    ((TextView) findViewById(R.id.textViewEnteredPhone)).setText(countryCode + phone);
                    layoutPhoneCorrect.setVisibility(View.VISIBLE);

                }

            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                ///String code = parseCode(intent.getStringExtra("msg"));//Parse verification code
                String code = GetHash.getCode(intent.getStringExtra("msg"));
                APPHelper.showLog("SMSReceived", intent.getStringExtra("msg") + " " + code);
                editTextOTP.setText(code);//set code in edit text

            }
        };

        editTextDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

        textViewNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateAlert();
            }
        });

     /*   buttonNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dob2.length() == 0) {
                    dateAlert();
                } else {
                    //layoutDob.setVisibility(View.GONE);
                    //layoutIAgree.setVisibility(View.VISIBLE);
                }

            }
        });*/
        buttonAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree = true;

            }
        });
        buttonDisagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree = false;

            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPhoneCorrect.setVisibility(View.GONE);

                new CheckAvailable().execute();
                ((RadioButton) v).setChecked(false);
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPhoneCorrect.setVisibility(View.GONE);
                layoutPhoneCountry.setVisibility(View.VISIBLE);
                ((RadioButton) v).setChecked(false);
            }
        });
/*

        findViewById(R.id.buttonNext3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editTextName.getText().toString().trim();
                if (name.length() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Enter name");
                } else {




                    //createVerification();
                }


            }

        });
*/

        findViewById(R.id.textViewResend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendSMS().execute();

            }
        });

        findViewById(R.id.buttonNext4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkPermission();
                APPHelper.hideKeyboard(NewRegisterActivity.this);
                name = editTextName.getText().toString();
                entered_otp = editTextOTP.getText().toString();

                password = editTextPass.getText().toString();
                if (name.equalsIgnoreCase("")) {
                    APPHelper.showToast(getApplicationContext(), "Enter name");
                } else if (password.length() < 6) {
                    APPHelper.showToast(getApplicationContext(), "Enter 6 Digit PIN");
                } else {
                    if (otp.equals(entered_otp)) {
                        new Register().execute();
                    } else {
                        APPHelper.showToast(getApplicationContext(), "Incorrect OTP");
                    }
                    //mVerification.verify(otp);
                    //new Register().execute();
                }

            }
        });
        findViewById(R.id.imageViewEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, 1);*/

                showPopUp(v);
            }
        });

        WebView webView = findViewById(R.id.webViewTerms);
        webView.setPadding(8, 8, 8, 8);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal + "termsprivacy.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i("Url", url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    APPHelper.launchChrome(NewRegisterActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
                if (url.equalsIgnoreCase(baseUrlLocal + "privacy.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "privacy-policies/");
                    intent.putExtra("title", "Privacy Policies");
                    //startActivity(intent);
                    APPHelper.launchChrome(NewRegisterActivity.this, APPConstants.baseUrlLocal2 + "privacy-policies/");

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("Url", url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    // APPHelper.launchChrome(NewRegisterActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
                if (url.equalsIgnoreCase(baseUrlLocal + "privacy.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "privacy-policies/");
                    intent.putExtra("title", "Privacy Policies");
                    // startActivity(intent);
                    APPHelper.launchChrome(NewRegisterActivity.this, APPConstants.baseUrlLocal2 + "privacy-policies/");

                }
            }
        });
       /* findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewProfileActivity.class);
                intent.putExtra("url", baseUrlLocal + "regulation.htm");
                intent.putExtra("title", "User Regulations");
                startActivity(intent);
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(NewRegisterActivity.this).registerReceiver((receiver),
                new IntentFilter("OTP")
        );
        APPHelper.showLog("SMR", "Start");
        /*registerReceiver(receiver, new IntentFilter("OTP")
        )*/
        ;
        //smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(NewRegisterActivity.this).unregisterReceiver(receiver);

        //unregisterReceiver(receiver);
        //smsVerifyCatcher.onStop();
    }

    void dateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRegisterActivity.this);

        builder.setMessage("You must be 13 or older to use messaging function.");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                datePicker();
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

    private void datePicker() {

        // Get Current Date

        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.YEAR, year - 13);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.YEAR, year);
                        dob1 = sdf.format(c.getTime());
                        dob2 = sdf2.format(c.getTime());
                        age = GetAge.getAge(year, month, dayOfMonth);
                        if (age < 13) {
                            dateAlert();
                            dob2 = "";
                            dob1 = "";
                        }
                        editTextDob.setText(dob1);

                        //*************Call Time Picker Here ********************
                    }
                }, year, month, day);


        datePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }


   /* void createVerification() {

        try {
            APPHelper.showLog("Phone", countryCode + phone);
            mVerification = SendOtpVerification.createSmsVerification
                    (SendOtpVerification
                            .config(countryCode + phone)
                            .context(this)
                            .message(GetHash.getHash(getApplicationContext()))
                            .senderId("COASAPP")
                            .autoVerification(false)
                            .build(), this);
            mVerification.initiate();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }*/


    /*@Override
    public void onInitiated(String response) {

    }

    @Override
    public void onInitiationFailed(Exception paramException) {

    }

    @Override
    public void onVerified(String response) {
        new Register().execute();
    }

    @Override
    public void onVerificationFailed(Exception paramException) {
        APPHelper.showToast(getApplicationContext(), "Incorrect OTP");
    }*/

    class GetCountries extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_countries.php");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONArray arrayCountries = new JSONArray(s);
                for (int i = 0; i < arrayCountries.length(); i++) {
                    JSONObject object = arrayCountries.getJSONObject(i);
                    arrayListCountries.add(object);
                    if (i == 0) {
                        arrayListCountriesSpinner.add(object.getString("country_name"));
                    } else {
                        arrayListCountriesSpinner.add("(" + object.getString("std_code") + ") " + object.getString("country_name"));
                    }
                }
                adapterCountries.notifyDataSetChanged();
                spinnerCountry.setSelection(231);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {


                Uri mImageUri = data.getData();
                // Get the cursor

                filepath = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                Glide.with(getApplicationContext()).load(filepath).into(imageViewProfile);
                // new UploadBill().execute();
            } else if (requestCode == 0) {
                filepath = ResizeImage.getResizedImage(img1.getAbsolutePath());
                Glide.with(getApplicationContext()).load(filepath).into(imageViewProfile);

            }

        }


    }


    class UploadBill extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            Log.d("image", "");
            String res = "";
            String url = MAIN_URL + "profile_picture.php";
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", "" + userId);
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, filepath, "ProfilePicture", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {

                    image = jsonObject.getString("response");


                    connectyCubeReg();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class CheckAvailable extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(Void... params) {

            HashMap<String, String> map = new HashMap<>();

            map.put("email", email);
            map.put("phone", countryCode + phone);
            return new RequestHandler().sendPostRequest(MAIN_URL + "check_available_reg.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {


                    SmsRetrieverClient client = SmsRetriever.getClient(NewRegisterActivity.this /* context */);

// Starts SmsRetriever, which waits for ONE matching SMS message until timeout
// (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
// action SmsRetriever#SMS_RETRIEVED_ACTION.
                    Task<Void> task = client.startSmsRetriever();

// Listen for success/failure of the start Task. If in a background thread, this
// can be made blocking using Tasks.await(task, [timeout]);
                    task.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully started retriever, expect broadcast intent
                            // ...
                            layoutProgress.setVisibility(View.GONE);

                        }
                    });

                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to start retriever, inspect Exception for more details
                            // ...
                            layoutProgress.setVisibility(View.GONE);
                        }
                    });


                    new SendSMS().execute();
                } else {
                    layoutPhoneCountry.setVisibility(View.VISIBLE);
                    APPHelper.showToast(getApplicationContext(), jsonObject.getString("response"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            layoutProgress.setVisibility(View.GONE);
        }
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
            map.put("email", email);
            map.put("country_code", countryCode);
            map.put("phone", phone);
            map.put("password", password);
            map.put("currency", currency);
            map.put("country", countryId);
            map.put("dob", dob2);

            return new RequestHandler().sendPostRequest(MAIN_URL + "register.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    userId = jsonObject.getString("user_id");
                    coasId = jsonObject.getString("coas_id");
                    if (!filepath.equals("")) {
                        new UploadBill().execute();
                    } else {
                        connectyCubeReg();
                    }

                } else {

                    APPHelper.showToast(getApplicationContext(), jsonObject.getString("response"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            layoutProgress.setVisibility(View.GONE);
        }
    }

    public void checkPermission() {
        /*String[] permissions = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };*/

        if (!APPHelper.checkPermissionsGranted(getApplicationContext(), permissions))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        99);
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions2,
                        99);
            }
        else {
            showBingoAlert();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 99:

               /* boolean contactOk = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean locationOk = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                //boolean phoneOk = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                boolean storageOk = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean camOK = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                boolean audioOk = grantResults[3] == PackageManager.PERMISSION_GRANTED;*/


                if (APPHelper.checkPermissionsGranted(getApplicationContext(), permissions)) {
                    showBingoAlert();
                } else {
                    showPermissionAlert();
                }

                break;
            case 80:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                       /* switch (v.getId()) {
                            case R.id.buttonCarReg:
                                code[0] = 5;
                                break;
                        }*/
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                break;
            case 81:
                Intent pictureIntent = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    img1 = GetPath.createImageFile(NewRegisterActivity.this);

                    Uri photoURI = FileProvider.getUriForFile(NewRegisterActivity.this, "com.mobicomkit.coas.provider", img1);
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            photoURI);


                    startActivityForResult(pictureIntent,
                            0);
                } catch (IOException e) {


                }
                break;
        }
    }

    void showPermissionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRegisterActivity.this);

        builder.setMessage("Please allow required permissions for better user experience");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                showBingoAlert();
                //onBackPressed();
                // Do nothing but close the dialog

            }
        });


        AlertDialog alert = builder.create();
        alert.show();
    }

    void showBingoAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRegisterActivity.this);

        builder.setMessage("Welcome! Bring your Friend/Family to COASAPPers like Yourself. Please check your email to verify your coasapp account before signing in.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), COASLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });


        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();

    }

    void connectyCubeReg() {
        layoutProgress.setVisibility(View.VISIBLE);
        final ConnectycubeUser user = new ConnectycubeUser(coasId, ChatPass);
        user.setLogin(coasId);
        user.setPassword(ChatPass);
        user.setEmail(email);
        user.setFullName(name);
        user.setPhone(countryCode + phone);
        user.setAvatar(MAIN_URL_IMAGE + image);
        /*StringifyArrayList<String> tags = new StringifyArrayList<String>();
        tags.add("android");
        tags.add("iOS");
        user.setTags(tags);*/

        ConnectycubeUsers.signUp(user).performAsync(new EntityCallback<ConnectycubeUser>() {
            @Override
            public void onSuccess(ConnectycubeUser user, Bundle args) {

                Log.i("Connectycube", new Gson().toJson(user));
                layoutProgress.setVisibility(View.GONE);
               /* Intent intent = new Intent(getApplicationContext(), COASLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/


                //checkPermission();

                new SignUpSendBird().execute();

            }

            @Override
            public void onError(ResponseException error) {
                layoutProgress.setVisibility(View.GONE);
                Toast.makeText(NewRegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class SignUpSendBird extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                JSONObject map = new JSONObject();
                map.put("user_id", countryCode.replace("+", "") + phone);
                map.put("nickname", name);
                map.put("profile_url", MAIN_URL_IMAGE + image);
                JSONObject object = new JSONObject();
                object.put("Phone", phone);
                object.put("email", email);
                map.put("metadata", object);
                Log.i("SendBirdUser", map.toString());
                return new RequestHandler().sendPostJsonRequest(SENDBIRDURL, map);
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
                    checkPermission();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {

       /* if (layoutOTP.getVisibility() == View.VISIBLE) {
            layoutOTP.setVisibility(View.GONE);
            layoutNamePic.setVisibility(View.VISIBLE);
        } else*/
        if (layoutNamePic.getVisibility() == View.VISIBLE) {
            layoutNamePic.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layoutPhoneCountry.setVisibility(View.VISIBLE);
        } else if (layoutPhoneCorrect.getVisibility() == View.VISIBLE) {
            layoutPhoneCorrect.setVisibility(View.GONE);
            layoutPhoneCountry.setVisibility(View.VISIBLE);
        } /*else if (layoutIAgree.getVisibility() == View.VISIBLE) {
            layoutIAgree.setVisibility(View.GONE);
            layoutDob.setVisibility(View.VISIBLE);
        } else if (layoutDob.getVisibility() == View.VISIBLE) {
            layoutDob.setVisibility(View.GONE);
            layoutPhoneCountry.setVisibility(View.VISIBLE);
        }*/ else {
            super.onBackPressed();
        }
    }

    class SendSMS extends AsyncTask<Void, Void, String> {
        String hash = new AppSignatureHelper(getApplicationContext()).getAppSignatures().get(0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("phone", phone);
            map.put("country_code", countryCode);
            map.put("app_sign", hash);
            return new RequestHandler().sendPostRequest(MAIN_URL + "sendsms.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                otp = object.getString("otp");
                JSONObject objectNexmo = object.getJSONObject("nexmo");
                JSONArray arrayMsg = objectNexmo.getJSONArray("messages");
                if (arrayMsg.length() > 0) {
                    JSONObject object1 = arrayMsg.getJSONObject(0);
                    if (object1.get("status").equals("0")) {
                        layoutPhoneCorrect.setVisibility(View.GONE);
                        layoutImage.setVisibility(View.VISIBLE);
                        layoutNamePic.setVisibility(View.VISIBLE);
                       /* layoutImage.setVisibility(View.GONE);
                        layoutNamePic.setVisibility(View.GONE);
                        layoutOTP.setVisibility(View.VISIBLE);*/
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(NewRegisterActivity.this, v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());

        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:
                        try {
                            if (APPHelper.checkPermissionStorage(getApplicationContext())) {
                                Intent pictureIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                img1 = GetPath.createImageFile(NewRegisterActivity.this);
                                Uri photoURI = FileProvider.getUriForFile(NewRegisterActivity.this, "com.coasapp.coas.provider", img1);
                                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        photoURI);


                                startActivityForResult(pictureIntent,
                                        0);
                            } else {
                                ActivityCompat.requestPermissions(NewRegisterActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        81);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                       /* Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);*/
                        if (APPHelper.checkPermissionStorage(getApplicationContext())) {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                       /* switch (v.getId()) {
                            case R.id.buttonCarReg:
                                code[0] = 5;
                                break;
                        }*/
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                        } else {
                            ActivityCompat.requestPermissions(NewRegisterActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    80);
                        }
                        break;

                }

                return true;
            }
        });

        popupMenu.show();
    }
}
