package com.coasapp.coas.shopping;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;

public class EditProductActivity extends AppCompatActivity implements APPConstants {
    MyProductImagesAdapter imagesAdapter;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    int pos = 0;
    EditText editTextAdd, editTextState;
    double lat, lng;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    String jsonArray, status;
    String productId = "0", catId = "0", subCat = "0", brandId = "0", brand = "", city = "", state = "", address = "", currency = "", country = "";
    Spinner spinnerBrand;
    ArrayList<JSONObject> arrayListBrands = new ArrayList<>();
    List<String> arrayListBrandsSpinner = new ArrayList<>();
    LinearLayout layoutProgress;
    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayList<String> arrayListCurrencySpinner = new ArrayList<>();
    ArrayAdapter<String> adapterCountries, adapterCurrency;
    Spinner spinnerCountry, spinnerCurrency;
    Geocoder geocoder;
    File img1;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE/*, Manifest.permission.CAMERA*/};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        geocoder = new Geocoder(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NumberPicker numberPicker = findViewById(R.id.numberPickerPro);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        String details = getIntent().getStringExtra("details");
        Switch aSwitch = findViewById(R.id.switchStatus);
        layoutProgress = findViewById(R.id.layoutProgress);
        RecyclerView recyclerViewImages = findViewById(R.id.recyclerViewImages);
        final RadioButton checkBox = findViewById(R.id.checkBoxDefault);
        final EditText editTextProduct = findViewById(R.id.editTextProductName);
        final EditText editTextPDesc = (EditText) findViewById(R.id.editTextDesc);
        final TextView textViewCat = findViewById(R.id.textViewCat);
        spinnerBrand = findViewById(R.id.spinnerBrand);
        editTextState = (EditText) findViewById(R.id.editTextState);
        editTextAdd = (EditText) findViewById(R.id.editTextCity);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        //final EditText editTextCount = (EditText) findViewById(R.id.editTextQty);
        final EditText editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        final EditText editTextTags = (EditText) findViewById(R.id.editTextTags);
        final Spinner spinner = findViewById(R.id.spinnerCondition);
        final ImageView imageView = findViewById(R.id.imageViewProduct);
        adapterCountries = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapterCountries);
        adapterCurrency = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListCurrencySpinner);
        adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapterCurrency);
        imagesAdapter = new MyProductImagesAdapter(productImagesArrayList, getApplicationContext());
        recyclerViewImages.setAdapter(imagesAdapter);

        editTextAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(EditProductActivity.this);
                    startActivityForResult(intent, 2);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/
                APPHelper.launchSelectAddressActivity(EditProductActivity.this, 2);
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        try {
            JSONArray arrayCountries = new JSONArray(sharedPreferences.getString("countries", "[]"));
            for (int i = 0; i < arrayCountries.length(); i++) {
                JSONObject object = arrayCountries.getJSONObject(i);
                arrayListCountries.add(object);
                //arrayListCountriesSpinner.add(object.getString("country_name"));
            }
            arrayListCurrencySpinner.add("USD");

            adapterCountries.notifyDataSetChanged();
            adapterCurrency.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status = "1";
                } else {
                    status = "0";
                }
            }
        });

        textViewCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SellerCategoriesActivity.class);
                intent.putExtra("mode", "edit");
                startActivityForResult(intent, 99);
            }
        });

        findViewById(R.id.imageViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    showAlert("Please grant storage permission");

                } else {
                    /*Intent intent = new Intent();
                    intent.setType("image/*");
                    //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);*/
                    showPopUp(v);
                }


            }
        });

        try {
            JSONObject object = new JSONObject(details);
            catId = object.getString("category");
            subCat = object.getString("subcategory");
            brand = object.getString("brand_name");
            productId = object.getString("productid");
            lat = Double.parseDouble(object.getString("pro_lat"));
            lng = Double.parseDouble(object.getString("pro_lng"));
            spinnerCurrency.setSelection(adapterCountries.getPosition(object.getString("pro_currency")));

            APPHelper.showLog("Log", object.getString("pro_condition"));
            if (object.getString("pro_condition").equalsIgnoreCase("used")) {
                spinner.setSelection(2);
            }
            if (object.getString("pro_condition").equalsIgnoreCase("new")) {
                spinner.setSelection(1);
            }
            status = object.getString("status");
            if (object.getString("status").equalsIgnoreCase("1")) {
                aSwitch.setChecked(true);
            }
            /*if(object.getString("pro_approve").equalsIgnoreCase("Approved")){
                editTextPrice.setFocusable(false);
            }*/
            editTextPrice.setFocusable(false);
            editTextAdd.setText(object.getString("pro_address"));
            numberPicker.setValue(Integer.parseInt(object.getString("count")));
            editTextProduct.setText(object.getString("pro_name"));
            textViewCat.setText(object.getString("category_name"));
            editTextPrice.setText(object.getString("price"));
            editTextPDesc.setText(object.getString("pro_descs"));
            editTextTags.setText(object.getString("pro_tags"));
            city = object.getString("pro_city");
            state = object.getString("pro_state");
            address = object.getString("pro_address");
            country = object.getString("pro_country");
            editTextAdd.setText(object.getString("pro_city"));
            editTextState.setText(object.getString("pro_state"));

            JSONArray jsonArray = new JSONArray(object.getString("images"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ProductImages productImages = new ProductImages();
                productImages.setId(jsonObject.getString("image_id"));
                productImages.setImage(jsonObject.getString("image"));
                productImages.setColor("#fffafafa");
                productImages.setSource("url");
                productImages.setStatus(jsonObject.getString("status"));
                productImagesArrayList.add(productImages);
            }
            if (productImagesArrayList.size() > 0) {
                productImagesArrayList.get(0).setColor("#ffd50000");
                Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + productImagesArrayList.get(0).getImage()).into(imageView);

            }
            checkBox.setChecked(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        imagesAdapter.notifyDataSetChanged();

        imagesAdapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
            @Override
            public void onClick(int position) {
                pos = position;
                ProductImages productImages = productImagesArrayList.get(position);
                for (int i = 0; i < productImagesArrayList.size(); i++) {
                    productImagesArrayList.get(i).setColor("#fffafafa");
                }
                productImagesArrayList.get(position).setColor("#ffd50000");
                imagesAdapter.notifyDataSetChanged();
                if (productImages.getStatus().equals("1")) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + productImagesArrayList.get(position).getImage()).into(imageView);

            }
        });
        imagesAdapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
            @Override
            public void onClick(int position) {
                ProductImages productImages = productImagesArrayList.get(position);

                if (productImages.getStatus().equalsIgnoreCase("1")) {
                    APPHelper.showToast(getApplicationContext(), "Cannot remove default image");
                } else {

                    new DelImage(position).execute(productImages.getId());

                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    APPHelper.showLog("Pos", String.valueOf(pos));
                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                        productImagesArrayList.get(i).setStatus("0");
                    }
                    productImagesArrayList.get(pos).setStatus("1");
                    imagesAdapter.notifyDataSetChanged();
                }
            }
        });
        new GetBrands().execute();
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JSONObject object = arrayListBrands.get(position);
                try {
                    brandId = object.getString("brand_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });
        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = editTextTags.getText().toString();
                String pName = editTextProduct.getText().toString().trim();
                String pDesc = editTextPDesc.getText().toString().trim();
                String sku = spinner.getSelectedItem().toString();
                String qty = String.valueOf(numberPicker.getValue());
                String price = editTextPrice.getText().toString().trim();
                try {
                    currency = arrayListCountries.get(spinnerCurrency.getSelectedItemPosition()).getString("currency");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (/*tags.equalsIgnoreCase("") ||*/ pName.equals("") || pDesc.equals("") || sku.contains("Select") || qty.equals("") || price.equals("")) {
                    Toast.makeText(EditProductActivity.this, "Fill All Fields", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(qty) < 1) {
                    APPHelper.showToast(EditProductActivity.this, "Enter Quantity");
                } else if (!InputValidator.isValidPrice(price)) {
                    APPHelper.showToast(EditProductActivity.this, "Enter Price between 1 & 50000");
                } else if (productImagesArrayList.size() == 0) {
                    APPHelper.showToast(EditProductActivity.this, "Upload Image(s)");
                } else {
                    try {
                        JSONArray array = new JSONArray();

                        String images = "[]";

                        jsonObjectArrayList.clear();
                        for (int i = 0; i < productImagesArrayList.size(); i++) {
                            JSONObject object = new JSONObject();
                            object.put("image", productImagesArrayList.get(i).getImage());
                            object.put("status", productImagesArrayList.get(i).getStatus());
                            jsonObjectArrayList.add(object);
                        }

                        for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                            array.put(jsonObjectArrayList.get(i));
                        }

                        jsonArray = array.toString();
                        APPHelper.showLog("Images", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addProduct(pName, pDesc, editTextAdd.getText().toString().trim(), sku, qty, "", price, tags);
                }
            }
        });
    }

    public void showAlert(String errorMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditProductActivity.this);
        alertDialogBuilder.setMessage(errorMsg);

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

    class GetBrands extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_brands.php?cat_id=" + subCat);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONArray jsonArrayBrands = new JSONArray(s);

                JSONObject object = new JSONObject();
                object.put("brand_id", "0");
                object.put("brand_name", "Not Branded");
                object.put("brand_cat_id", subCat);
                arrayListBrandsSpinner.add("Not Branded");
                arrayListBrands.add(object);

                for (int i = 0; i < jsonArrayBrands.length(); i++) {
                    object = jsonArrayBrands.getJSONObject(i);
                    arrayListBrands.add(object);
                    arrayListBrandsSpinner.add(object.getString("brand_name"));
                }

                ArrayAdapter<String> adapterBrands = new ArrayAdapter<String>(
                        EditProductActivity.this,
                        android.R.layout.simple_spinner_item,
                        arrayListBrandsSpinner
                );
                adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBrand.setAdapter(adapterBrands);
                spinnerBrand.setSelection(adapterBrands.getPosition(brand));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class DelImage extends AsyncTask<String, Integer, String> {

        int index;

        public DelImage(int index) {
            this.index = index;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(final String... params) {
            String res = "";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APPHelper.showLog("image", params[0]);
                }
            });
            final String url = MAIN_URL + "delete_product_image.php?image_id=" + params[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APPHelper.showLog("image", url);
                }
            });
            return new RequestHandler().sendGetRequest(url);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    /*JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    object.put("status", "0");
                    jsonObjectArrayList.add(object);*/
                    productImagesArrayList.remove(index);
                    imagesAdapter.notifyDataSetChanged();
                }
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
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Uri mImageUri = data.getData();
                // Get the cursor
