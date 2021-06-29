package com.coasapp.coas.shopping;

import android.Manifest;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.payment.StripePaymentActivity;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.webservices.GetCommission;

import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AddProductActivity extends AppCompatActivity implements APPConstants {

    MyProductImagesAdapter imagesAdapter;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> arrayListBrands = new ArrayList<>();
    List<String> arrayListBrandsSpinner = new ArrayList<>();
    String jsonArray;
    EditText editTextCity, editTextState;
    Spinner spinnerBrand;
    String brandId = "0";
    double lat, lng;
    String tags = "", city = "", state = "", address1 = "", country = "", price = "0.00", orderId;
    Geocoder geocoder;
    Spinner spinnerCountry, spinnerCurrency;
    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayList<String> arrayListCurrencySpinner = new ArrayList<>();
    LinearLayout layoutProgress;
    ArrayAdapter<String> adapterCountries, adapterCurrency;
    File img1;
    double charges;
    String currency = "USD";
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE/*, Manifest.permission.CAMERA*/};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        ActivityCompat.requestPermissions(this, permissions, 99);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NumberPicker numberPicker = findViewById(R.id.numberPickerPro);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        layoutProgress = findViewById(R.id.layoutProgress);
        adapterCountries = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapterCountries);
        adapterCurrency = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListCurrencySpinner);
        adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapterCurrency);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        try {
            JSONArray arrayCountries = new JSONArray(sharedPreferences.getString("countries", "[]"));
            for (int i = 0; i < arrayCountries.length(); i++) {
                JSONObject object = arrayCountries.getJSONObject(i);
                arrayListCountries.add(object);
                //arrayListCountriesSpinner.add(object.getString("country_name"));
                //arrayListCurrencySpinner.add(object.getString("currency"));
            }
            arrayListCurrencySpinner.add("USD");

            adapterCountries.notifyDataSetChanged();
            adapterCurrency.notifyDataSetChanged();
            spinnerCurrency.setSelection(adapterCurrency.getPosition(sharedPreferences.getString("currency", "USD")));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        geocoder = new Geocoder(getApplicationContext());
        RecyclerView recyclerViewImages = findViewById(R.id.recyclerViewImages);
        //recyclerViewImages.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new MyProductImagesAdapter(productImagesArrayList, getApplicationContext());
        recyclerViewImages.setAdapter(imagesAdapter);
        final EditText editTextProduct = findViewById(R.id.editTextProductName);
        final EditText editTextPDesc = (EditText) findViewById(R.id.editTextDesc);
        editTextCity = (EditText) findViewById(R.id.editTextCity);
        editTextState = (EditText) findViewById(R.id.editTextState);
        final EditText editTextCount = (EditText) findViewById(R.id.editTextQty);
        final EditText editTextMRP = (EditText) findViewById(R.id.editTextMrp);
        final EditText editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        final TextView textViewPrice = findViewById(R.id.textViewPrice);
        final EditText editTextTags = (EditText) findViewById(R.id.editTextTags);
        final Spinner spinner = findViewById(R.id.spinnerCondition);
        CheckBox checkBoxAgree = findViewById(R.id.checkBoxAgree);
        spinnerBrand = findViewById(R.id.spinnerBrand);
        editTextCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(AddProductActivity.this);
                    startActivityForResult(intent, 2);

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/
                APPHelper.launchSelectAddressActivity(AddProductActivity.this, 2);
            }
        });
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
                    APPHelper.launchChrome(AddProductActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");
                    /*Intent intent = new Intent(getApplicationContext(), WebViewProfileActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    startActivity(intent);*/
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
                    APPHelper.launchChrome(AddProductActivity.this, APPConstants.baseUrlLocal2 + "terms-conditions/");
                }
            }

        });

        APPHelper.setTerms(this,findViewById(R.id.layoutAgree));

        final String userId = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0");

       /* ProductImages productImages = new ProductImages();
        productImages.setPlaceholder(R.drawable.placeholder);
        if (productImagesArrayList.size() == 0) {
            productImages.setStatus("1");
        } else {
            productImages.setStatus("0");

        }
        productImages.setColor("#fafafa");

        productImagesArrayList.add(productImages);
        productImages = new ProductImages();
        productImages.setPlaceholder(R.drawable.placeholder);
        if (productImagesArrayList.size() == 0) {
            productImages.setStatus("1");
        } else {
            productImages.setStatus("0");

        }
        productImages.setColor("#fafafa");

        productImagesArrayList.add(productImages);*/
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
        /*spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    currency = arrayListCountries.get(position).getString("currency");
                    APPHelper.showToast(getApplicationContext(), currency);
                    spinnerCurrency.setSelection(adapterCurrency.getPosition(currency));
                    //editTextPrice.setHint("Price (" + currency + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        imagesAdapter.notifyDataSetChanged();

        imagesAdapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
            @Override
            public void onClick(int position) {

                productImagesArrayList.remove(position);
                jsonObjectArrayList.remove(position);
                imagesAdapter.notifyDataSetChanged();
            }
        });

        imagesAdapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
            @Override
            public void onClick(int position) {
               /* productImagesArrayList.remove(position);
                jsonObjectArrayList.remove(position);
                imagesAdapter.notifyDataSetChanged();*/
            }
        });
        editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (InputValidator.isValidPrice(s.toString())) {
                    BigDecimal price = BigDecimal.valueOf(Double.valueOf(s.toString()));
                    //Toast.makeText(AddProductActivity.this, "" + Double.parseDouble(s.toString()), Toast.LENGTH_SHORT).show();
                    Log.i("Price", String.valueOf(Double.parseDouble(s.toString())));
                    Log.i("Charge", String.valueOf(Double.parseDouble(s.toString()) * charges));

                    textViewPrice.setText("Platform fee for listing: " + formatter.format(price.multiply(BigDecimal.valueOf(charges))));
                } else
                    textViewPrice.setText("Platform fee for listing: " + formatter.format(0.0));

            }
        });
        findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setType("image/*");
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);*/
                showPopUp(v);
            }
        });
        findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal + "regulation.htm");
                intent.putExtra("title", "User Regulations");
                startActivity(intent);
            }
        });
        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tags = editTextTags.getText().toString();
                String pName = editTextProduct.getText().toString().trim();
                String pDesc = editTextPDesc.getText().toString().trim();
                String sku = spinner.getSelectedItem().toString();
                String qty = String.valueOf(numberPicker.getValue());

                price = editTextPrice.getText().toString();

            //    Toast.makeText(AddProductActivity.this, price, Toast.LENGTH_SHORT).show();

                String[] separated = price.split("-");
                currency = separated[0];
                price = separated[1];

              //  Toast.makeText(AddProductActivity.this,  currency+ " " +price , Toast.LENGTH_SHORT).show();


                        price = price + ".00";
               /* try {
                    currency = arrayListCountries.get(spinnerCurrency.getSelectedItemPosition()).getString("currency");
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                if (pName.equals("") || pDesc.equals("") || sku.contains("Select") || qty.equals("") || price.equals("")/* || tags.equals("")*/) {
                    Toast.makeText(AddProductActivity.this, "Fill All Fields", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(qty) < 1) {
                    APPHelper.showToast(AddProductActivity.this, "Enter Quantity");
                } else if (!InputValidator.isValidPrice(price)) {
                    APPHelper.showToast(AddProductActivity.this, "Enter Price");
                } else if (productImagesArrayList.size() == 0) {
                    APPHelper.showToast(AddProductActivity.this, "Upload Image(s)");
                } else if (!checkBoxAgree.isChecked()) {
                    APPHelper.showToast(AddProductActivity.this, "Agree to the terms & Conditions");
                } else {
                    JSONArray array = new JSONArray();

                    for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                        array.put(jsonObjectArrayList.get(i));
                    }

                    jsonArray = array.toString();
                    APPHelper.showLog("Images", jsonArray);
                    addProduct(userId, pName, pDesc, sku, qty, price);
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
                    JSONObject object1 = new JSONObject(result);
                    String charges1 = object1.getString("commission_value");
                    charges = Double.parseDouble(charges1) / 100;
                    Log.i("Charges", "" + charges);
                    String html = "By Proceeding to the next step, I agree to the <a href='file:///android_asset/regulation.htm'><font color=\"FF0000\"> User Terms & Conditions</font></a>. I also, agree NOT to share my personal phone number, email address and or any other term of communication to any user in COASAPP. Doing so, may result in suspension of my account or being removed from the COASAPP platform.";

                    if (charges == 0) {
                        textViewPrice.setVisibility(View.GONE);
                    }
                    ((TextView) findViewById(R.id.textViewTerms)).setText(Html.fromHtml(html));
                    ((TextView) findViewById(R.id.textViewCommission)).setVisibility(View.VISIBLE);
                    String text = "As a seller you are responsible to pay taxes to your city/state’s proper authorities. ";
                    if (charges > 0) {
                        text += "Platform fee is " + charges1 + "% (of listing price) you MUST pay in order for your item(s) to appear in shopping center and after our approval.";
                    }
                    ((TextView) findViewById(R.id.textViewCommission)).setText(text);
                    //((TextView)findViewById(R.id.textViewTerms)).setMovementMethod(LinkMovementMethod.getInstance());
                    //((TextView)findViewById(R.id.textViewTerms)).setText("By Proceeding to the next step, I agree to the User Terms & Conditions. I also, agree NOT to share my personal phone number, email address and or any other term of communication to any user in COASAPP. Doing so, may result in suspension of my account or being removed from the COASAPP platform. As a seller you are responsible to pay taxes to your city/state’s proper authorities. Platform fee is 6% (of listing price) you MUST pay in order for your item(s) to appear in shopping center and after our approval.");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                layoutProgress.setVisibility(View.GONE);
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("type", "Shopping");
        new GetCommission(chargeAsyncCallbacks, map).execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            /*boolean granted2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;*/
            if (!granted) {
                showAlert("You need to allow storage permissions to add product images");
            }
        }

    }


    public void showAlert(String errorMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddProductActivity.this);
        alertDialogBuilder.setMessage(errorMsg);

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ActivityCompat.requestPermissions(AddProductActivity.this, permissions, 99);
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

        }

        @Override
        protected String doInBackground(String... strings) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_brands.php?cat_id=" + getIntent().getStringExtra("sub_cat_id"));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArrayBrands = new JSONArray(s);
                JSONObject object = new JSONObject();
                object.put("brand_id", "0");
                object.put("brand_name", "Not Branded");
                object.put("brand_cat_id", getIntent().getStringExtra("sub_cat_id"));
                arrayListBrandsSpinner.add("Not Branded");
                arrayListBrands.add(object);

                for (int i = 0; i < jsonArrayBrands.length(); i++) {
                    object = jsonArrayBrands.getJSONObject(i);
                    arrayListBrands.add(object);
                    arrayListBrandsSpinner.add(object.getString("brand_name"));
                }


                ArrayAdapter<String> adapterBrands = new ArrayAdapter<>(
                        AddProductActivity.this,
                        android.R.layout.simple_spinner_item,
                        arrayListBrandsSpinner
                );
                adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBrand.setAdapter(adapterBrands);
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
                /*Cursor cursor = getContentResolver().query(mImageUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);*/
                String imageEncoded = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                ProductImages productImages = new ProductImages();
                productImages.setImage(imageEncoded);
                productImages.setStatus("0");
                productImages.setColor("#ff000000");
                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                int index = productImagesArrayList.size() - 1;
                new UploadBill(index).execute(index);
            } else if (requestCode == 0) {
                String imageEncoded = ResizeImage.getResizedImage(img1.getPath());
                ProductImages productImages = new ProductImages();
                productImages.setImage(imageEncoded);
                productImages.setStatus("0");
                productImages.setColor("#ff000000");

                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                int index = productImagesArrayList.size() - 1;
                new UploadBill(index).execute(index);
            } else if (requestCode == 2) {
                address1 = data.getStringExtra("address");
                lat = data.getDoubleExtra("latitude", 0);
                lng = data.getDoubleExtra("longitude", 0);

                List<Address> addresses = null;
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

                try {
                    addresses = geocoder.getFromLocation(
                            lat, lng,
                            // In this sample, get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e("Location", ioException.getMessage());
                }
                if (addresses == null || addresses.size() == 0) {

                    state = "";
                    city = "";

                } else {

                    Address address = addresses.get(0);

                    ArrayList<String> addressFragments = new ArrayList<String>();

                    state = address.getAdminArea();
                    city = address.getLocality();
                    country = address.getCountryName();
                    editTextCity.setText(city);
                    editTextState.setText(state);
                    spinnerCountry.setSelection(adapterCountries.getPosition(country));
                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.


                }


            } else if (requestCode == 100) {
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //APPHelper.showToast(getContext(), "Paid");
                try {
                    JSONObject object = new JSONObject(data.getStringExtra("charge"));
                    String txnId = object.getString("id");
                    String balanceTxn = object.getString("balance_transaction");
                    int amt = object.getInt("amount") / 100;
                    long created = object.getLong("created");
                    String desc = object.getString("description");
                    JSONObject objectSource = object.getJSONObject("source");
                    Date date4 = new java.util.Date(created * 1000L);
                    String dateCreated = sdf.format(date4);
                    new UpdatePayment().execute(txnId, balanceTxn, objectSource.toString(), dateCreated, String.valueOf(amt));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    class UploadBill extends AsyncTask<Integer, Integer, String> {
        int i;

        public UploadBill(int index) {
            this.i = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            i = params[0];
            String url = MAIN_URL + "upload_product_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("status", "0");
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
                    JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    if (i == 0) {
                        object.put("status", 1);

                    } else {
                        object.put("status", 0);

                    }
                    productImagesArrayList.get(i).setImage(jsonObject.getString("response"));
                    productImagesArrayList.get(i).setSource("url");

                    jsonObjectArrayList.add(object);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Failed Please try again", Toast.LENGTH_SHORT).show();
                productImagesArrayList.remove(i);
                imagesAdapter.notifyDataSetChanged();
                e.printStackTrace();
            }

        }
    }

    void addProduct(final String userId, final String name, final String desc, final String cond, final String count, final String price) {


        class AddProduct extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", userId);
                map.put("product_name", name);
                map.put("desc", desc);
                map.put("count", count);
                map.put("condition", cond);
                map.put("price", price);
                map.put("address", address1);
                map.put("city", city);
                map.put("state", state);
                map.put("country", country);
                map.put("currency", currency);
                map.put("brand", brandId);
                map.put("tags", tags);
                map.put("lat", String.valueOf(lat));
                map.put("lng", String.valueOf(lng));
                map.put("images", jsonArray);
                map.put("sub_cat", getIntent().getStringExtra("sub_cat_id"));
                map.put("category", getIntent().getStringExtra("cat_id"));
                return new RequestHandler().sendPostRequest(MAIN_URL + "add_product.php", map);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                layoutProgress.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("response_code").equals("1")) {
                        /*String productId = jsonObject.getString("p_id");
                        addProductImages(productId);*/
                        orderId = jsonObject.getString("pro_id");
                        Toast.makeText(AddProductActivity.this, "Item Listed Successfully", Toast.LENGTH_SHORT).show();
                        if (charges == 0) {
                        //    Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                            double tot1 = Math.round(Double.valueOf(price) * 100.0) / 100.0;
                            intent.putExtra("amount", String.valueOf(Math.round(tot1 * charges * 100)));
                            intent.putExtra("desc", "Shopping" + "_" + orderId);
                            startActivityForResult(intent, 100);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        new AddProduct().execute();
    }


    class UpdatePayment extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("txn_id", strings[0]);
            map.put("txn_date", strings[3]);
            map.put("source", strings[2]);
            map.put("txn_balance", strings[1]);
            map.put("amount", strings[4]);
            map.put("user_id", getSharedPreferences(APP_PREF, 0).getString("userId", "0"));
            map.put("order_id", orderId);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_pro_payment.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);

                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                }
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

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(AddProductActivity.this, v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:
                        try {
                            Intent pictureIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            img1 = GetPath.createImageFile(AddProductActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(AddProductActivity.this, APPHelper.photoProvider(AddProductActivity.this), img1);
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
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        //intent.setAction(Intent.ACTION_GET_CONTENT);
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
