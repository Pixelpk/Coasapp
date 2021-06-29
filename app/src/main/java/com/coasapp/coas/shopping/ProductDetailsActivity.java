package com.coasapp.coas.shopping;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.bargain.VehicleDetailsActivity;
import com.coasapp.coas.general.FullScreenImageSlide;
import com.coasapp.coas.roombook.BookingDetailsActivity;
import com.coasapp.coas.roombook.ImagesAdapter;

import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;


import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.ChatMessageActivity;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;

public class ProductDetailsActivity extends AppCompatActivity implements APPConstants {

    ArrayList<ProductImages> imagesArrayList = new ArrayList<>();
    ArrayList<String> arrayListImages = new ArrayList<>();
    ProductImagesAdapter imagesAdapter;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    TextView textViewCart;
    ContentValues contentValues;
    int qty, addQty, newQty, stock;
    String proId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_product_details);
        Toolbar toolbar = findViewById(R.id.toolbarProductDetails);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(this);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        //sqLiteDatabase.delete("cart", null, null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");
        textViewCart = findViewById(R.id.textViewCart);
        TextView textViewProduct = findViewById(R.id.textViewProduct);
        TextView textViewSeller = findViewById(R.id.textViewSeller);
        TextView textViewDesc = findViewById(R.id.textViewDesc);
        final TextView textViewPrice = findViewById(R.id.textViewPrice);
        CircleImageView imageView = findViewById(R.id.imageViewSeller);
        final ImageView imageViewPro = findViewById(R.id.imageViewProduct);
        final Spinner spinner = findViewById(R.id.spinnerQty);
        RecyclerView recyclerViewImagesThumb = findViewById(R.id.recyclerViewImages);
        RecyclerView recyclerViewImagesL = findViewById(R.id.recyclerViewImagesLarge);
        final CheckBox checkBoxCart = findViewById(R.id.checkBoxAdd);
        ImagesAdapter imagesAdapterLarge = new ImagesAdapter(arrayListImages, this, getApplicationContext());
        recyclerViewImagesL.setAdapter(imagesAdapterLarge);
        recyclerViewImagesThumb.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new ProductImagesAdapter(imagesArrayList, this, getApplicationContext());
        recyclerViewImagesThumb.setAdapter(imagesAdapter);
        imagesAdapterLarge.setOnItemClick(new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getApplicationContext(), FullScreenImageSlide.class);
                intent.putExtra("position", position);
                intent.putExtra("images", arrayListImages);
                startActivity(intent);
            }
        });
        String details = getIntent().getStringExtra("details");
      //  Toast.makeText(this, details, Toast.LENGTH_SHORT).show();
        try {
            final JSONObject jsonObject = new JSONObject(details);
            if (Integer.valueOf(jsonObject.getString("count")) > 0) {
                ((TextView) findViewById(R.id.textViewAvailable)).setText("Available");

            } else {
                ((TextView) findViewById(R.id.textViewAvailable)).setText("Out of Stock");


            }

           // ((TextView) findViewById(R.id.textViewAddress)).setText(jsonObject.getString("pro_city")+", "+jsonObject.getString("pro_country"));
            ((TextView) findViewById(R.id.textViewAddress)).setText(jsonObject.getString("pro_city")+", "+jsonObject.getString("pro_state"));
            proId = jsonObject.getString("productid");
            stock = Integer.parseInt(jsonObject.getString("count"));

           //x Toast.makeText(this, jsonObject.getString("pro_city")+", "+jsonObject.getString("pro_country"), Toast.LENGTH_SHORT).show();
           /* imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                    try {
                        intent.putExtra("takeOrder", true);
                        intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                        intent.putExtra(ConversationUIService.USER_ID, jsonObject.getString("coas_id"));
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });*/
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*try {

                            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                            intent.putExtra("takeOrder", true);
                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            intent.putExtra(ConversationUIService.USER_ID, jsonObject.getString("coas_id"));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                    try {
                        if (!jsonObject.getString("user_id").equalsIgnoreCase(sharedPreferences.getString("userId", "0"))) {
                            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                            new LaunchChatUtils(getApplicationContext(), ProductDetailsActivity.this, new LaunchChatCallbacks() {
                                @Override
                                public void onChatCreatedSuccess(Intent intent) {
                                    intent.putExtra("from","shopping");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                            LaunchChatUtils.launchChatMessageActivity(ProductDetailsActivity.this,intent);

                                        }
                                    });
                                }

                                @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                        }
                                    });

                                }
                            }).createChatDialog(jsonObject.getString("coas_id"));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            textViewSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*try {
                        if (!jsonObject.getString("user_id").equalsIgnoreCase(sharedPreferences.getString("userId", "0"))) {
                            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                            intent.putExtra("takeOrder", true);
                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                            intent.putExtra(ConversationUIService.USER_ID, jsonObject.getString("coas_id"));
                            startActivity(intent);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }
            });

            textViewProduct.setText(jsonObject.getString("pro_name"));
            textViewSeller.setText(jsonObject.getString("name"));
            if (jsonObject.getString("user_id").equalsIgnoreCase(sharedPreferences.getString("userId", "0"))) {
                textViewSeller.setText("You");

            }
            textViewPrice.setText(jsonObject.getString("pro_currency") + " " + jsonObject.getString("price"));
            Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + jsonObject.getString("image")).into(imageView);
            ArrayList<Integer> stock = new ArrayList<>();
            /*if (Integer.parseInt(jsonObject.getString("count")) >= 10) {
                for (int i = 1; i <= 10; i++) {
                    stock.add(i);
                }
            } else {
                for (int i = 1; i <= Integer.parseInt(jsonObject.getString("count")); i++) {
                    stock.add(i);
                }
            }*/
            for (int i = 1; i <= 20; i++) {
                stock.add(i);
            }
            ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_qty, stock);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            textViewDesc.setText(Html.fromHtml(jsonObject.getString("pro_descs")));
            JSONArray jsonArrayImages = new JSONArray(jsonObject.getString("images"));
            for (int i = 0; i < jsonArrayImages.length(); i++) {
                JSONObject object = jsonArrayImages.getJSONObject(i);
                ProductImages productImages = new ProductImages();
                productImages.setImage(object.getString("image"));
                productImages.setSelected(false);
                imagesArrayList.add(productImages);
                arrayListImages.add(MAIN_URL_IMAGE + object.getString("image"));
            }
            imagesAdapterLarge.notifyDataSetChanged();
            imagesArrayList.get(0).setSelected(true);
            Glide.with(getApplicationContext()).load(imagesArrayList.get(0).getImage()).into(imageViewPro);
            imagesAdapter.notifyDataSetChanged();
            imagesAdapter.setOnImageSelected(new ProductImagesAdapter.OnImageSelected() {
                @Override
                public void onImageSelected(int position) {
                    for (int i = 0; i < imagesArrayList.size(); i++) {
                        imagesArrayList.get(i).setSelected(false);
                    }
                    imagesArrayList.get(position).setSelected(true);
                    imagesAdapter.notifyDataSetChanged();
                    recyclerViewImagesL.scrollToPosition(position);
                    APPHelper.showLog("Image", imagesArrayList.get(position).getImage());
                    Glide.with(getApplicationContext()).load(imagesArrayList.get(position).getImage()).into(imageViewPro);

                }

            });
            recyclerViewImagesL.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerViewImagesL.getLayoutManager());
                    int pos = layoutManager.findFirstVisibleItemPosition();
                    for (int i = 0; i < imagesArrayList.size(); i++) {
                        imagesArrayList.get(i).setSelected(false);
                    }
                    imagesArrayList.get(pos).setSelected(true);
                    imagesAdapter.notifyDataSetChanged();
                }
            });
            final String sql = "select * from cart where pro_id = ?";
            /*Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{jsonObject.getString("productid")});
            APPHelper.showLog("Pro", sql + " " + String.valueOf(cursor.getCount()) + " " + jsonObject.getString("productid"));
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (Integer.valueOf(jsonObject.getString("count")) < cursor.getInt(cursor.getColumnIndex("qty"))) {
                        spinner.setSelection(Integer.valueOf(jsonObject.getString("count")) - 1);

                    } else if (Integer.parseInt(jsonObject.getString("count")) == 0) {
                        int del = sqLiteDatabase.delete("cart", "pro_id = ?", new String[]{jsonObject.getString("productid")});
                    } else {
                        spinner.setSelection(cursor.getInt(cursor.getColumnIndex("qty")) - 1);
                    }
                    checkBoxCart.setChecked(true);
                    textViewPrice.setText("$  " + cursor.getDouble(cursor.getColumnIndex("points")));
                }
            }
            cursor.close();*/
            checkBoxCart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        if (isChecked) {
                            if (Integer.parseInt(jsonObject.getString("count")) < spinner.getSelectedItemPosition() + 1) {
                                APPHelper.showToast(getApplicationContext(), "Only " + Integer.parseInt(jsonObject.getString("count")) + " in stock.");
                                checkBoxCart.setChecked(false);
                            } else {
                                Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{jsonObject.getString("productid")});
                                APPHelper.showLog("Pro", sql + " " + String.valueOf(cursor.getCount()));
                                if (cursor.getCount() > 0) {
                                    int del = sqLiteDatabase.delete("cart", "pro_id = ?", new String[]{jsonObject.getString("productid")});
                                    APPHelper.showLog("ProDel", "" + del + " " + jsonObject.getString("productid"));
                                }
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("pro_id", jsonObject.getString("productid"));
                                contentValues.put("pro_name", jsonObject.getString("pro_name"));
                                contentValues.put("pro_image", imagesArrayList.get(0).getImage());
                                contentValues.put("qty", spinner.getSelectedItemPosition() + 1);
                                contentValues.put("merchant", jsonObject.getString("pro_user_id"));
                                contentValues.put("merchant_name", jsonObject.getString("name"));
                                contentValues.put("points", (spinner.getSelectedItemPosition() + 1) * Double.parseDouble(jsonObject.getString("price")));
                                long ins = sqLiteDatabase.insert("cart", null, contentValues);
                                cursor.close();
                                APPHelper.showLog("ProIns", "" + ins);
                                textViewPrice.setText("$" + (spinner.getSelectedItemPosition() + 1) * Double.parseDouble(jsonObject.getString("price")));
                            }

                        } else {
                            int del = sqLiteDatabase.delete("cart", "pro_id = ?", new String[]{jsonObject.getString("productid")});
                            APPHelper.showLog("ProDel", "" + del + " " + jsonObject.getString("productid"));

                        }
                        APPHelper.exportDB();
                    } catch (JSONException e) {

                    }
                    updateCart();
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
            /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (checkBoxCart.isChecked()) {
                            if (Integer.parseInt(jsonObject.getString("count")) < spinner.getSelectedItemPosition() + 1) {
                                APPHelper.showToast(getApplicationContext(), "Only " + Integer.parseInt(jsonObject.getString("count")) + " in stock.");
                                checkBoxCart.setChecked(false);
                            } else {
                                updateCart();
                                //onSpinnerItemSelected.onItemSelected(getAdapterPosition(), spinner.getSelectedItemPosition()+1);
                                Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{jsonObject.getString("productid")});
                                APPHelper.showLog("Pro", sql + " " + String.valueOf(cursor.getCount()));
                                if (cursor.getCount() > 0) {
                                    int del = sqLiteDatabase.delete("cart", "pro_id = " + jsonObject.getString("productid"), null);
                                }
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("pro_id", jsonObject.getString("productid"));
                                contentValues.put("pro_name", jsonObject.getString("pro_name"));
                                contentValues.put("pro_image", imagesArrayList.get(0).getImage());
                                contentValues.put("qty", spinner.getSelectedItemPosition() + 1);
                                contentValues.put("merchant", jsonObject.getString("pro_user_id"));
                                contentValues.put("merchant_name", jsonObject.getString("name"));
                                contentValues.put("points", (spinner.getSelectedItemPosition() + 1) * Double.parseDouble(jsonObject.getString("price")));
                                long ins = sqLiteDatabase.insert("cart", null, contentValues);

                                textViewPrice.setText("$" + (spinner.getSelectedItemPosition() + 1) * Double.parseDouble(jsonObject.getString("price")));

                                cursor.close();
                                updateCart();
                            }


                        }
                        textViewPrice.setText("$" + (spinner.getSelectedItemPosition() + 1) * Double.parseDouble(jsonObject.getString("price")));

                        updateCart();
                    } catch (JSONException e) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/
            findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    qty = spinner.getSelectedItemPosition() + 1;
                    new AddCart().execute();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCart();
        new GetCart().execute();
    }

    protected void exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/" + "com.mobicomkit.coas" + "/databases/coasapp.db";
        String backupDBPath = "coas.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Log.d("Database", "DB Exported");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Database", e.getMessage());
            String packageName = "com.myapp.esplus";

        }
    }

    void updateCart() {
        String sql = "select * from cart";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        textViewCart.setText("" + cursor.getCount());
        cursor.close();
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
                /*if (jsonArray.length() == 0) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class CheckCount extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = MAIN_URL + "get_product_stock.php?pro_id=" + proId;
            return new RequestHandler().sendGetRequest(url);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                int qty2 = Integer.parseInt(object.getString("count"));
                if (qty2 < qty) {
                                   /* double unitPrice = Double.valueOf(map.get("amount")) / Integer.parseInt(map.get("qty"));
                                    map.put("qty", String.valueOf(qty2));
                                    map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));*/

                    Toast.makeText(getApplicationContext(), "Only " + qty2 + " available", Toast.LENGTH_SHORT).show();

                } else {

                    new AddCart().execute();

                    /*map.put("qty", String.valueOf(qty + 1));
                    double unitPrice = Double.valueOf(map.get("amount")) / qty;
                    map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("points", Integer.valueOf(map.get("qty")) * unitPrice);
                    contentValues.put("qty", Integer.valueOf(map.get("qty")));
                    sqLiteDatabase.update("cart", contentValues, "pro_id=?", new String[]{pId});*/

                }
                                /*total = total + Double.valueOf(map.get("amount"));

                                newPoint = point - total;
                                checkoutAdapter.notifyDataSetChanged();
                                textViewTotal.setText("$" + total);*/

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class AddCart extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("qty", String.valueOf(qty));
            map.put("pro_id", proId);
            map.put("user_id", getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "add_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    new GetCart().execute();

                }
                APPHelper.showToast(getApplicationContext(), object.getString("message"));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bundle bundle = new Bundle();sqLiteDatabase.close();

    }
}
