package com.coasapp.coas.bargain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddVehicleActivity extends AppCompatActivity implements APPConstants {

    ImageView imageViewVehicle;
    String vehicleName, vehicleNo, category, desc, seats, vehicleId;
    EditText editTextVehDesc, editTextVehName, editTextVehNo;
    Spinner spinnerCat, spinnerSeat;
    ArrayAdapter<CharSequence> arrayAdapterCat;
    ArrayAdapter<CharSequence> arrayAdapterSeat;
    String filepath = "", image = "";
    LinearLayout layoutProgress;
    Switch aSwitchActive;
    String active = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        aSwitchActive = findViewById(R.id.switchOnline);
        layoutProgress = findViewById(R.id.layoutProgress);
        editTextVehName = findViewById(R.id.editTextVehName);
        editTextVehDesc = findViewById(R.id.editTextDesc);
        editTextVehNo = findViewById(R.id.editTextVehNum);
        spinnerCat = findViewById(R.id.spinnerCategory);
        spinnerSeat = findViewById(R.id.spinnerPersons);
        Button buttonSave = findViewById(R.id.buttonSave);
        imageViewVehicle = findViewById(R.id.imageViewVehicle);
        arrayAdapterCat = ArrayAdapter.createFromResource(this,
                R.array.VehType, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        arrayAdapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerCat.setAdapter(arrayAdapterCat);
        arrayAdapterSeat = ArrayAdapter.createFromResource(this,
                R.array.Seats, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        arrayAdapterSeat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerSeat.setAdapter(arrayAdapterSeat);
        new GetVehicle().execute();
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleName = editTextVehName.getText().toString();
                vehicleNo = editTextVehNo.getText().toString();
                category = spinnerCat.getSelectedItem().toString();
                desc = editTextVehDesc.getText().toString();
                seats = spinnerSeat.getSelectedItem().toString();
                if (vehicleName.trim().length() == 0 || desc.length() == 0 || seats.length() == 0 || vehicleNo.trim().length() == 0) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else if (filepath.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Upload Image");

                } else {
                    if (!filepath.equalsIgnoreCase(image)) {
                        new UploadBill().execute(0);
                    } else {
                        new AddProduct().execute();
                    }

                }

            }
        });
        aSwitchActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    active = "1";
                } else {
                    active = "0";
                }
                new UpdateActive().execute();
            }
        });
        imageViewVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
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
            if (requestCode == 1) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Uri mImageUri = data.getData();
                // Get the cursor
                Cursor cursor = getContentResolver().query(mImageUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filepath = GetPath.getPath(getApplicationContext(), data.getData());
                Glide.with(getApplicationContext()).load(filepath).into(imageViewVehicle);
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
            String url = MAIN_URL + "upload_room_image.php";
            HashMap<String, String> map = new HashMap<>();
            map.put("status", "0");
            map.put("file_name", "" + System.currentTimeMillis());
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, filepath, "room", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {

                    image = jsonObject.getString("response");
                    new AddProduct().execute();
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
            HashMap<String,String> map=new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_vehicle_details.php",map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            APPHelper.showLog("vehicle", s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                if (jsonArray.length() > 0) {
                    aSwitchActive.setVisibility(View.VISIBLE);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        vehicleId = object.getString("vehicle_id");
                        vehicleName = object.getString("vehicle_name");
                        desc = object.getString("vehicle_desc");
                        image = object.getString("vehicle_image");
                        category = object.getString("vehicle_cat");
                        seats = object.getString("vehicle_seats");
                        image = object.getString("vehicle_image");
                        editTextVehName.setText(object.getString("vehicle_name"));
                        editTextVehDesc.setText(object.getString("vehicle_desc"));
                        editTextVehNo.setText(object.getString("vehicle_no"));
                        spinnerCat.setSelection(arrayAdapterCat.getPosition(object.getString("vehicle_cat")));
                        spinnerSeat.setSelection(arrayAdapterSeat.getPosition(object.getString("vehicle_seats")));
                        filepath = image;
                        if (object.getString("vehicle_active").equals("1")) {
                            aSwitchActive.setChecked(true);
                        }
                        Glide.with(getApplicationContext()).load(object.getString("vehicle_image")).into(imageViewVehicle);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class AddProduct extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            map.put("vehicle_name", vehicleName);
            map.put("desc", desc);
            map.put("vehicle_no", vehicleNo);
            map.put("seat", seats);
            map.put("category", category);
            map.put("image", image);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_vehicle.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    SharedPreferences sharedPreferencesCar = getSharedPreferences(CAR_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesCar.edit();
                    editor.putString("vehicle_id", jsonObject.getString("vehicle_id"));
                    editor.putString("vehicle_name", vehicleName);
                    editor.putString("vehicle_num", vehicleNo);
                    editor.putString("seats", seats);
                    editor.putString("vehicle_desc", desc);
                    editor.putString("category", category);
                    editor.putString("vehicle_image", image);
                    editor.apply();
                }
                APPHelper.showToast(getApplicationContext(), jsonObject.getString("response"));
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
            map.put("status", active);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_vehicle_status.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {

                }
                //APPHelper.showToast(getApplicationContext(), jsonObject.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
