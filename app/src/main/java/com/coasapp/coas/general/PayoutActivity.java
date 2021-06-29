package com.coasapp.coas.general;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.GetPath;

import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PayoutActivity extends AppCompatActivity implements APPConstants {

    EditText editTextCardNum, editTextMonth, editTextYear, editTextFirstName, editTextMiddleName, editTextLastName, editTextAddress, editTextCity,
            editTextState, editTextZip, editTextRoutingNum, editTextAccNum, editTextBank;
    Spinner spinnerYear, spinnerMonth;
    TextView textViewStatus;


    List<String> listMonth = new ArrayList<>();
    List<String> listYear = new ArrayList<>();
    ArrayAdapter<String> adapterMonths;
    ArrayAdapter<String> adapterYear;

    LinearLayout layoutProgress;
    String payoutId = "0";
    ImageView imageView;
    String depositImage = "";
    File img1;
    boolean granted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout);
        checkPermissionStorage();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");
        layoutProgress = findViewById(R.id.layoutProgress);
        editTextCardNum = findViewById(R.id.editTextCardNum);
        editTextBank = findViewById(R.id.editTextBankName);
        //editTextCardNum = findViewById(R.id.editTextCardNum);
        editTextRoutingNum = findViewById(R.id.editTextRoutingNum);
        editTextAccNum = findViewById(R.id.editTextAccNum);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextMiddleName = findViewById(R.id.editTextMiddleName);
        editTextLastName = findViewById(R.id.editTextLastName);

        editTextAddress = findViewById(R.id.editTextAddress);
        editTextCity = findViewById(R.id.editTextCity);
        editTextState = findViewById(R.id.editTextState);
        editTextZip = findViewById(R.id.editTextZip);
        imageView = findViewById(R.id.imageViewDeposit);
        textViewStatus = findViewById(R.id.textViewStatus);
        for (int i = 1; i < 13; i++) {
            if (i < 10) {
                listMonth.add("0" + i);
            } else {
                listMonth.add("" + i);
            }
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        for (int i = year; i < year + 21; i++) {
            listYear.add("" + i);
        }
        adapterMonths = new ArrayAdapter<>(PayoutActivity.this, android.R.layout.simple_spinner_item, listMonth);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonths);

        adapterYear = new ArrayAdapter<>(PayoutActivity.this, android.R.layout.simple_spinner_item, listYear);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        new GetPayout().execute(userId);
        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNum = editTextRoutingNum.getText().toString();
               /* String month = spinnerMonth.getSelectedItem().toString();
                String year = spinnerYear.getSelectedItem().toString();*/
                String bank = editTextBank.getText().toString();
                String accNum = editTextAccNum.getText().toString();
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String middleName = editTextMiddleName.getText().toString().trim();
                String address = editTextAddress.getText().toString().trim();
                String city = editTextCity.getText().toString().trim();
                String state = editTextState.getText().toString().trim();
                String zip = editTextZip.getText().toString().trim();
                if (cardNum.length() < 9) {
                    APPHelper.showToast(getApplicationContext(), "Enter Valid Routing Number");
                } else if (accNum.length() < 9) {
                    APPHelper.showToast(getApplicationContext(), "Enter Valid Account Number");
                } else if (bank.length() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Enter Bank Name");
                } else if (!InputValidator.isName(firstName) || !InputValidator.isName(middleName) || !InputValidator.isName(lastName)) {
                    APPHelper.showToast(getApplicationContext(), "Enter Valid First, Middle & Last Name");

                } else if (depositImage.length() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Upload Deposit Image");
                } else if (address.length() == 0 || state.length() == 0 || city.length() == 0 || zip.length() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Enter Address, City, State & Zip");

                } else {
                    new UpdatePayout().execute(cardNum, accNum, bank, firstName, lastName, address, city, state, zip, userId, middleName);
                }
            }
        });

        editTextAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(PayoutActivity.this);
                    startActivityForResult(intent, 2);

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/
                APPHelper.launchSelectAddressActivity(PayoutActivity.this, 2);
            }
        });

        findViewById(R.id.buttonChoose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (granted) {
                    showPopUp(v);
                } else {
                    showAlert();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                try {
                    Geocoder geocoder = new Geocoder(PayoutActivity.this);
                    // place = PlaceAutocomplete.getPlace(this, data);
                    //


                    List<Address> addresses = new ArrayList<>();
                    addresses = geocoder.getFromLocation(
                            data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0),
                            // In this sample, get just a single address.
                            1);
                    Address address = addresses.get(0);

                    editTextAddress.setText(data.getStringExtra("address"));
                    editTextCity.setText(address.getLocality());
                    editTextState.setText(address.getAdminArea());
                    editTextZip.setText(address.getPostalCode());
                } catch (Exception e) {
                    APPHelper.showLog("Place", e.getMessage());
                }

            } else if (requestCode == 0) {
                depositImage = ResizeImage.getResizedImage(img1.getAbsolutePath());
                Glide.with(getApplicationContext()).load(depositImage).into(imageView);
                new UploadBill().execute();

            } else if (requestCode == 5) {
                depositImage = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                Glide.with(getApplicationContext()).load(depositImage).into(imageView);
                new UploadBill().execute();
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
            String res = "";
            String url = MAIN_URL + "upload_payout_image.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", String.valueOf(System.currentTimeMillis()));
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, depositImage, "payout", "image/*");
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
                    depositImage = jsonObject.getString("response");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class UpdatePayout extends AsyncTask<String, Void, String> {
String userId="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("payout_id", payoutId);
            userId = strings[9];
            //map.put("card_num", strings[0]);
            //map.put("month", strings[1]);
            //map.put("year", strings[2]);
            map.put("routing_num", strings[0]);
            map.put("account_num", strings[1]);
            map.put("bank_name", strings[2]);
            map.put("f_name", strings[3]);
            map.put("l_name", strings[4]);
            map.put("address", strings[5]);
            map.put("city", strings[6]);
            map.put("state", strings[7]);
            map.put("zip", strings[8]);
            map.put("user_id", strings[9]);
            map.put("m_name", strings[10]);
            map.put("deposit_image", depositImage);
            String url = MAIN_URL + "update_payout.php";
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    textViewStatus.setTextColor(Color.BLACK);
                    new GetPayout().execute(userId);
                }
                APPHelper.showToast(getApplicationContext(), object.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class GetPayout extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();

            map.put("user_id", strings[0]);
            String url = MAIN_URL + "get_payout.php";
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject objectR = new JSONObject(s);
                JSONArray array = objectR.getJSONArray("payout");

                if (array.length() > 0) {
                    JSONObject object = array.getJSONObject(0);
                    payoutId = object.getString("payout_id");
                    editTextBank.setText(object.getString("payout_bank_name"));
                    editTextRoutingNum.setText(object.getString("payout_routing_num"));
                    editTextAccNum.setText(object.getString("payout_account_num"));
                  /*  spinnerMonth.setSelection(adapterMonths.getPosition(object1.getString("payout_card_exp_month")));
                    spinnerYear.setSelection(adapterYear.getPosition(object1.getString("payout_card_exp_year")));*/
                    editTextFirstName.setText(object.getString("payout_fname"));
                    editTextMiddleName.setText(object.getString("payout_lname"));
                    editTextLastName.setText(object.getString("payout_lname"));
                    editTextAddress.setText(object.getString("payout_address"));
                    editTextCity.setText(object.getString("payout_city"));
                    editTextState.setText(object.getString("payout_state"));
                    editTextZip.setText(object.getString("payout_zip"));
                    textViewStatus.setText(object.getString("payout_status"));
                    if (object.getString("payout_status").equals("Rejected")) {
                        textViewStatus.setTextColor(Color.RED);
                    } else if (object.getString("payout_status").equals("Approved")) {
                        textViewStatus.setTextColor(Color.parseColor("#0F9D58"));
                    }
                    depositImage = object.getString("payout_deposit_image");
                    Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + depositImage).into(imageView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
    }

    public void showAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PayoutActivity.this);
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

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(PayoutActivity.this, v);
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
                            img1 = GetPath.createImageFile(PayoutActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(PayoutActivity.this, getPackageName(), img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);

                            code[0] = 0;


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
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");

                        code[0] = 5;
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code[0]);


                }

                return true;
            }
        });

        popupMenu.show();
    }
}
