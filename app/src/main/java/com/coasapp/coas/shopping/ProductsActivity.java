package com.coasapp.coas.shopping;

import androidx.appcompat.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProductsActivity extends AppCompatActivity implements APPConstants {


    ProductsAdapter productsAdapter;
    ArrayList<JSONObject> productsArrayList1 = new ArrayList<>();
    String filters = "";
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    ContentValues contentValues;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView textViewCart;
    int start;
    String condition = "";
    FrameLayout layoutMore;
    boolean filter = false;
    LinearLayout shopFilter;
    double lat, lng, minPrice = 0.0, maxPrice = 0.0, distance = 0.0, minPrice2 = 0.0, maxPrice2 = 5000.00;
    int step1, step2, step3, min1, min2, min3, max1, max2, max3, step4, step5, min4, max4, min5, max5, seek1, seek2, seek3, seek4, seek5, spinnerPos;
    String sort = "latest", pricefilter = "", searchfilter = "", conditionFilter = "", distanceFilter = "", finalfilter = "", carFilter = "", userId, sortFilter = "", brandFilter = "";
    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    ArrayList<JSONObject> arrayListBrands = new ArrayList<>();
    List<String> arrayListBrandsSpinner = new ArrayList<>();
    String brandId = "0", search = "", catId = "0", subCat = "0";
    SeekBar seekBarDistance, seekBarMinPrice, seekBarMaxPrice;
    TextView textViewDistance, textViewMinPrice, textViewMaxPrice;
    Spinner spinnerSort, spinnerCondition, spinnerBrand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        step1 = 1;
        max1 = 50;
        min1 = 0;

        step2 = 1;
        max2 = 400;
        min2 = 1;

        step3 = 10;
        min3 = 10;
        max3 = 50000;
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        swipeRefreshLayout = findViewById(R.id.swipe);
        databaseHandler = new DatabaseHandler(this);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbarProducts));
        layoutMore = findViewById(R.id.layoutMore);
        shopFilter = findViewById(R.id.shopFilter);
        search = getIntent().getStringExtra("search");
        shopFilter.setVisibility(View.GONE);
        if (!search.equals("")) {
            APPHelper.showToast(getApplicationContext(), search);
            //searchfilter = " and pro_name like '%" + search + "%' or pro_city like '%" + search + "%' or pro_city like '%" + search + "%' or brand_name like '%" + search + "%'";
        }
        catId = getIntent().getStringExtra("cat_id");
        subCat = getIntent().getStringExtra("sub_cat_id");
        if (!catId.equals("0")) {
            //carFilter = " and (category = " + catId + " or parent = " + catId + ") ";
        }
        //finalfilter = pricefilter + conditionFilter + brandFilter + searchfilter + carFilter + sortFilter;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("cat"));
        textViewCart = findViewById(R.id.textViewCart);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewProducts);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        productsAdapter = new ProductsAdapter(productsArrayList1, ProductsActivity.this, getApplicationContext());
        recyclerView.setAdapter(productsAdapter);
        //new GetBrands().execute();
        new ViewProducts().execute();

        productsAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                start = 0;
                swipeRefreshLayout.setRefreshing(false);
                productsArrayList1.clear();
                productsAdapter.notifyDataSetChanged();
                new ViewProducts().execute();
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {



            }

            @Override
            public void afterTextChanged(Editable s) {
                productsArrayList1.clear();
                productsAdapter.notifyDataSetChanged();
                search = s.toString();
                start = 0;
                /*for (int i = 0; i < productsArrayList.size(); i++) {
                    JSONObject jsonObject = productsArrayList.get(i);
                    try {
                        if (jsonObject.getString("pro_name").toLowerCase().contains(search) || jsonObject.getString("pro_tags").toLowerCase().contains(search)) {
                            productsArrayList1.add(jsonObject);
                        }
                        productsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                new ViewProducts().execute();
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

        findViewById(R.id.floatingActionButtonFilter).setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if (!filter) {
                    minPrice = 1;
                    maxPrice = 10;
                }
                step1 = 1;
                max1 = 50;
                min1 = 0;

                step2 = 1;
                max2 = 400;
                min2 = 1;

                step3 = 10;
                min3 = 10;
                max3 = 50000;
                ArrayAdapter<String> adapterBrands = new ArrayAdapter<String>(
                        ProductsActivity.this,
                        android.R.layout.simple_spinner_item,
                        arrayListBrandsSpinner
                );
                adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBrand.setAdapter(adapterBrands);
                shopFilter.setVisibility(View.VISIBLE);

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLastItemDisplaying(recyclerView)) {
                    APPHelper.showLog("Scroll", "scrolled");
                    //Calling the method getdata again
                    //getData();

                    new ViewProducts().execute();

                }
            }
        });


        seekBarDistance = findViewById(R.id.seekBarDistance);
        seekBarMinPrice = findViewById(R.id.seekBarMinPrice);
        seekBarMaxPrice = findViewById(R.id.seekBarMaxPrice);
        textViewDistance = findViewById(R.id.textViewDistance);
        textViewMinPrice = findViewById(R.id.textViewMinPrice);
        textViewMaxPrice = findViewById(R.id.textViewMaxPrice);
        Button buttonApply = findViewById(R.id.buttonApply);
        Button buttonClear = findViewById(R.id.buttonClear);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerBrand = findViewById(R.id.spinnerBrand);
        spinnerCondition = findViewById(R.id.spinnerCondition);

        seekBarDistance.setMax(((max1 - min1) / step1));
        seekBarMinPrice.setMax(((max2 - min2) / step2));
        seekBarMaxPrice.setMax(((max3 - min3) / step3));
        if (filter) {
            textViewMaxPrice.setText(formatter.format(maxPrice));

        } else {
            seekBarDistance.setProgress(0);
            textViewMaxPrice.setText(formatter.format(10.0));
        }
        textViewMinPrice.setText(formatter.format(minPrice));
        seekBarDistance.setProgress(seek5);
        seekBarMinPrice.setProgress(seek1);
        seekBarMaxPrice.setProgress(seek2);
        if (distance > 0) {
            textViewDistance.setText(distance + " miles");
        } else {
            textViewDistance.setText("Any Distance");
        }

        if (condition.equals("New")) {
            spinnerCondition.setSelection(1);
        } else if (condition.equals("Used")) {
            spinnerCondition.setSelection(2);
        }
        if (sort.contains("asc")) {
            spinnerSort.setSelection(1);
        } else if (sort.contains("desc")) {
            spinnerSort.setSelection(2);
        }
        if (!brandId.equals("0")) {
            for (int i = 0; i < arrayListBrands.size(); i++) {
                try {
                    if (arrayListBrands.get(i).getString("brand_id").equals(brandId)) {
                        spinnerBrand.setSelection(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    JSONObject object = arrayListBrands.get(position);
                    try {
                        brandId = object.getString("brand_id");
                        brandFilter = " and pro_brand = " + brandId + " ";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    brandFilter = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seekBarMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minPrice = (min2 + (progress * step2));
                textViewMinPrice.setText(numberFormat.format(minPrice));
                //textViewMaxPrice.setText(numberFormat.format(minPrice));
                seek1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMaxPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek2 = progress;
                maxPrice = (min3 + (progress * step3));
                textViewMaxPrice.setText(numberFormat.format(maxPrice));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = min1 + (progress * step1);

                if (distance > 0) {
                    textViewDistance.setText(distance + " miles");
                } else {
                    textViewDistance.setText("Any Distance");
                }
                seek5 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //APPHelper.showLog("spinner", String.valueOf(spinnerPkg.getSelectedItemPosition()));

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if (maxPrice == 0 && minPrice == 0) {
                    pricefilter = "";
                } else {
                    pricefilter = " and price between " + minPrice + " and " + maxPrice;
                }*/
                if (minPrice > maxPrice) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Price Range");
                } else {
                    shopFilter.setVisibility(View.GONE);
                    filter = true;
                    switch (spinnerCondition.getSelectedItemPosition()) {

                        case 0:
                            condition = "";
                            conditionFilter = "";
                            break;

                        case 1:
                            conditionFilter = " and pro_condition = 'New' ";
                            condition = "New";
                            break;

                        case 2:
                            condition = "Used";
                            conditionFilter = " and pro_condition = 'Used' ";
                            break;

                    }

                    switch (spinnerSort.getSelectedItemPosition()) {

                        case 0:
                            sortFilter = " order by productid desc";
                            sort = "latest";
                            break;

                        case 1:
                            sortFilter = " order by price asc";
                            sort = "priceasc";
                            break;

                        case 2:
                            sortFilter = " order by price desc";
                            sort = "pricedesc";
                            break;

                    }

                    finalfilter = pricefilter + conditionFilter + brandFilter + searchfilter + carFilter + sortFilter;
                    start = 0;
                    //new GetRooms().execute();
                    productsArrayList1.clear();
                    productsAdapter.notifyDataSetChanged();
                    APPHelper.showLog("Filter", finalfilter);
                    new ViewProducts().execute();

                }
            }


        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
                productsArrayList1.clear();
                productsAdapter.notifyDataSetChanged();
                new ViewProducts().execute();
            }
        });
    }


    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }

    void showFilterDialog() {

        step1 = 1;
        max1 = 50;
        min1 = 0;

        step2 = 1;
        max2 = 400;
        min2 = 1;

        step3 = 10;
        min3 = 10;
        max3 = 50000;


        LayoutInflater li = LayoutInflater.from(ProductsActivity.this);
        //Creating a view to get the dialog box
        View viewFilter = li.inflate(R.layout.shopping_filter, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductsActivity.this);

        //Adding our dialog box to the view of alert dialog
        builder.setView(viewFilter);


        //Creating an alert dialog
        final AlertDialog alertDialog = builder.create();
        //Displaying the alert dialog
        alertDialog.show();

        SeekBar seekBarDistance = viewFilter.findViewById(R.id.seekBarDistance);
        final SeekBar seekBarMinPrice = viewFilter.findViewById(R.id.seekBarMinPrice);
        final SeekBar seekBarMaxPrice = viewFilter.findViewById(R.id.seekBarMaxPrice);
        final TextView textViewDistance = viewFilter.findViewById(R.id.textViewDistance);
        final TextView textViewMinPrice = viewFilter.findViewById(R.id.textViewMinPrice);
        final TextView textViewMaxPrice = viewFilter.findViewById(R.id.textViewMaxPrice);
        Button buttonApply = viewFilter.findViewById(R.id.buttonApply);
        Button buttonClear = viewFilter.findViewById(R.id.buttonClear);
        final Spinner spinnerSort = viewFilter.findViewById(R.id.spinnerSort);
        final Spinner spinnerBrand = viewFilter.findViewById(R.id.spinnerBrand);
        final Spinner spinnerCondition = viewFilter.findViewById(R.id.spinnerCondition);
        ArrayAdapter<String> adapterBrands = new ArrayAdapter<String>(
                ProductsActivity.this,
                android.R.layout.simple_spinner_item,
                arrayListBrandsSpinner
        );
        adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrand.setAdapter(adapterBrands);
        seekBarDistance.setMax(((max1 - min1) / step1));
        seekBarMinPrice.setMax(((max2 - min2) / step2));
        seekBarMaxPrice.setMax(((max3 - min3) / step3));
        if (filter) {
            textViewMaxPrice.setText(formatter.format(maxPrice));

        } else {
            seekBarDistance.setProgress(0);
            textViewMaxPrice.setText(formatter.format(10.0));
        }
        textViewMinPrice.setText(formatter.format(minPrice));
        seekBarDistance.setProgress(seek5);
        seekBarMinPrice.setProgress(seek1);
        seekBarMaxPrice.setProgress(seek2);
        if (distance > 0) {
            textViewDistance.setText(distance + " miles");
        } else {
            textViewDistance.setText("Any Distance");
        }

        if (condition.equals("New")) {
            spinnerCondition.setSelection(1);
        } else if (condition.equals("Used")) {
            spinnerCondition.setSelection(2);
        }
        if (sort.contains("asc")) {
            spinnerSort.setSelection(1);
        } else if (sort.contains("desc")) {
            spinnerSort.setSelection(2);
        }
        if (!brandId.equals("0")) {
            for (int i = 0; i < arrayListBrands.size(); i++) {
                try {
                    if (arrayListBrands.get(i).getString("brand_id").equals(brandId)) {
                        spinnerBrand.setSelection(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    JSONObject object = arrayListBrands.get(position);
                    try {
                        brandId = object.getString("brand_id");
                        brandFilter = " and pro_brand = " + brandId + " ";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    brandFilter = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seekBarMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minPrice = (min2 + (progress * step2));
                textViewMinPrice.setText(numberFormat.format(minPrice));
                //textViewMaxPrice.setText(numberFormat.format(minPrice));
                seek1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMaxPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek2 = progress;
                maxPrice = (min3 + (progress * step3)) ;
                textViewMaxPrice.setText(numberFormat.format(maxPrice));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = min1 + (progress * step1);

                if (distance > 0) {
                    textViewDistance.setText(distance + " miles");
                } else {
                    textViewDistance.setText("Any Distance");
                }
                seek5 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //APPHelper.showLog("spinner", String.valueOf(spinnerPkg.getSelectedItemPosition()));

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if (maxPrice == 0 && minPrice == 0) {
                    pricefilter = "";
                } else {
                    pricefilter = " and price between " + minPrice + " and " + maxPrice;
                }*/
                if (minPrice > maxPrice) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Price Range");
                } else {
                    filter = true;
                    switch (spinnerCondition.getSelectedItemPosition()) {
                        case 0:
                            condition = "";
                            conditionFilter = "";
                            break;
                        case 1:
                            conditionFilter = " and pro_condition = 'New' ";
                            condition = "New";
                            break;
                        case 2:
                            condition = "Used";
                            conditionFilter = " and pro_condition = 'Used' ";
                            break;
                    }
                    switch (spinnerSort.getSelectedItemPosition()) {
                        case 0:
                            sortFilter = " order by productid desc";
                            sort = "latest";
                            break;
                        case 1:
                            sortFilter = " order by price asc";
                            sort = "priceasc";
                            break;
                        case 2:
                            sortFilter = " order by price desc";
                            sort = "pricedesc";
                            break;
                    }
                    finalfilter = pricefilter + conditionFilter + brandFilter + searchfilter + carFilter + sortFilter;
                    alertDialog.dismiss();
                    start = 0;
                    //new GetRooms().execute();
                    productsArrayList1.clear();
                    productsAdapter.notifyDataSetChanged();
                    APPHelper.showLog("Filter", finalfilter);
                    new ViewProducts().execute();

                }
            }


        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = false;
                pricefilter = "";
                conditionFilter = "";
                finalfilter = "";

                alertDialog.dismiss();
                start = 0;
                distance = 0;
                brandId = "0";
                condition = "";
                minPrice = 1;
                maxPrice = 50000;
                sort = "latest";
                productsArrayList1.clear();
                productsAdapter.notifyDataSetChanged();
                new ViewProducts().execute();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetCart().execute();
    }

    class ViewProducts extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutMore.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            lat = Double.parseDouble(sharedPreferences.getString("lat", "0.0"));
            lng = Double.parseDouble(sharedPreferences.getString("lng", "0.0"));
            HashMap<String, String> map = new HashMap<>();
            map.put("cat_id", catId);
            map.put("sub_cat_id", subCat);
            map.put("brand_id", brandId);
            map.put("minprice", String.valueOf(minPrice));
            map.put("maxprice", String.valueOf(maxPrice));
            map.put("condition", condition);
            map.put("search", search);
            map.put("distance", String.valueOf(distance));
            //map.put("filters", finalfilter);
            map.put("sort", sort);
            map.put("index", String.valueOf(start));

            map.put("lat", String.valueOf(lat));
            map.put("lng", String.valueOf(lng));
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            APPHelper.showLog("pro", String.valueOf(map));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_products1.php", map);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutMore.setVisibility(View.GONE);
            arrayListBrandsSpinner.clear();
            arrayListBrands.clear();
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("products");
                JSONArray arrayBrands = jsonObject.getJSONArray("brands");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    /*if (distance > 0) {
                        double latitude = Double.parseDouble(object.getString("pro_lat"));
                        double longitude = Double.parseDouble(object.getString("pro_lng"));
                        double d = new FindDistance().distance(lat, lng, latitude, longitude);
                        APPHelper.showLog("dist", distance + " " + d + " " + latitude + " " + longitude + " " + lat + " " + lng);
                        if (d <= distance) {
                            productsArrayList.add(object);
                            productsArrayList1.add(object);
                        }
                    } else {
                        productsArrayList.add(object);
                        productsArrayList1.add(object);
                    }*/
                    productsArrayList1.add(object);
                }
                JSONObject object = new JSONObject();
                object.put("brand_id", "0");
                object.put("brand_name", "Any Brand");
                object.put("brand_cat_id", getIntent().getStringExtra("cat_id"));
                arrayListBrandsSpinner.add("Not Branded");
                arrayListBrands.add(object);
                for (int i = 0; i < arrayBrands.length(); i++) {
                    object = arrayBrands.getJSONObject(i);
                    arrayListBrands.add(object);
                    arrayListBrandsSpinner.add(object.getString("brand_name"));
                }
                start = productsArrayList1.size();
                productsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
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
            HashMap<String,String> map=new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //return new RequestHandler().sendGetRequest(MAIN_URL + "get_addresses.php?user_id=" + sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_addresses.php",map);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            APPHelper.showLog("add", s);
            try {
                JSONArray jsonArray = new JSONArray(s);
               /* if (jsonArray.length() == 0) {
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

    class GetBrands extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arrayListBrands.clear();
            arrayListBrandsSpinner.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_brands?cat_id=" + getIntent().getStringExtra("sub_cat_id"));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArrayBrands = new JSONArray(s);
                JSONObject object = new JSONObject();
                object.put("brand_id", "0");
                object.put("brand_name", "Any Brand");
                object.put("brand_cat_id", getIntent().getStringExtra("cat_id"));
                arrayListBrandsSpinner.add("Not Branded");
                arrayListBrands.add(object);
                for (int i = 0; i < jsonArrayBrands.length(); i++) {
                    object = jsonArrayBrands.getJSONObject(i);
                    arrayListBrands.add(object);
                    arrayListBrandsSpinner.add(object.getString("brand_name"));
                }
               /* ArrayAdapter<String> adapterBrands = new ArrayAdapter<String>(
                        AddProductActivity.this,
                        android.R.layout.simple_spinner_item,
                        arrayListBrandsSpinner
                );
                adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBrand.setAdapter(adapterBrands);*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    void resetFilter() {
        filter = false;
        pricefilter = "";
        conditionFilter = "";
        finalfilter = "";

        start = 0;
        distance = 0;
        brandId = "0";
        condition = "";
        minPrice = 0;
        maxPrice = 0;
        seekBarDistance.setProgress(0);
        seekBarMaxPrice.setProgress(0);
        seekBarMinPrice.setProgress(0);
        spinnerBrand.setSelection(0);
        spinnerCondition.setSelection(0);
        spinnerSort.setSelection(0);
        textViewMinPrice.setText(formatter.format(1));
        textViewMaxPrice.setText(formatter.format(10));
        sort = "latest";
        shopFilter.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        if (shopFilter.getVisibility() == View.VISIBLE) {
            shopFilter.setVisibility(View.GONE);
            //resetFilter();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
    }
}
