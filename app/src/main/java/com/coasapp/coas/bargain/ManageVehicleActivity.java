package com.coasapp.coas.bargain;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.webservices.GetCommission;
import com.coasapp.coas.webservices.UploadMultipart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ManageVehicleActivity extends AppCompatActivity implements APPConstants {

    LinearLayout scrollView1, scrollView2, scrollView3;
    Spinner spinnerCat, spinnerBrand, spinnerModel, spinnerYear;
    String filepath = "", image = "";
    LinearLayout layoutProgress;
    String brandId = "0", vehicleId1 = "0", category = "", userId = "", vehicleId = "0";
    ArrayList<JSONObject> arrayListBrands = new ArrayList<>();
    ArrayList<String> arrayListBrandsSpinner = new ArrayList<>();
    ArrayList<JSONObject> arrayListColors = new ArrayList<>();
    ArrayList<String> arrayListColorsSpinner = new ArrayList<>();
    ArrayAdapter<String> adapterBrands, adapterColors, adapterYears, adapterCat;
    String name, email, phone;
    String dwi = "", license = "", carry = "", felony = "", insurance = "", socialSecurity = "", year;
    ImageView imageViewNumber, imageViewCar, imageViewIns, imageViewInspection, imageViewLicense;
    String imgPathCarReg = "", imgPathIns = "", imgPathCar = "", imgPathLicense = "", imgPathInspection = "", vehicleNum = "";
    File img1, img2, img3, img4, img5;
    boolean granted = false;
    String active = "0";
    int color;
    Switch aSwitchActive;
    String vehicleColor = "", vehicleColorName = "", vehicleName = "", brandName = "";
    EditText editTextModelName, editTextName, editTextMName, editTextLName;
    ArrayList<String> arrayListImages = new ArrayList<>();
    ArrayList<String> arrayListYear = new ArrayList<>();
    String fName, mName, lName;
    APIService apiService;
    Menu menuBargain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_vehicle);
        aSwitchActive = findViewById(R.id.switchOnline);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        for (int i = 0; i < 5; i++) {
            arrayListImages.add("");
        }
        Calendar calendar = Calendar.getInstance();

        for (int i = calendar.get(Calendar.YEAR); i >= 1960; i--) {
            arrayListYear.add(String.valueOf(i));
        }
        if (getIntent().hasExtra("vehicle_id")) {
            vehicleId = getIntent().getStringExtra("vehicle_id");

        }
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "0");
        name = sharedPreferences.getString("firstName", "Guest");
        email = sharedPreferences.getString("email", "");
        phone = sharedPreferences.getString("phone", "");
        editTextName = findViewById(R.id.editTextName);
        editTextMName = findViewById(R.id.editTextMName);
        editTextLName = findViewById(R.id.editTextLName);


        layoutProgress = findViewById(R.id.layoutProgress);
        scrollView1 = findViewById(R.id.scrollViewVehicleDetail);
        scrollView2 = findViewById(R.id.scrollViewDriverDetail);
        scrollView3 = findViewById(R.id.scrollViewDriverKyc);
        editTextModelName = findViewById(R.id.editTextModelName);
        editTextName = findViewById(R.id.editTextName);
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextNum = findViewById(R.id.editTextVehNum);

        spinnerCat = findViewById(R.id.spinnerCategory);

        spinnerBrand = findViewById(R.id.spinnerBrand);
        spinnerModel = findViewById(R.id.spinnerModel);
        spinnerYear = findViewById(R.id.spinnerYear);
        TextView textViewColor = findViewById(R.id.textViewColor);

        //color = ((ColorDrawable) textViewColor.getBackground()).getColor();
        vehicleColor = String.format("#%06X", (0xFFFFFF & color));
        imageViewCar = findViewById(R.id.imageViewCar);
        imageViewIns = findViewById(R.id.imageViewInsurance);
        imageViewLicense = findViewById(R.id.imageViewLicense);
        imageViewNumber = findViewById(R.id.imageViewCarReg);
        imageViewInspection = findViewById(R.id.imageViewInspection);
        CheckBox checkBoxAgree = findViewById(R.id.checkBoxAgree);
        if (!vehicleId.equals("0")) {
            checkBoxAgree.setChecked(true);
        }
        EditText editTextSocial = findViewById(R.id.editTextSocial);
        adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.VehType));
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapterCat);
        adapterBrands = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayListBrandsSpinner);
        adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrand.setAdapter(adapterBrands);

        adapterColors = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayListColorsSpinner);
        adapterColors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapterColors);

        adapterYears = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayListYear);
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYears);
        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                layoutProgress.setVisibility(View.VISIBLE);

            }

            @Override
            public void taskEnd(String type, String response) {
                layoutProgress.setVisibility(View.GONE);
                try {
                    JSONObject object = new JSONObject(response);
                    if (type.equalsIgnoreCase("delete")) {
                        if (object.getString("response_code").equalsIgnoreCase("1")) {
                            sharedPreferences.edit().putString("isDriver", "0").apply();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        apiService = new APIService(apiCallbacks, getApplicationContext());
        new GetBrands().execute();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 3000);*/
        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextPhone.setText(phone);
        findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal + "regulation.htm");
                intent.putExtra("title", "User Regulations");
                startActivity(intent);
            }
        });
        APPHelper.setTerms(this,findViewById(R.id.layoutAgree));
        WebView webView = findViewById(R.id.webViewTerms);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal + "termsconditions.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    APPHelper.launchChrome(ManageVehicleActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    APPHelper.launchChrome(ManageVehicleActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
            }
        });
        textViewColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AmbilWarnaDialog dialog = new AmbilWarnaDialog(ManageVehicleActivity.this, color, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        //textViewColor.setBackgroundColor(color);
                        vehicleColor = String.valueOf(color);
                        vehicleColor = String.format("#%06X", (0xFFFFFF & color));

                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                    }
                });
                dialog.show();
            }
        });
        aSwitchActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(ManageVehicleActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showAlert("Allow Location Permission");
                        aSwitchActive.setChecked(false);
                    } else if (!gps_enabled) {
                        aSwitchActive.setChecked(false);
                        showAlert2();
                    } else {
                        active = "1";
                        new UpdateActive().execute();
                    }

                } else {
                    active = "0";
                    new UpdateActive().execute();
                }

            }
        });
        findViewById(R.id.buttonNext1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName = editTextMName.getText().toString().trim();
                name = editTextName.getText().toString().trim();
                lName = editTextLName.getText().toString().trim();
                vehicleNum = editTextNum.getText().toString();

                category = spinnerCat.getSelectedItem().toString();
                try {
                    vehicleColor = arrayListColors.get(spinnerModel.getSelectedItemPosition()).getString("color_code");
                    year = arrayListYear.get(spinnerYear.getSelectedItemPosition());
                    brandId = arrayListBrands.get(spinnerBrand.getSelectedItemPosition()).getString("brand_id");
//                    vehicleId1 = arrayListColors.get(spinnerModel.getSelectedItemPosition()).getString("model_id");
                    vehicleName = editTextModelName.getText().toString().trim();
                    if (name.equalsIgnoreCase("") || lName.equalsIgnoreCase("")) {
                        APPHelper.showToast(getApplicationContext(), "Enter 1st & last name");
                    } else if (category.contains("select") || brandId.equals("0") || vehicleName.equals("") || vehicleNum.equals("")) {
                        APPHelper.showToast(getApplicationContext(), "Fill all fields");
                    } else {
                        scrollView1.setVisibility(View.GONE);
                        scrollView2.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        findViewById(R.id.radioButtonDwiYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dwi = "Yes";
            }
        });
        findViewById(R.id.radioButtonDwiNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dwi = "No";
            }
        });
        findViewById(R.id.radioButtonLicenseYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                license = "Yes";
            }
        });
        findViewById(R.id.radioButtonLicenseNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                license = "No";
            }
        });
        findViewById(R.id.radioButtonLiftYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carry = "Yes";
            }
        });
        findViewById(R.id.radioButtonLiftNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carry = "No";
            }
        });
        findViewById(R.id.radioButtonFelonyYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                felony = "Yes";
            }
        });
        findViewById(R.id.radioButtonFelonyNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                felony = "No";
            }
        });
        findViewById(R.id.radioButtonInsYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insurance = "Yes";
            }
        });
        findViewById(R.id.radioButtonInsNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insurance = "No";
            }
        });
        findViewById(R.id.buttonNext2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwi.equals("") || license.equals("") || carry.equals("") || felony.equals("") || insurance.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else {
                    scrollView2.setVisibility(View.GONE);
                    scrollView3.setVisibility(View.VISIBLE);
                    checkPermissionStorage();
                }

            }
        });
        findViewById(R.id.buttonBack2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView2.setVisibility(View.GONE);
                scrollView1.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.buttonBack3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView3.setVisibility(View.GONE);
                scrollView2.setVisibility(View.VISIBLE);
            }
        });
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {

                    brandId = arrayListBrands.get(position).getString("brand_id");
                    //new GetModels().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.buttonCarReg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
                if (granted)
                    showPopUp(v);
                else {
                    showAlert();
                }
            }
        });
        findViewById(R.id.buttonInsurance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
                if (granted)
                    showPopUp(v);
                else {
                    showAlert();
                }
            }
        });
        findViewById(R.id.buttonCarImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
                if (granted)
                    showPopUp(v);
                else {
                    showAlert();
                }
            }
        });
        findViewById(R.id.buttonLicense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
                if (granted)
                    showPopUp(v);
                else {
                    showAlert();
                }
            }
        });
        findViewById(R.id.buttonInspection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
                if (granted)
                    showPopUp(v);
                else {
                    showAlert();
                }
            }
        });

        findViewById(R.id.buttonNext3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socialSecurity = editTextSocial.getText().toString().trim();
                for (int i = 0; i < 5; i++) {
                    APPHelper.showLog("image", arrayListImages.get(i));
                }
                if (socialSecurity.equals("") || imgPathCarReg.equals("") || imgPathIns.equals("") || imgPathCar.equals("") || imgPathLicense.equals("")
                        || imgPathInspection.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");

                } else if (!checkBoxAgree.isChecked()) {
                    APPHelper.showToast(ManageVehicleActivity.this, "Agree to the terms & Conditions");
                } else {
                    new AddVehicle().execute();
                }
            }
        });

        ChargeAsyncCallbacks chargeAsyncCallbacks = new ChargeAsyncCallbacks() {
            @Override
            public void onTaskStart() {
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTaskEnd(String result) {
                try {
                    ((TextView) findViewById(R.id.textViewCommission)).setVisibility(View.VISIBLE);
                    JSONObject object1 = new JSONObject(result);
                    String charges = object1.getString("commission_value");
                    ((TextView) findViewById(R.id.textViewCommission)).setText("As a driver you are responsible to pay taxes to your city/state’s proper authorities and follow your local driving regulation; platform fee is " + charges + "%");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                layoutProgress.setVisibility(View.GONE);
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("type", "Bargain");
        new GetCommission(chargeAsyncCallbacks, map).execute();
    }

    public void showAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManageVehicleActivity.this);
        alertDialogBuilder.setMessage("Allow Storage Permission");

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
    }

    public void gotoAppInfo() {


    }

    public void checkPermissionStorage() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            granted = false;
            /*ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    99);*/
        } else {
            granted = true;

            //
        }


        APPHelper.showLog("Storage", String.valueOf(granted));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 99:
                granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    APPHelper.showToast(getApplicationContext(), "Please Allow Storage Permission");
                }
                break;
        }

    }


    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(ManageVehicleActivity.this, v);
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
                            img1 = GetPath.createImageFile(ManageVehicleActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(ManageVehicleActivity.this, getPackageName() + ".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);
                            switch (v.getId()) {
                                case R.id.buttonCarReg:
                                    code[0] = 0;
                                    break;
                                case R.id.buttonInsurance:
                                    code[0] = 1;
                                    break;
                                case R.id.buttonCarImg:
                                    code[0] = 2;
                                    break;
                                case R.id.buttonLicense:
                                    code[0] = 3;
                                    break;
                                case R.id.buttonInspection:
                                    code[0] = 4;
                                    break;
                            }

                            startActivityForResult(pictureIntent,
                                    code[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                        /*Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT)*/
                        ;
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        switch (v.getId()) {
                            case R.id.buttonCarReg:
                                code[0] = 5;
                                break;
                            case R.id.buttonInsurance:
                                code[0] = 6;
                                break;
                            case R.id.buttonCarImg:
                                code[0] = 7;
                                break;
                            case R.id.buttonLicense:
                                code[0] = 8;
                                break;
                            case R.id.buttonInspection:
                                code[0] = 9;
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
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    imgPathCarReg = ResizeImage.getResizedImage(img1.getAbsolutePath());
                    Log.i("ImgCam", imgPathCarReg);
                    Glide.with(getApplicationContext()).load(imgPathCarReg).into(imageViewNumber);
                    arrayListImages.set(0, imgPathCarReg);
                    new UploadBill(0).execute();
                    break;
                case 1:
                    imgPathIns = ResizeImage.getResizedImage(img1.getAbsolutePath());
                    Log.i("ImgCam", imgPathIns);
                    Glide.with(getApplicationContext()).load(imgPathIns).into(imageViewIns);
                    arrayListImages.set(1, imgPathIns);
                    new UploadBill(1).execute();
                    break;
                case 2:
                    imgPathCar = ResizeImage.getResizedImage(img1.getAbsolutePath());
                    Log.i("ImgCam", imgPathCar);
                    Glide.with(getApplicationContext()).load(imgPathCar).into(imageViewCar);
                    arrayListImages.set(2, imgPathCar);
                    new UploadBill(2).execute();
                    break;
                case 3:
                    imgPathLicense = ResizeImage.getResizedImage(img1.getAbsolutePath());
                    Log.i("ImgCam", imgPathLicense);
                    Glide.with(getApplicationContext()).load(imgPathLicense).into(imageViewLicense);
                    arrayListImages.set(3, imgPathLicense);
                    new UploadBill(3).execute();
                    break;
                case 4:
                    imgPathInspection = ResizeImage.getResizedImage(img1.getAbsolutePath());
                    Log.i("ImgCam", imgPathInspection);
                    Glide.with(getApplicationContext()).load(imgPathInspection).into(imageViewInspection);
                    arrayListImages.set(4, imgPathInspection);
                    new UploadBill(4).execute();
                    break;
                case 5:
                    imgPathCarReg = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Log.i("ImgCam", imgPathCarReg);
                    Glide.with(getApplicationContext()).load(imgPathCarReg).into(imageViewNumber);
                    arrayListImages.set(0, imgPathCarReg);
                    new UploadBill(0).execute();
                    break;
                case 6:
                    imgPathIns = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Log.i("ImgCam", imgPathIns);
                    Glide.with(getApplicationContext()).load(imgPathIns).into(imageViewIns);
                    arrayListImages.set(1, imgPathIns);
                    new UploadBill(1).execute();
                    break;
                case 7:
                    imgPathCar = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Log.i("ImgCam", imgPathCar);
                    Glide.with(getApplicationContext()).load(imgPathCar).into(imageViewCar);
                    arrayListImages.set(2, imgPathCar);
                    new UploadBill(2).execute();
                    break;
                case 8:
                    imgPathLicense = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Log.i("ImgCam", imgPathLicense);
                    Glide.with(getApplicationContext()).load(imgPathLicense).into(imageViewLicense);
                    arrayListImages.set(3, imgPathLicense);
                    new UploadBill(3).execute();
                    break;
                case 9:
                    imgPathInspection = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Log.i("ImgCam", imgPathInspection);
                    Glide.with(getApplicationContext()).load(imgPathInspection).into(imageViewInspection);
                    arrayListImages.set(4, imgPathInspection);
                    new UploadBill(4).execute();
                    break;
            }
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
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_driver_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", userId + System.currentTimeMillis());
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            APPHelper.showLog("Image", arrayListImages.get(index));
            res = multipart.multipartRequest(url, map, arrayListImages.get(index), "driver", "image/*");
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
                    arrayListImages.set(index, jsonObject.getString("response"));
                }

                Log.i("Image", arrayListImages.toString());
            } catch (JSONException e) {
                Toast.makeText(ManageVehicleActivity.this, "Failed Please try again", Toast.LENGTH_SHORT).show();
                arrayListImages.set(index, "");
                Log.i("Image", arrayListImages.toString());
                if (index == 0) {
                    imageViewNumber.setImageResource(R.drawable.placeholder);
                } else if (index == 1) {
                    imageViewIns.setImageResource(R.drawable.placeholder);
                } else if (index == 2) {
                    imageViewCar.setImageResource(R.drawable.placeholder);
                } else if (index == 3) {
                    imageViewLicense.setImageResource(R.drawable.placeholder);
                } else if (index == 4) {
                    imageViewInspection.setImageResource(R.drawable.placeholder);
                }
                e.printStackTrace();
            }

        }
    }

    class GetBrands extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arrayListBrands.clear();
            arrayListBrandsSpinner.clear();
            adapterBrands.notifyDataSetChanged();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_vehicle_brands.php");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject();
                object.put("brand_id", "0");
                object.put("brand_name", "Select");
                arrayListBrands.add(object);
                arrayListBrandsSpinner.add("Select");
                JSONArray arrayCountries = new JSONArray(s);
                for (int i = 0; i < arrayCountries.length(); i++) {
                    object = arrayCountries.getJSONObject(i);
                    arrayListBrands.add(object);
                    arrayListBrandsSpinner.add(object.getString("brand_name"));
                }
                adapterBrands.notifyDataSetChanged();
                new GetColors().execute();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class GetColors extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arrayListColors.clear();
            arrayListColorsSpinner.clear();
            adapterColors.notifyDataSetChanged();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_vehicle_colors.php");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);

            try {

                /*JSONObject object = new JSONObject();
                object.put("model_id", "0");
                object.put("model_brand", "0");
                object.put("model_name", "Select");
                arrayListColors.add(object);
                arrayListColorsSpinner.add("Select");*/
                JSONArray arrayCountries = new JSONArray(s);
                for (int i = 0; i < arrayCountries.length(); i++) {
                    JSONObject object = arrayCountries.getJSONObject(i);
                    arrayListColors.add(object);
                    arrayListColorsSpinner.add(object.getString("color_name"));
                }
                adapterColors.notifyDataSetChanged();
                new GetVehicle().execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class AddVehicle extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("vehicle_id", vehicleId);
            map.put("vehicle_name", vehicleName);
            map.put("fname", name);
            map.put("lname", lName);
            map.put("mname", mName);
            map.put("brand_id", brandId);
            map.put("category", category);
            map.put("color", vehicleColor);
            map.put("year", year);
            map.put("dwi", dwi);
            map.put("felony", felony);
            map.put("carry", carry);
            map.put("license", license);
            map.put("insurance", insurance);
            map.put("vehicle_num", vehicleNum);
            map.put("vehicle_num_image", arrayListImages.get(0));
            map.put("insurance_image", arrayListImages.get(1));
            map.put("vehicle_image", arrayListImages.get(2));
            map.put("license_image", arrayListImages.get(3));
            map.put("inspection", arrayListImages.get(4));
            map.put("social_security", socialSecurity);
            return new RequestHandler().sendPostRequest(MAIN_URL + "add_vehicle.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    onBackPressed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GetVehicle extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("vehicle_id", vehicleId);
            map.put("user_id", active);
            return new RequestHandler().sendPostRequest(MAIN_URL + "vehicle_details.php", map);
            //return new RequestHandler().sendGetRequest(MAIN_URL + "get_vehicle_details.php?user_id=" + sharedPreferences.getString("userId", "0"));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            APPHelper.showLog("vehicle", s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        vehicleId = object.getString("vehicle_id");
                        //vehicleId1 = object.getString("vehicle_model_id");
                        vehicleName = object.getString("model_name");
                        brandName = object.getString("brand_name");
                        category = object.getString("vehicle_cat");
                        imgPathCar = object.getString("vehicle_image");
                        year = object.getString("vehicle_year");
                        vehicleColor = object.getString("vehicle_color");
                        dwi = object.getString("dwi");
                        insurance = object.getString("insurance");
                        carry = object.getString("carry");
                        felony = object.getString("felony");
                        license = object.getString("license");
                        imgPathCarReg = object.getString("vehicle_no_image");
                        vehicleNum = object.getString("vehicle_no");
                        imgPathIns = object.getString("vehicle_insurance");
                        imgPathLicense = object.getString("vehicle_license");
                        imgPathInspection = object.getString("vehicle_inspection");
                        spinnerCat.setSelection(adapterCat.getPosition(object.getString("vehicle_cat")));

                        spinnerYear.setSelection(adapterYears.getPosition(year));
                        if (arrayListBrands.size() == 0) {
                            new GetBrands().execute();
                        }
                        arrayListImages.set(0, imgPathCarReg);
                        arrayListImages.set(1, imgPathIns);
                        arrayListImages.set(2, imgPathCar);
                        arrayListImages.set(3, imgPathLicense);
                        arrayListImages.set(4, imgPathInspection);

                        //((TextView) findViewById(R.id.textViewColor)).setBackgroundColor(Color.parseColor(vehicleColor));
                        ((EditText) findViewById(R.id.editTextVehNum)).setText(vehicleNum);
                        if (dwi.equalsIgnoreCase("yes")) {
                            ((RadioButton) findViewById(R.id.radioButtonDwiYes)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonDwiNo).setEnabled(false);
                        } else {
                            ((RadioButton) findViewById(R.id.radioButtonDwiNo)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonDwiYes).setEnabled(false);
                        }
                        if (felony.equalsIgnoreCase("yes")) {
                            ((RadioButton) findViewById(R.id.radioButtonFelonyYes)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonFelonyNo).setEnabled(false);
                        } else {
                            ((RadioButton) findViewById(R.id.radioButtonFelonyNo)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonFelonyYes).setEnabled(false);
                        }
                        if (carry.equalsIgnoreCase("yes")) {
                            ((RadioButton) findViewById(R.id.radioButtonLiftYes)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonLiftNo).setEnabled(false);
                        } else {
                            ((RadioButton) findViewById(R.id.radioButtonLiftNo)).setChecked(true);
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonLiftYes).setEnabled(false);
                        }
                        if (insurance.equalsIgnoreCase("yes")) {
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonInsNo).setEnabled(false);
                            ((RadioButton) findViewById(R.id.radioButtonInsYes)).setChecked(true);
                        } else {
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonInsYes).setEnabled(false);
                            ((RadioButton) findViewById(R.id.radioButtonInsNo)).setChecked(true);
                        }
                        if (license.equalsIgnoreCase("yes")) {
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonLicenseNo).setEnabled(false);
                            ((RadioButton) findViewById(R.id.radioButtonLicenseYes)).setChecked(true);
                        } else {
                            if (object.getString("vehicle_approve").equals("Approved"))
                                findViewById(R.id.radioButtonLicenseYes).setEnabled(false);
                            ((RadioButton) findViewById(R.id.radioButtonLicenseNo)).setChecked(true);
                        }
                        loadImage(getApplicationContext(), imgPathCar, imageViewCar);
                        loadImage(getApplicationContext(), imgPathLicense, imageViewLicense);
                        loadImage(getApplicationContext(), imgPathInspection, imageViewInspection);
                        loadImage(getApplicationContext(), imgPathCarReg, imageViewNumber);
                        loadImage(getApplicationContext(), imgPathIns, imageViewIns);
                        socialSecurity = object.getString("social_security");
                        ((EditText) findViewById(R.id.editTextSocial)).setText(socialSecurity);
                        if (object.getString("vehicle_active").equals("1")) {
                            aSwitchActive.setChecked(true);
                        }
                        editTextModelName.setText(vehicleName);
                        editTextName.setText(object.getString("driver_fname"));
                        editTextMName.setText(object.getString("driver_mname"));
                        editTextLName.setText(object.getString("driver_lname"));

                        spinnerCat.setSelection(adapterCat.getPosition(category));
                        spinnerBrand.setSelection(adapterBrands.getPosition(brandName));
                        spinnerModel.setSelection(adapterColors.getPosition(object.getString("color_name")));
                        ((TextView) findViewById(R.id.textViewStatus)).setVisibility(View.VISIBLE);
                        if (object.getString("vehicle_approve").equals("Approved")) {
                            findViewById(R.id.layoutAgree).setVisibility(View.GONE);
                            ((CheckBox) findViewById(R.id.checkBoxAgree)).setVisibility(View.GONE);

                            spinnerCat.setEnabled(false);
                            ((TextView) findViewById(R.id.textBrand)).setText(brandName);
                            spinnerBrand.setEnabled(false);
                            spinnerBrand.setVisibility(View.GONE);

                            editTextModelName.setFocusable(false);
                            spinnerYear.setEnabled(false);
                            ((TextView) findViewById(R.id.textBrand)).setText(brandName);
                            spinnerModel.setEnabled(false);
                            spinnerModel.setVisibility(View.GONE);
                            findViewById(R.id.textViewColor).setVisibility(View.VISIBLE);
                            findViewById(R.id.textBrand).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.textViewColor)).setText(object.getString("color_name"));
                            ((EditText) findViewById(R.id.editTextVehNum)).setFocusable(false);
                            editTextName.setFocusable(false);
                            editTextMName.setFocusable(false);
                            editTextLName.setFocusable(false);

                            ((EditText) findViewById(R.id.editTextSocial)).setFocusable(false);
                            findViewById(R.id.buttonCarImg).setVisibility(View.GONE);
                            findViewById(R.id.buttonCarReg).setVisibility(View.GONE);
                            findViewById(R.id.buttonInspection).setVisibility(View.GONE);
                            findViewById(R.id.buttonInsurance).setVisibility(View.GONE);
                            findViewById(R.id.buttonLicense).setVisibility(View.GONE);
                            findViewById(R.id.buttonNext3).setVisibility(View.GONE);
                            aSwitchActive.setVisibility(View.VISIBLE);

                        } else {
                            aSwitchActive.setVisibility(View.GONE);
                        }

                        menuBargain.findItem(R.id.action_delete).setVisible(true);
                        ((TextView) findViewById(R.id.textViewStatus)).setText(object.getString("vehicle_approve"));
                    }


                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class UpdateActive extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("vehicle_id", vehicleId);
            map.put("user_id", userId);
            map.put("status", active);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_vehicle_status.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    setResult(RESULT_OK);
                }
                //APPHelper.showToast(getApplicationContext(), jsonObject.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_delete, menu);
        menuBargain = menu;
        menuBargain.findItem(R.id.action_delete).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public static void loadImage(Context context, String path, ImageView imageView) {
        Glide.with(context).load(MAIN_URL_IMAGE + path).into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_delete:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                HashMap<String, String> map = new HashMap<>();
                                map.put("vehicle_id", vehicleId);
                                apiService.callAPI(map, MAIN_URL + "delete_vehicle.php", "delete");
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Delete this listing?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

        }
        return super.onOptionsItemSelected(item);
    }

    public void showAlert(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManageVehicleActivity.this);
        alertDialogBuilder.setMessage(msg);

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
    }

    public void showAlert2() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManageVehicleActivity.this);
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
}