//                Cursor cursor = getContentResolver().query(mImageUri,
//                        filePathColumn, null, null, null);
//                // Move to first row
//                cursor.moveToFirst();
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imageEncoded = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), mImageUri));
                ProductImages productImages = new ProductImages();
                productImages.setImage(imageEncoded);
                productImages.setStatus("0");
                productImages.setColor("#ff000000");
                productImages.setSource("file");
                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                int index = productImagesArrayList.size() - 1;
                new UploadBill(index).execute(index);
            } else if (requestCode == 0) {

                // Get the cursor
//                Cursor cursor = getContentResolver().query(mImageUri,
//                        filePathColumn, null, null, null);
//                // Move to first row
//                cursor.moveToFirst();
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imageEncoded = ResizeImage.getResizedImage(img1.getPath());
                ProductImages productImages = new ProductImages();
                productImages.setImage(imageEncoded);
                productImages.setSource("file");
                productImages.setStatus("0");
                productImages.setColor("#ff000000");

                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                int index = productImagesArrayList.size() - 1;
                new UploadBill(index).execute(index);
            } else if (requestCode == 2) {


                    /*List<Address> addresses = null;
                    address = place.getAddress().toString();
                    lat = place.getLatLng().latitude;
                    lng = place.getLatLng().longitude;
                    try {
                        addresses = geocoder.getFromLocation(
                                place.getLatLng().latitude, place.getLatLng().longitude,
                                // In this sample, get just a single address.
                                1);
                    } catch (IOException ioException) {
                        // Catch network or other I/O problems.
                        Log.e("Location", ioException.getMessage());
                    } catch (IllegalArgumentException illegalArgumentException) {
                        // Catch invalid latitude or longitude values.

                    }*/

                // Handle case where no address was found.
                address = data.getStringExtra("address");
                lat = data.getDoubleExtra("latitude", 0);
                lng = data.getDoubleExtra("longitude", 0);

                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(
                            lat, lng,
                            // In this sample, get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e("Location", ioException.getMessage());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.

                }
                /*Place place = PlaceAutocomplete.getPlace(this, data);

                address1 = place.getAddress().toString();
                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
                try {
                    addresses = geocoder.getFromLocation(
                            place.getLatLng().latitude, place.getLatLng().longitude,
                            // In this sample, get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e("Location", ioException.getMessage());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.

                }
*/
                // Handle case where no address was found.
                if (addresses == null || addresses.size() == 0) {

                    state = "";
                    city = "";

                } else {

                    Address address = addresses.get(0);

                    ArrayList<String> addressFragments = new ArrayList<String>();

                    state = address.getAdminArea();
                    city = address.getLocality();
                    country = address.getCountryName();
                    editTextAdd.setText(city);
                    editTextState.setText(state);
                    spinnerCountry.setSelection(adapterCountries.getPosition(country));
                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.


                }

            } else if (requestCode == 99) {
                catId = data.getStringExtra("cat_id");
                subCat = data.getStringExtra("sub_cat_id");
                Toast.makeText(this, data.getStringExtra("cat_name"), Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.textViewCat)).setText(data.getStringExtra("cat_name"));
            }
        }

    }


    void addProduct(final String name, final String desc, final String addredd, final String cond, final String count, final String mrp, final String price, final String tags) {


        class AddProduct extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<>();
                map.put("pro_id", productId);
                map.put("product_name", name);
                map.put("desc", desc);
                map.put("count", count);
                map.put("condition", cond);
                map.put("brand", brandId);
                map.put("price", price);
                map.put("address", address);
                map.put("city", city);
                map.put("state", state);
                map.put("country", country);
                map.put("currency", currency);
                map.put("tags", tags);
                map.put("status", status);
                map.put("lat", String.valueOf(lat));
                map.put("lng", String.valueOf(lng));
                map.put("sub_cat", subCat);
                map.put("category", catId);
                map.put("images", jsonArray);
                return new RequestHandler().sendPostRequest(MAIN_URL + "edit_product.php", map);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                layoutProgress.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Toast.makeText(EditProductActivity.this, jsonObject.getString("response"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("response_code").equals("1")) {
                        /*String productId = jsonObject.getString("p_id");
                        addProductImages(productId);*/
                        setResult(RESULT_OK);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        new AddProduct().execute();
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
            String url = MAIN_URL + "upload_product_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("status", productImagesArrayList.get(params[0]).getStatus());
            APPHelper.showLog("image", "" + productImagesArrayList.get(params[0]).getStatus());
            map.put("file_name", "" + System.currentTimeMillis());
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, productImagesArrayList.get(params[0]).getImage(), "room", "image/*");
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
                    /*JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    object.put("status", "0");
                    jsonObjectArrayList.add(object);*/
                    productImagesArrayList.get(index).setImage(jsonObject.getString("response"));
                    productImagesArrayList.get(index).setSource("url");
                    imagesAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Failed Please try again", Toast.LENGTH_SHORT).show();
                productImagesArrayList.remove(index);
                imagesAdapter.notifyDataSetChanged();
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

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(EditProductActivity.this, v);
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
                            img1 = GetPath.createImageFile(EditProductActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(EditProductActivity.this, getPackageName() + ".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);
                            switch (v.getId()) {
                                case R.id.buttonAdd:
                                    code[0] = 0;
                                    break;
                            }

                            startActivityForResult(pictureIntent,
                                    0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        switch (v.getId()) {
                            case R.id.buttonAdd:
                                code[0] = 1;
                                break;
                        }
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                        break;

                }

                return true;
            }
        });

        popupMenu.show();
    }
}
