package com.coasapp.coas.shopping;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyProductsActivity extends AppCompatActivity implements APPConstants {

    ArrayList<JSONObject> productsArrayList = new ArrayList<>();
    ArrayList<JSONObject> productsArrayList1 = new ArrayList<>();
    MyProductsAdapter productsAdapter;
    int position = 0;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        productsAdapter = new MyProductsAdapter(productsArrayList, MyProductsActivity.this, getApplicationContext());
        recyclerViewProducts.setAdapter(productsAdapter);
        final String userId = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0");
        new MyProducts().execute(userId);
        ((EditText) findViewById(R.id.editTextSearch)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String search = s.toString();
                productsArrayList.clear();
                productsAdapter.notifyDataSetChanged();
                for (int i = 0; i < productsArrayList1.size(); i++) {

                    JSONObject object = productsArrayList1.get(i);

                    try {
                        APPHelper.showLog("Name", object.getString("pro_name"));
                        if (object.getString("pro_name").toLowerCase().contains(search)) {
                            productsArrayList.add(object);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                productsAdapter.notifyDataSetChanged();
            }
        });

        productsAdapter.setOnDelClick(new MyProductsAdapter.OnDelClick() {
            @Override
            public void onDelClick(int i) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                String id = null;
                                try {
                                    id = productsArrayList.get(i).getString("productid");
                                    new DeleteProduct(i).execute(id);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MyProductsActivity.this);
                builder.setMessage("Delete this Product?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });
       /* Products products = new Products();
        products.setProductId("0");
        products.setProduct("Moto G");
        products.setDesc("");
        products.setSku("32 GB, Black");
        products.setCount("1");
        products.setImages("http://www.santacruzmentor.org/wp-content/uploads/2012/12/Placeholder.png");
        products.setPrice("120.00");
        products.setMrp("130.00");
        products.setStatus("1");
        products.setChecked(false);
        productsArrayList.add(products);

        productsAdapter.notifyDataSetChanged();*/
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new MyProducts().execute(userId);

            }
        });
        findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SellerCategoriesActivity.class);
                intent.putExtra("mode", "add");
                startActivityForResult(intent, 99);
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 99) {
                productsArrayList.clear();
                productsAdapter.notifyDataSetChanged();
                String userId = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0");
                new MyProducts().execute(userId);
            }
        }
    }

    class DeleteProduct extends AsyncTask<String, Void, String> {

        int i;

        public DeleteProduct(int i) {
            this.i = i;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> map = new HashMap<>();
            map.put("productid", params[0]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "delete_product.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    productsArrayList.remove(i);
                    productsAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    class MyProducts extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            productsArrayList.clear();
            productsArrayList1.clear();
            productsAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", params[0]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "view_products.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("products");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    productsArrayList.add(object);
                    productsArrayList1.add(object);
                }
                productsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
