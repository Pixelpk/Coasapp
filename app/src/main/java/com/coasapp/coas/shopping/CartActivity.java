package com.coasapp.coas.shopping;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.coasapp.coas.Pleasepaycash;
import com.coasapp.coas.R;
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.payment.Payalpayment;
import com.coasapp.coas.payment.StripePaymentActivity;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements APPConstants {
    CheckoutAdapter checkoutAdapter;
    ArrayList<HashMap<String, String>> arrayListCheckout = new ArrayList<>();
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    double total, newPoint, point;
    LinearLayout linearLayoutProgress;
    TextView textViewTotal;
    String items = "[]";
    SharedPreferences sharedPreferences;
    String orderId = "0";
    String payment_type = "null";

    private static final int REQUEST_CODE = 1234;
    EditText amountET;
    Button checkoutBtn;

    static final String API_GET_TOKEN = "https://www.coasapp.com/paypal/braintree/main.php";
    final String API_CHECK_OUT = "https://www.coasapp.com/paypal/braintree/checkout.php";

    static String token;
    String amount;

    HashMap<String,String> paramsHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(getApplicationContext());
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        linearLayoutProgress = findViewById(R.id.layoutProgress);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewCart);
        checkoutAdapter = new CheckoutAdapter(arrayListCheckout, getApplicationContext(), this);
        recyclerView.setAdapter(checkoutAdapter);
        textViewTotal = findViewById(R.id.textViewTotal);
        new GetCart().execute();
        new getToken().execute();
        findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal + "regulation.htm");
                intent.putExtra("title",  "User Regulations");
                startActivity(intent);
            }
        });
        APPHelper.setTerms(this,findViewById(R.id.layoutAgree));
        WebView webView = findViewById(R.id.webViewTerms);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal+"termsconditions.html");
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
                    APPHelper.launchChrome(CartActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");

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
                    APPHelper.launchChrome(CartActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");

                }
            }
        });
        checkoutAdapter.setOnDelClick(new CheckoutAdapter.OnDelClick() {
            @Override
            public void onDelClick(int position) {
                HashMap<String, String> map = arrayListCheckout.get(position);

                sqLiteDatabase.delete("cart", "pro_id=?", new String[]{map.get("pro_id")});
                /*arrayListCheckout.remove(position);
                checkoutAdapter.notifyDataSetChanged();*/
                new DeleteCart(position).execute();
                //loadCart();
            }
        });
        checkoutAdapter.setOnPlusClick(new CheckoutAdapter.PlusClick() {
            @Override
            public void onPlusClick(int i) {
                HashMap<String, String> map = arrayListCheckout.get(i);
                if (Integer.parseInt(map.get("qty")) < 21) {
                    /*map.put("qty", String.valueOf(Integer.valueOf(map.get("qty")) + 1));
                    map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));
*/
                    /*int qty = Integer.valueOf(map.get("qty"));
                    String pId = map.get("pro_id");

                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            linearLayoutProgress.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected String doInBackground(Void... voids) {
                            //String url = MAIN_URL + "get_product_stock.php?pro_id=" + pId;

                            String url = MAIN_URL + "get_product_stock.php?pro_id=" + pId;
                            return new RequestHandler().sendGetRequest(url);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            linearLayoutProgress.setVisibility(View.GONE);
                            try {
                                JSONObject object = new JSONObject(s);
                                int qty2 = Integer.parseInt(object.getString("count"));
                                APPHelper.showLog("qty", "" + qty + qty2);
                                if (qty2 < qty + 1) {
                                   *//* double unitPrice = Double.valueOf(map.get("amount")) / Integer.parseInt(map.get("qty"));
                                    map.put("qty", String.valueOf(qty2));
                                    map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));*//*

                                    Toast.makeText(CartActivity.this, "Only " + qty2 + " available", Toast.LENGTH_SHORT).show();

                                } else {

                                    map.put("qty", String.valueOf(qty + 1));
                                    double unitPrice = Double.valueOf(map.get("amount")) / qty;
                                    map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("points", Integer.valueOf(map.get("qty")) * unitPrice);
                                    contentValues.put("qty", Integer.valueOf(map.get("qty")));
                                    sqLiteDatabase.update("cart", contentValues, "pro_id=?", new String[]{pId});

                                }
                                *//*total = total + Double.valueOf(map.get("amount"));

                                newPoint = point - total;
                                checkoutAdapter.notifyDataSetChanged();
                                textViewTotal.setText("$" + total);*//*
                                loadList();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute();*/
                    int newQty = Integer.parseInt(map.get("qty")) + 1;
                    new UpdateCart(i).execute(String.valueOf(newQty), map.get("pro_id"));


                }
            }
        });
        findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal + "regulation.htm");
                intent.putExtra("title", baseUrlLocal2 + "User Regulations");
                startActivity(intent);
            }
        });
        CheckBox checkBox = findViewById(R.id.checkBoxAgree);

        checkoutAdapter.setOnMinusClick(new CheckoutAdapter.MinusClick() {
            @Override
            public void onMinusClick(int i) {

                HashMap<String, String> map = arrayListCheckout.get(i);
                String pId = map.get("pro_id");
                /*  double unitPrice = Double.valueOf(map.get("amount")) / Integer.parseInt(map.get("qty"));*/
                double unitPrice = Double.valueOf(map.get("unitamount"));

                if (Integer.parseInt(map.get("qty")) > 1) {
                    /*ContentValues contentValues = new ContentValues();
                    contentValues.put("points", Integer.valueOf(map.get("qty")) * unitPrice);
                    contentValues.put("qty", Integer.valueOf(map.get("qty")));
                    sqLiteDatabase.update("cart", contentValues, "pro_id=?", new String[]{pId});
                    loadList();*/
                    int newQty = Integer.parseInt(map.get("qty")) - 1;
                    new UpdateCart2(i).execute(String.valueOf(newQty), map.get("pro_id"));

                }

                //checkoutAdapter.notifyDataSetChanged();
            }
        });
        checkoutAdapter.setOnItemClick(new CheckoutAdapter.OnItemClick() {
            @Override
            public void onItemClick(int adapterPosition, String add_id) {
                arrayListCheckout.get(adapterPosition).put("address_id", add_id);
            }
        });

        findViewById(R.id.buttonCheckout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items = new Gson().toJson(arrayListCheckout);
                APPHelper.showLog("Items", items);
                if (arrayListCheckout.size() == 0) {
                    APPHelper.showToast(getApplicationContext(), "No items");

                }

                else if (!checkBox.isChecked()) {
                    findViewById(R.id.nestedScrollView).post(new Runnable() {
                        @Override
                        public void run() {
                            ((NestedScrollView) findViewById(R.id.nestedScrollView)).fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    APPHelper.showToast(getApplicationContext(), "Agree to terms and conditions");
                }
                else {
                   /* Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                    intent.putExtra("amount", String.valueOf(Math.round(total * 100)));
                    intent.putExtra("desc","Shopping");
                    startActivityForResult(intent, 100);*/

                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(CartActivity.this, "Please Select a Payment Method");
                    //submitPayment();

                }
            }
        });
    }

    void loadCart() {
        arrayListCheckout.clear();
        checkoutAdapter.notifyDataSetChanged();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from cart", null);
        String address = sharedPreferences.getString("address", "[]");
        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            String pId = cursor.getString(cursor.getColumnIndex("pro_id"));
            map.put("pro_id", cursor.getString(cursor.getColumnIndex("pro_id")));
            map.put("product", cursor.getString(cursor.getColumnIndex("pro_name")));
            map.put("image", cursor.getString(cursor.getColumnIndex("pro_image")));
            int qty = cursor.getInt(cursor.getColumnIndex("qty"));
           /* new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    String url = MAIN_URL + "get_product_stock.php?pro_id=" + pId;
                    return new RequestHandler().sendGetRequest(url);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    linearLayoutProgress.setVisibility(View.GONE);
                    try {
                        JSONObject object = new JSONObject(s);
                        int qty2 = Integer.parseInt(object.getString("count"));
                        if (qty2 < qty) {
                            map.put("qty", String.valueOf(qty2));

                        }
                        *//*else if(qty2==0){
                            map.put("qty", String.valueOf(cursor.getInt(cursor.getColumnIndex("qty"))));

                        }*//*
                        else {
                            map.put("qty", String.valueOf(cursor.getInt(cursor.getColumnIndex("qty"))));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();*/
            map.put("qty", String.valueOf(cursor.getInt(cursor.getColumnIndex("qty"))));
            map.put("merchant_id", String.valueOf(cursor.getString(cursor.getColumnIndex("merchant"))));
            map.put("amount", String.valueOf(cursor.getDouble(cursor.getColumnIndex("points"))));
            map.put("address", address);
            map.put("address_id", "0");

            arrayListCheckout.add(map);
            /*total = total + cursor.getDouble(cursor.getColumnIndex("points"));*/
        }
        cursor.close();

        loadList();

    }

    void loadList() {
        total = 0.0;

        for (int i = 0; i < arrayListCheckout.size(); i++) {
            HashMap<String, String> map = arrayListCheckout.get(i);

            int qty = Integer.valueOf(map.get("qty"));
            String pId = map.get("pro_id");

            new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    String url = MAIN_URL + "get_product_stock.php?pro_id=" + pId;
                    return new RequestHandler().sendGetRequest(url);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    linearLayoutProgress.setVisibility(View.GONE);
                    try {
                        JSONObject object = new JSONObject(s);
                        int qty2 = Integer.parseInt(object.getString("count"));
                        APPHelper.showLog("qty", "" + qty2);
                        if (qty2 < qty) {
                            double unitPrice = Double.valueOf(map.get("amount")) / Integer.parseInt(map.get("qty"));
                            map.put("qty", String.valueOf(qty2));
                            map.put("amount", String.valueOf(Integer.valueOf(map.get("qty")) * unitPrice));
                        }
                        total = total + Double.valueOf(map.get("amount"));
                        newPoint = point - total;
                        // checkoutAdapter.notifyDataSetChanged();
                        textViewTotal.setText(formatter.format(total));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }
        checkoutAdapter.notifyDataSetChanged();
    }

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
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
                    new UpdatePayment().execute(txnId, balanceTxn, objectSource.toString(), dateCreated);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/


    class CheckStock extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //map.put("points", String.valueOf(newPoint));
            map.put("items", items);
            return new RequestHandler().sendPostRequest(MAIN_URL + "check_stock.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    new Checkout().execute();
                   /* APPHelper.showToast(getApplicationContext(), "Order Placed");
                    onBackPressed();*/
                    /*Intent intent = new Intent(getApplicationContext(), PaymentHistoryActivity.class);
                    intent.putExtra("trans","fp");
                    startActivity(intent);*/

                } else {
                    APPHelper.showToast(getApplicationContext(), "Some Items out of stock");
                    new GetCart().execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class Checkout extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            //map.put("points", String.valueOf(newPoint));
            map.put("items", items);
            map.put("amount", String.valueOf(Math.round(total)));
            return new RequestHandler().sendPostRequest(MAIN_URL + "order_checkout_pay.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    orderId = jsonObject.getString("order_id");
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("points", String.valueOf(newPoint));
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                    double tot1 = Math.round(total * 100.0) / 100.0;
                    intent.putExtra("amount", String.valueOf(Math.round(tot1 * 100)));
                    intent.putExtra("desc", "Shopping" + "_" + orderId);
                    startActivityForResult(intent, 100);
                   /* APPHelper.showToast(getApplicationContext(), "Order Placed");
                    onBackPressed();*/
                    /*Intent intent = new Intent(getApplicationContext(), PaymentHistoryActivity.class);
                    intent.putExtra("trans","fp");
                    startActivity(intent);*/

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class UpdatePayment extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("txn_id", strings[0]);
            map.put("txn_date", strings[3]);
            map.put("source", strings[2]);
            map.put("txn_balance", strings[1]);
            map.put("amount", String.valueOf(total));
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            map.put("order_id", orderId);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_shop_payment.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);

                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    sqLiteDatabase.delete("cart", null, null);

                    Intent intent = new Intent(getApplicationContext(), BuyerOrdersActivity.class);
                    intent.putExtra("checkout", true);
                    startActivity(intent);
                    finish();
                }
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


    class GetCart extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedPreferences.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "view_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                arrayListCheckout.clear();
                checkoutAdapter.notifyDataSetChanged();
                JSONObject object = new JSONObject(s);
                JSONArray array = object.getJSONArray("products");

                for (int i = 0; i < array.length(); i++) {

                    JSONObject object1 = array.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();

                    String address = sharedPreferences.getString("address", "[]");

                    map.put("pro_id", object1.getString("productid"));
                    map.put("cart_id", object1.getString("cart_id"));
                    map.put("product", object1.getString("pro_name"));
                    JSONArray array1 = new JSONArray(object1.getString("images"));
                    map.put("image", array1.getJSONObject(0).getString("image"));
           /* new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    String url = MAIN_URL + "get_product_stock.php?pro_id=" + pId;
                    return new RequestHandler().sendGetRequest(url);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    linearLayoutProgress.setVisibility(View.GONE);
                    try {
                        JSONObject object = new JSONObject(s);
                        int qty2 = Integer.parseInt(object.getString("count"));
                        if (qty2 < qty) {
                            map.put("qty", String.valueOf(qty2));

                        }
                        *//*else if(qty2==0){
                            map.put("qty", String.valueOf(cursor.getInt(cursor.getColumnIndex("qty"))));

                        }*//*
                        else {
                            map.put("qty", String.valueOf(cursor.getInt(cursor.getColumnIndex("qty"))));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();*/
                    map.put("qty", object1.getString("cart_qty"));
                    map.put("merchant_id", object1.getString("pro_user_id"));
                    map.put("unitamount", object1.getString("price"));
                    map.put("count", object1.getString("count"));

                    map.put("amount", String.valueOf(Double.valueOf(object1.getString("cart_qty")) * Double.valueOf(object1.getString("price"))));
                    map.put("address", address);
                    map.put("address_id", "0");
                    arrayListCheckout.add(map);

                }
                checkoutAdapter.notifyDataSetChanged();
                getTotal();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class UpdateCart extends AsyncTask<String, Void, String> {

        int index;

        public UpdateCart(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("qty", strings[0]);
            map.put("pro_id", strings[1]);
            map.put("cart_id", arrayListCheckout.get(index).get("cart_id"));
            map.put("user_id", getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {

                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    HashMap<String, String> map = arrayListCheckout.get(index);
                    map.put("qty", String.valueOf(Integer.parseInt(map.get("qty")) + 1));
                    map.put("count", object.getString("stock"));
                    map.put("amount", String.valueOf(Integer.parseInt(map.get("qty")) * Double.valueOf(map.get("unitamount"))));
                    checkoutAdapter.notifyDataSetChanged();
                    getTotal();
                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new GetCart().execute();
    }

    class UpdateCart2 extends AsyncTask<String, Void, String> {

        int index;

        public UpdateCart2(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("qty", strings[0]);
            map.put("pro_id", strings[1]);
            map.put("cart_id", arrayListCheckout.get(index).get("cart_id"));

            map.put("user_id", getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {

                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {

                } else {
                    //APPHelper.showToast(getApplicationContext(), object.getString("message"));
                }
                HashMap<String, String> map = arrayListCheckout.get(index);
                map.put("qty", String.valueOf(Integer.parseInt(map.get("qty")) - 1));
                map.put("count", object.getString("stock"));
                map.put("amount", String.valueOf(Integer.parseInt(map.get("qty")) * Double.valueOf(map.get("unitamount"))));
                checkoutAdapter.notifyDataSetChanged();
                getTotal();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class DeleteCart extends AsyncTask<String, Void, String> {

        int index;

        public DeleteCart(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("cart_id", arrayListCheckout.get(index).get("cart_id"));
            map.put("user_id", getSharedPreferences(APP_PREF, Context.MODE_PRIVATE).getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "delete_cart.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    arrayListCheckout.remove(index);
                    checkoutAdapter.notifyDataSetChanged();
                    getTotal();
                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getTotal() {
        total = 0.0;

        for (int i = 0; i < arrayListCheckout.size(); i++) {
            HashMap<String, String> map = arrayListCheckout.get(i);
            total = total + Double.valueOf(map.get("amount"));
            newPoint = point - total;
            // checkoutAdapter.notifyDataSetChanged();
            textViewTotal.setText(formatter.format(total));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
    }

    private void submitPayment() {

      //  Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(CartActivity.this),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

     //   Toast.makeText(this, String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
      //  Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNonce = nonce.getNonce();

                if (!textViewTotal.getText().toString().isEmpty()) {
                    amount = textViewTotal.getText().toString();
                    String value = amount;
                    value = value.substring(1);
                //    Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
                    paramsHash = new HashMap<>();
                    paramsHash.put("amount",value);
                    paramsHash.put("nonce", strNonce);

                    sendPayments();

                } else {
                    Toast.makeText(this, "Please Enter an amount", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "User Cancelled the Request", Toast.LENGTH_SHORT).show();
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("EDMT_ERROR", error.toString());
            }
        }
    }

    private void sendPayments() {

        RequestQueue queue = Volley.newRequestQueue(CartActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_CHECK_OUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.toString().contains("Successful"))
                {
                    // Toast.makeText(Payalpayment.this, "Transaction Successfull!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Toast.makeText(Payalpayment.this, "Transaction failed!", Toast.LENGTH_SHORT).show();

                }
                Log.d("EDMT_LOG",response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("EDMT_ERROR",error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                if(paramsHash == null)
                {
                    return null;
                }

                Map<String,String> param = new HashMap<>();
                for(String key:paramsHash.keySet())
                {
                    param.put(key,paramsHash.get(key));
                }
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> params = new HashMap<>();
                params.put("Content_Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public class getToken extends AsyncTask {

        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog = new ProgressDialog(CartActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            mDialog.setCancelable(false);
            mDialog.setMessage("Please wait");
            mDialog.show();

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mDialog.dismiss();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token = responseBody;
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {

                }
            });


            return null;


        }


    }


    public class ViewDialog {
        public void showDialog(Activity activity, String msg) {


            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.select_payment_method_dialog);

            RadioButton radioButton_paypal = (RadioButton) dialog.findViewById(R.id.radioButton_paypal);
            RadioButton radioButton_cash = (RadioButton) dialog.findViewById(R.id.radioButton_cash);
            Button payment_method_dialog_ok = (Button) dialog.findViewById(R.id.payment_method_dialog_ok);
            Button payment_method_dialog_cancel = (Button) dialog.findViewById(R.id.payment_method_dialog_cancel);
            //  text.setText(msg);

            //      Button dialogButton = (Button) dialog.findViewById(R.id.payment_method_dialog_cancel);

            radioButton_paypal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        payment_type = "paypal";
                        radioButton_cash.setChecked(false);
                    } else {
                        payment_type = "null";
                    }
                }
            });

            radioButton_cash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        payment_type = "cash";
                        radioButton_paypal.setChecked(false);
                    } else {
                        payment_type = "null";
                    }
                }
            });


            payment_method_dialog_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(payment_type.equals("cash"))
                    {
                        Intent intent = new Intent(CartActivity.this, Pleasepaycash.class);
                        intent.putExtra("totalamount",textViewTotal.getText().toString());
                        startActivity(intent);
                    }
                    else if(payment_type.equals("paypal"))
                    {
                        submitPayment();
                    }

                    dialog.dismiss();
                }
            });

            payment_method_dialog_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }

    }

}