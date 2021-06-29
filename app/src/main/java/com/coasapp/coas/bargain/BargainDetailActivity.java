package com.coasapp.coas.bargain;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.Spinner;
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
import com.coasapp.coas.R;
import com.coasapp.coas.general.FullScreenImageSlide;
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.payment.StripePaymentActivity;

import com.coasapp.coas.roombook.BookingDetailsActivity;
import com.coasapp.coas.roombook.HourlyRoomBookFragment;
import com.coasapp.coas.shopping.MyProductImagesAdapter;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.webservices.GetCommission;

import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.webservices.GetCharges;
import com.coasapp.coas.utils.DateFormats;
import com.coasapp.coas.utils.FindDistance;
import com.coasapp.coas.utils.LocationHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class BargainDetailActivity extends AppCompatActivity implements APPConstants {

    String status = "Accepted", bargainId = "0", driverID = "0", amount = "0.00", orderId = "", tripStatus = "", remarks = "", tip = "0.00";
    float rating;

    LinearLayout linearLayoutProgress;
    double charges, newamt, totalamt;
    ImageView imageViewReg;
    File img1;
    String imgPath = "", feedback = "";
    RatingBar ratingBarSend;
    EditText editTextRemark, editTextTip;
    BargainDriverImagesAdapter imagesAdapter;
    APICallbacks apiCallbacks;
    boolean granted = false;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    ArrayList<String> arrayListImages = new ArrayList<>();
    APIService apiService;
    Spinner spinnerReg;
    String userId ="";

    static final String API_GET_TOKEN = "https://www.coasapp.com/paypal/braintree/main.php";
    final String API_CHECK_OUT = "https://www.coasapp.com/paypal/braintree/checkout.php";

    static String token;
   // String amount;

    HashMap<String,String> paramsHash;

    double newprice;

    String incoming_userid;

    String uid,did;

    String bargin_id;
    String bargin_status;

    String user_payment;

    Button acceptPayment,rejectPayment;
    LinearLayout LL_payment,LL_confirm_reject;
    String send_to = "",message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bargain_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        acceptPayment = findViewById(R.id.button_makepayment);
        rejectPayment = findViewById(R.id.button_rejectpayment);
        LL_payment = findViewById(R.id.LL_payment);
        LL_confirm_reject = findViewById(R.id.LL_confirm_reject);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        userId = sharedPreferences.getString("userId", "");
      //  incoming_userid = sharedPreferences.getString(APPConstants.incoming_user_id,null);
     //   Toast.makeText(this, incoming_userid, Toast.LENGTH_SHORT).show();
        uid = getIntent().getStringExtra("uid");
        did = getIntent().getStringExtra("did");
        user_payment = getIntent().getStringExtra("payment");
        bargin_status = getIntent().getStringExtra("status");

        if(userId.equals(uid))
        {
            send_to = did;
            message = "";
            user_payment = "yes";
        }
        else
        {
            send_to = uid;
            message = "";
            user_payment = "no";
        }


        if(user_payment.equals("yes"))
        {
         //   Toast.makeText(this, user_payment, Toast.LENGTH_SHORT).show();
            if(bargin_status.equals("PaymentPending"))
            {
                LL_payment.setVisibility(View.VISIBLE);
                LL_confirm_reject.setVisibility(View.GONE);
            }
            else
            {
                LL_payment.setVisibility(View.GONE);
                LL_confirm_reject.setVisibility(View.GONE);
            }
        }
        else
        {
            if(bargin_status.equals("Pending"))
            {
                LL_payment.setVisibility(View.GONE);
                LL_confirm_reject.setVisibility(View.VISIBLE);
            }
            else
            {
                LL_payment.setVisibility(View.GONE);
                LL_confirm_reject.setVisibility(View.GONE);
            }
          //  Toast.makeText(this, user_payment, Toast.LENGTH_SHORT).show();

        }

        acceptPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitPayment();
                finish();

            }
        });

        rejectPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendNotification("0");
                finish();

            }
        });


       /* Toast.makeText(BargainDetailActivity.this, uid, Toast.LENGTH_SHORT).show();
        Toast.makeText(BargainDetailActivity.this, did, Toast.LENGTH_SHORT).show();*/
        linearLayoutProgress = findViewById(R.id.layoutProgress);
        RecyclerView recyclerViewImagesDriver = findViewById(R.id.recyclerViewImages);
        imagesAdapter = new BargainDriverImagesAdapter(productImagesArrayList, getApplicationContext());
        recyclerViewImagesDriver.setAdapter(imagesAdapter);
        imageViewReg = findViewById(R.id.imageViewCarReg);
        ratingBarSend = findViewById(R.id.ratingBarSend);
        RatingBar ratingBarView = findViewById(R.id.ratingBarView);
        editTextRemark = findViewById(R.id.editTextRemark);
        LinearLayout linearLayoutAgreeC = findViewById(R.id.agreeCustomer);
        LinearLayout linearLayoutAgreeD = findViewById(R.id.agreeDriver);
        CheckBox checkBoxAgree = linearLayoutAgreeC.findViewById(R.id.checkBoxAgree);
        CheckBox checkBoxAgree2 = linearLayoutAgreeD.findViewById(R.id.checkBoxAgree);
        WebView webView = linearLayoutAgreeC.findViewById(R.id.webViewTerms);
        WebView webView2 = linearLayoutAgreeD.findViewById(R.id.webViewTerms);
        APPHelper.setTerms(this,linearLayoutAgreeC);
        APPHelper.setTerms(this,linearLayoutAgreeD);
        spinnerReg = findViewById(R.id.spinnerTrip);
        new getToken().execute();
        try {
            final JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            String status2 = object.getString("bargain_status");
            bargin_id = object.getString("bargain_id");

     //       Toast.makeText(this, bargin_id, Toast.LENGTH_SHORT).show();
            apiCallbacks = new APICallbacks() {
                @Override
                public void taskStart() {
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void taskEnd(String type, String response) {
                    linearLayoutProgress.setVisibility(View.GONE);
                    try {
                        JSONObject object1 = new JSONObject(response);
                        if (type.equalsIgnoreCase("confirm")) {

                            if (object1.getString("response_code").equalsIgnoreCase("1")) {
                                APPHelper.showToast(getApplicationContext(), "You have Confirmed Delivery");
                                findViewById(R.id.buttonConfirmDelivery).setVisibility(View.GONE);
                                findViewById(R.id.layoutSendRating).setVisibility(View.VISIBLE);

                                if (rating > 0) {

                                    ratingBarSend.setRating(rating);
                                    ratingBarSend.setIsIndicator(true);
                                    findViewById(R.id.buttonEdit).setVisibility(View.VISIBLE);
                                    findViewById(R.id.buttonSend).setVisibility(View.GONE);
                                    editTextRemark.setFocusable(false);
                                    editTextRemark.setText(object.getString("bargain_feedback"));

                                }
                                setResult(RESULT_OK);

                            }
                        } else if (type.equalsIgnoreCase("request")) {

                            if (object1.getString("response_code").equalsIgnoreCase("1")) {
                                APPHelper.showToast(getApplicationContext(), "Payment Request Sent");
                                String link = object1.getString("link");
                                String name = getIntent().getStringExtra("name");

                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("merchantID", getSharedPreferences(APP_PREF, 0).getString("firstName", "") + SPACE + "sent you a bargain payment request " + link);
                                clipboard.setPrimaryClip(clip);


                            } else {
                                APPHelper.showToast(getApplicationContext(), object1.getString("message"));
                            }
                            setResult(RESULT_OK);
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            apiService = new APIService(apiCallbacks, getApplicationContext());
            APPHelper.showLog("details", object.toString());
            bargainId = object.getString("bargain_id");
            driverID = object.getString("bargain_driver_id");
            Glide.with(getApplicationContext())
                    .load(MAIN_URL_IMAGE + object.getString("image"))
                    .into((ImageView)
                            findViewById(R.id.imageViewProfile));
            amount = object.getString("bargain_amount");
            orderId = object.getString("bargain_ref");
            ((TextView) findViewById(R.id.textViewCar)).setText(object.getString("brand_name") + SPACE + object.getString("model_name"));
            ((TextView) findViewById(R.id.textViewCustomer)).setText(object.getString("name"));
            ((TextView) findViewById(R.id.textViewSource)).setText(object.getString("bargain_source"));
            ((TextView) findViewById(R.id.textViewDest)).setText(object.getString("bargain_dest"));
/*
            ((TextView) findViewById(R.vehicleId.textViewPersons)).setText(object.getString("bargain_persons"));
*/
            findViewById(R.id.imageViewProfile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                    try {
                        if (!object.getString("coas_id").equals(new MyPrefs(getApplicationContext(), APP_PREF).getString("coasId"))) {
                            new LaunchChatUtils(getApplicationContext(), BargainDetailActivity.this, new LaunchChatCallbacks() {
                                @Override
                                public void onChatCreatedSuccess(Intent intent) {
                                    intent.putExtra("from", "bargain");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LaunchChatUtils.launchChatMessageActivity(BargainDetailActivity.this, intent);

                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
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
                            }).createChatDialog(object.getString("coas_id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //Date date = sdfDatabaseTime.parse(object.getString("bargain_time"));
            DateFormats dateFormats = new DateFormats();

            SimpleDateFormat sdf1 =
                    dateFormats.getFormatDateTimeDb();
            sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date1 = sdf1.parse(object.getString("bargain_date"));
            //parse the date into another format
            //String strTime = sdfNativeTime.format(date);
            SimpleDateFormat sdf2 = dateFormats.getFormatDateTimeNative();
            sdf2.setTimeZone(TimeZone.getDefault());
            String strDate1 = sdf2.format(date1);
            APPHelper.showLog("date", "" + date1);
            ((TextView) findViewById(R.id.textViewDate)).setText(strDate1 + "\nBooking ID: " + object.getString("bargain_ref"));
            // ((TextView) findViewById(R.driverId.textViewTime)).setText(strTime);
            ((TextView) findViewById(R.id.textViewAmount)).setText("Fare: " + formatter.format(Double.valueOf(object.getString("bargain_amount"))));
            if (Double.valueOf(object.getString("bargain_tip")) > 0) {
                ((TextView) findViewById(R.id.textViewAmount)).append(" Tip: " + formatter.format(Double.valueOf(object.getString("bargain_tip"))));

            }

            JSONArray jsonArray = new JSONArray(object.getString("images"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ProductImages productImages = new ProductImages();
                productImages.setId(jsonObject.getString("image_id"));
                productImages.setUrlImage(jsonObject.getString("image"));
                productImages.setColor("#fffafafa");
                productImages.setSource("url");
                productImages.setStatus("1");
                productImagesArrayList.add(productImages);
                arrayListImages.add(MAIN_URL_IMAGE + jsonObject.getString("image"));
            }

            imagesAdapter.notifyDataSetChanged();
            imagesAdapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
                @Override
                public void onClick(int position) {

                    productImagesArrayList.remove(position);
                    arrayListImages.remove(position);
                    imagesAdapter.notifyDataSetChanged();

                }
            });
            imagesAdapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
                @Override
                public void onClick(int position) {

                    Intent intent = new Intent(getApplicationContext(), FullScreenImageSlide.class);
                    intent.putExtra("position", position);
                    intent.putExtra("images", arrayListImages);
                    startActivity(intent);

                }
            });
            Calendar calendar = Calendar.getInstance();
            String currentTime = sdfDatabaseDateTime.format(calendar.getTime());
            long currentUnix = APPHelper.getUnixTime(currentTime);
            long tripUnix = APPHelper.getUnixTime(object.getString("bargain_date"));
            Log.i("TimeDiff1", "" + (tripUnix));
            Log.i("TimeDiff1", "" + (currentUnix));
            Log.i("TimeDiff1", "" + (currentUnix - tripUnix));
           /* if (currentUnix - tripUnix <= 3600) {
                findViewById(R.driverId.layoutAccept).setVisibility(View.VISIBLE);

            } else {
                findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);

            }*/

            ChargeAsyncCallbacks chargeAsyncCallbacks = new ChargeAsyncCallbacks() {
                @Override
                public void onTaskStart() {
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTaskEnd(String result) {
                    try {
                        JSONObject object1 = new JSONObject(result);
                        charges = Double.valueOf(object1.getString("charge_percent")) / 100;


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    linearLayoutProgress.setVisibility(View.GONE);
                }
            };

            ChargeAsyncCallbacks chargeAsyncCallbacks2 = new ChargeAsyncCallbacks() {

                @Override
                public void onTaskStart() {
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTaskEnd(String result) {
                    try {

                        linearLayoutAgreeD.setVisibility(View.VISIBLE);
                        ((TextView) linearLayoutAgreeD.findViewById(R.id.textViewCommission)).setVisibility(View.VISIBLE);
                        JSONObject object1 = new JSONObject(result);
                        String charges = object1.getString("commission_value");
                        ((TextView) linearLayoutAgreeD.findViewById(R.id.textViewCommission)).setText("As a driver you are responsible to pay taxes to your city/state’s proper authorities and follow your local driving regulation; platform fee is " + charges + "%");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    linearLayoutProgress.setVisibility(View.GONE);
                }
            };

            HashMap<String, String> map1 = new HashMap<>();
            map1.put("type", "Bargain");


            HashMap<String, String> map = new HashMap<>();
            map.put("type", "bargain");
            new GetCharges(chargeAsyncCallbacks, map).execute();


            imgPath = object.getString("bargain_load_image");
            rating = Float.parseFloat(object.getString("bargain_rating"));
            editTextTip = findViewById(R.id.editTextTip);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.TripStatus));
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReg.setAdapter(arrayAdapter);
            spinnerReg.setSelection(arrayAdapter.getPosition(object.getString("bargain_status")));
            if (!imgPath.equals("")) {
                Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + imgPath).into(imageViewReg);
            }
            Log.i("Role", getIntent().getStringExtra("role"));
            if (getIntent().getStringExtra("role").equalsIgnoreCase("customer")) {
                if (status2.equalsIgnoreCase("requested")
                        /*|| status2.equalsIgnoreCase("Accepted")
                        || status2.equalsIgnoreCase("Pickup")
                        || status2.equalsIgnoreCase("Dropoff")
                        || status2.equalsIgnoreCase("Completed")
                        || (status2.equalsIgnoreCase("rejected"))
                        || (status2.equalsIgnoreCase("Pending"))*/) {
                    findViewById(R.id.layoutAccept).setVisibility(View.VISIBLE);
                    //((TextView) findViewById(R.driverId.textViewAmount)).setText("Fare: " + formatter.format(Double.valueOf(object.getString("bargain_amt"))));

                } else {
                    findViewById(R.id.layoutAccept).setVisibility(View.GONE);
                   /* if (currentUnix - tripUnix >= 3600) {
                        findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);

                    } else {
                        findViewById(R.driverId.layoutAccept).setVisibility(View.VISIBLE);

                    }
*/
                }
                findViewById(R.id.layoutDriver).setVisibility(View.GONE);
                findViewById(R.id.cardViewAdd).setVisibility(View.GONE);

                if (status2.equalsIgnoreCase("Dropoff")) {
                    if (object.getString("bargain_delivery_approve").equalsIgnoreCase("0")) {
                        findViewById(R.id.buttonConfirmDelivery).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.layoutSendRating).setVisibility(View.VISIBLE);

                        if (rating > 0) {
                            ratingBarSend.setRating(rating);
                            ratingBarSend.setIsIndicator(true);
                            //findViewById(R.id.buttonEdit).setVisibility(View.VISIBLE);
                            findViewById(R.id.buttonSend).setVisibility(View.GONE);
                            editTextRemark.setFocusable(false);
                            editTextRemark.setText(object.getString("bargain_feedback"));
                        }
                    }
                }
            }




           /* if (getIntent().getStringExtra("role").equals("driver") && status2.equalsIgnoreCase("accepted")) {
                findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);
                if (currentUnix < tripUnix) {
                    findViewById(R.driverId.layoutDriver).setVisibility(View.VISIBLE);

                } else {
                    findViewById(R.driverId.layoutDriver).setVisibility(View.GONE);

                }

            }*/
            /*findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPermissionStorage();
                    if (granted)
                        showPopUp(v);
                    else {
                        showAlert("Allow Storage Permission");
                    }
                }
            });*/
            if (getIntent().getStringExtra("role").equalsIgnoreCase("driver")) {
                ((TextView) findViewById(R.id.textViewAmount)).append("\nService Charges: " + formatter.format(Double.valueOf(object.getString("bargain_commission"))));
                ((TextView) findViewById(R.id.textViewAmount)).append("\nReceivable: " + formatter.format(Double.valueOf(object.getString("bargain_receivable"))));
                findViewById(R.id.layoutAccept).setVisibility(View.GONE);
                Log.i("TimeDiff", "" + (tripUnix));
                Log.i("TimeDiff", "" + (currentUnix));
                Log.i("TimeDiff", "" + (tripUnix - currentUnix));
                /*if (currentUnix - tripUnix > 3600) {

                    findViewById(R.driverId.layoutDriver).setVisibility(View.GONE);

                } else {*/
                Log.d("status", status2);
                if (status2.equalsIgnoreCase("requested") || status2.equalsIgnoreCase("Pending")) {
                    editTextTip.setVisibility(View.GONE);

                    linearLayoutAgreeC.setVisibility(View.GONE);
                    //findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);
                    findViewById(R.id.layoutDriver).setVisibility(View.GONE);
                    //findViewById(R.driverId.buttonRequest).setVisibility(View.VISIBLE);
                    if (!status2.equalsIgnoreCase("expired")) {
                        if (status2.equalsIgnoreCase("pending")) {
                            findViewById(R.id.tvagreedriver).setVisibility(View.VISIBLE);
                            findViewById(R.id.layoutAccept).setVisibility(View.VISIBLE);
                            new GetCommission(chargeAsyncCallbacks2, map1).execute();
                        } else {
                            linearLayoutAgreeD.setVisibility(View.GONE);
                            findViewById(R.id.layoutAccept).setVisibility(View.GONE);

                        }
                    } else {
                        findViewById(R.id.layoutAccept).setVisibility(View.GONE);

                    }

                } else if (status2.equalsIgnoreCase("dropoff")) {

                    findViewById(R.id.cardViewAdd).setVisibility(View.GONE);
                    findViewById(R.id.layoutAccept).setVisibility(View.GONE);
                    findViewById(R.id.buttonUpdate).setVisibility(View.GONE);
                    findViewById(R.id.buttonCarReg).setVisibility(View.GONE);
                    spinnerReg.setEnabled(false);
                    if (rating > 0) {
                        findViewById(R.id.layoutMyRating).setVisibility(View.VISIBLE);
                        ratingBarView.setRating(rating);
                        ((TextView) findViewById(R.id.textViewRemarks)).setText(object.getString("bargain_feedback"));
                    }

                } else if (status2.equalsIgnoreCase("accepted") || status2.equalsIgnoreCase("pickup") || status2.equalsIgnoreCase("on the way to pickup")) {
                    //((TextView) findViewById(R.driverId.textViewAmount)).append("\nService Charges: " + formatter.format(Double.valueOf(object.getString("bargain_commission"))));
                    //((TextView) findViewById(R.driverId.textViewAmount)).append("\nReceivable: " + formatter.format(Double.valueOf(object.getString("bargain_receivable"))));

                    // findViewById(R.id.layoutDriver).setVisibility(View.VISIBLE);

                    //findViewById(R.id.layoutAccept).setVisibility(View.GONE);
                    if (status2.equalsIgnoreCase("pickup")) {
                        double lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                        double lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                        double distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("dest_lat")), Double.valueOf(object.getString("dest_lng"))) * 100.0) / 100.0;
                        if (distance1 <= 0.01) {
                            Toast.makeText(this, "Near Destination", Toast.LENGTH_SHORT).show();
                            spinnerReg.setSelection(2);
                        }
                    }
                    findViewById(R.id.buttonGPS).setVisibility(View.VISIBLE);
                        /*if (rating > 0) {
                            findViewById(R.driverId.layoutMyRating).setVisibility(View.VISIBLE);
                            ratingBarView.setRating(rating);
                            ((TextView) findViewById(R.driverId.textViewRemarks)).setText(object.getString("bargain_feedback"));
                        }
*/
                } else if ((status2.equalsIgnoreCase("cancelled") || (status2.equalsIgnoreCase("rejected")) || (status2.equalsIgnoreCase("expired")))) {
                    findViewById(R.id.layoutDriver).setVisibility(View.GONE);
                    findViewById(R.id.cardViewAdd).setVisibility(View.GONE);
                    findViewById(R.id.layoutAccept).setVisibility(View.GONE);
                    findViewById(R.id.buttonUpdate).setVisibility(View.GONE);
                    findViewById(R.id.buttonCarReg).setVisibility(View.GONE);
                    spinnerReg.setEnabled(false);
                }

            }
                /*if(status2.equalsIgnoreCase("accepted")){

                    findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);
                    if (currentUnix < tripUnix) {
                        findViewById(R.driverId.layoutDriver).setVisibility(View.VISIBLE);

                    } else {
                        findViewById(R.driverId.layoutDriver).setVisibility(View.GONE);

                    }

                }

                if (status2.equalsIgnoreCase("dropoff")) {


                    findViewById(R.driverId.layoutAccept).setVisibility(View.GONE);
                    findViewById(R.driverId.buttonUpdate).setVisibility(View.GONE);
                    findViewById(R.driverId.buttonCarReg).setVisibility(View.GONE);
                    spinnerReg.setEnabled(false);
                    if (rating > 0) {
                        findViewById(R.driverId.layoutMyRating).setVisibility(View.VISIBLE);
                        ratingBarView.setRating(rating);
                        ((TextView) findViewById(R.driverId.textViewRemarks)).setText(object.getString("bargain_feedback"));
                    }

                }
                if(status2.equalsIgnoreCase("requested")){
                    if (currentUnix < tripUnix) {
                        findViewById(R.driverId.layoutDriver).setVisibility(View.VISIBLE);

                    } else {
                        findViewById(R.driverId.layoutDriver).setVisibility(View.GONE);

                    }
                }*/
            //}
            findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextRemark.setFocusableInTouchMode(true);
                    editTextRemark.setFocusable(true);
                    editTextRemark.requestFocus();
                    v.setVisibility(View.GONE);
                    findViewById(R.id.buttonSend).setVisibility(View.VISIBLE);
                    ratingBarSend.setIsIndicator(false);
                }
            });
            findViewById(R.id.buttonGPS).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(LocationHelper.checkPermissionLocation(getApplicationContext()))) {
                        APPHelper.goToAppPage(BargainDetailActivity.this, "Allow Location Permission");
                    } else if (!LocationHelper.checkGPS(getApplicationContext())) {
                        LocationHelper.goToLocationSettings(BargainDetailActivity.this);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), TrackLocationActivity.class);
                        intent.putExtra("driverId", driverID);
                        intent.putExtra("detail", getIntent().getStringExtra("details"));
                        intent.putExtra("role", "driver");
                        startActivityForResult(intent, 50);
                    }
                }
            });


            findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    HashMap<String, String> map1 = new HashMap<>();
                    map1.put("bargain_id", bargainId);
                    map1.put("driver_id", driverID);
                    apiService.callAPI(map1, MAIN_URL + "request_delivery.php", "request");
                    Toast.makeText(BargainDetailActivity.this, "Requested", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BargainDetailActivity.this,MyBargainRequests.class);
                    startActivity(intent);

                }
            });
            findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                    boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                    if (ActivityCompat.checkSelfPermission(BargainDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showAlert("Allow Location Permission");
                    } else if (!LocationHelper.checkGPS(getApplicationContext())) {
                        showAlert2();
                    } else {
                        Log.i("Images", "" + productImagesArrayList.size());
                        if (productImagesArrayList.size() > 10 || productImagesArrayList.size() < 5)

                            APPHelper.showToast(getApplicationContext(), "upload 5 to 10 images");
                        else if (spinnerReg.getSelectedItemPosition() == 0) {
                            APPHelper.showToast(getApplicationContext(), "Select Trip Status");

                        } else {
                            boolean nearby = false;
                            double distance1;
                            status = spinnerReg.getSelectedItem().toString();
                            double lat = Double.valueOf(sharedPreferences.getString("lat", "0.0"));
                            double lng = Double.valueOf(sharedPreferences.getString("lng", "0.0"));
                            try {
                                if (status.equalsIgnoreCase("pickup")) {
                                    distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("source_lat")), Double.valueOf(object.getString("source_lng"))) * 100.0) / 100.0;
                                    if (distance1 <= 0.01)
                                        nearby = true;

                                } else if (status.equalsIgnoreCase("dropoff")) {
                                    distance1 = Math.round(new FindDistance().distance(lat, lng, Double.valueOf(object.getString("dest_lat")), Double.valueOf(object.getString("dest_lng"))) * 100.0) / 100.0;
                                    if (distance1 <= 0.01)
                                        nearby = true;
                                }
                                if (!nearby && status.equalsIgnoreCase("pickup")) {
                                    APPHelper.showToast(getApplicationContext(), "Not near pickup location");
                                } else if (!nearby && status.equalsIgnoreCase("dropoff")) {
                                    APPHelper.showToast(getApplicationContext(), "Not near dropoff location");
                                } else {

                                    JSONArray array = new JSONArray("[]");
                                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                                        JSONObject object1 = new JSONObject();
                                        object1.put("image", productImagesArrayList.get(i).getUrlImage());
                                        array.put(object1);
                                    }

                                    new UpdateTrip().execute(spinnerReg.getSelectedItem().toString(), array.toString());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }


                }
            });
            findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getIntent().getStringExtra("role").equalsIgnoreCase("driver")) {
                        if (!checkBoxAgree2.isChecked()) {
                            APPHelper.showToast(BargainDetailActivity.this, "Agree to the terms & Conditions");
                        } else {
                           /* Intent intent = new Intent(getApplicationContext(), BarginDetailsActivityNonPayment.class);
                            newamt = (Double.valueOf(amount) *//*+ Double.valueOf(tip)*//*);
                            double totalamt1 = newamt + (newamt * charges);
                            totalamt = Math.round(totalamt1 * 100.0) / 100.0;
                            try {
                                intent.putExtra("amount", amount);
                                intent.putExtra("receivable", object.getString("bargain_receivable"));
                                intent.putExtra("role", "driver");

                                intent.putExtra("desc", "Bargain" + "_" + orderId);

                                startActivityForResult(intent, 90);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }*/
                            sendNotification("1");
                          //  submitPayment();
                        }

                    } else {
                        status = "Accepted";
                        if (editTextTip.getText().toString().length() > 0) {
                            tip = editTextTip.getText().toString();
                        } else {
                            tip = "0.00";
                        }
                        if (InputValidator.isValidNumber(tip)) {
                            if (!checkBoxAgree.isChecked()) {
                                APPHelper.showToast(BargainDetailActivity.this, "Agree to the terms & Conditions");
                            } else {
                                try {
                                    new UpdateRequest().execute(object.getString("bargain_id"), object.getString("bargain_driver_id"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            Toast.makeText(BargainDetailActivity.this, "Enter valid tip", Toast.LENGTH_SHORT).show();
                        }
                    }

                    /*Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                    intent.putExtra("amount", String.valueOf(Math.round(Double.valueOf(amount) * 100)));
                    intent.putExtra("desc", "Bargain");
                    startActivityForResult(intent, 100);*/
                    // new UpdateRequest().execute(object.getString("bargain_id"), object.getString("bargain_driver_id"));
                    //new UpdateRequest().execute("", "", "", "");


                }
            });

            findViewById(R.id.buttonCarReg).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    APPHelper.showLog("Click", "Reg");
                    checkPermissionStorage();
                    if (granted)
                        showPopUp(v);
                    else {
                        showAlert("Allow Storage Permission");
                    }
                }
            });

            findViewById(R.id.button_reject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status = "Rejected";
                    sendNotification("0");
                    new UpdateRequest().execute("", "", "", "");
                }
            });
            findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    feedback = editTextRemark.getText().toString().trim();
                    rating = ratingBarSend.getRating();
                    if (rating == 0) {
                        APPHelper.showToast(getApplicationContext(), "Send Rating");
                    } else if (feedback.equalsIgnoreCase("")) {
                        APPHelper.showToast(getApplicationContext(), "Enter feedback");
                    } else {
                        new SendRating().execute();
                    }
                }
            });

            findViewById(R.id.buttonConfirmDelivery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> map1 = new HashMap<>();
                    map1.put("bargain_id", bargainId);
                    map1.put("driver_id", driverID);
                    apiService.callAPI(map1, MAIN_URL + "confirm_bargain_delivery.php", "confirm");
                    finish();
                }


            });
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
                        //startActivity(intent);
                        APPHelper.launchChrome(BargainDetailActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");
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
                        //startActivity(intent);
                        APPHelper.launchChrome(BargainDetailActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");
                    }
                }
            });

            webView2.getSettings().setJavaScriptEnabled(true);
            webView2.loadUrl(baseUrlLocal + "termsconditions.html");
            webView2.setBackgroundColor(Color.TRANSPARENT);
            webView2.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                    if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                        webView2.goBack();
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                        intent.putExtra("title", "User Regulations");
                        APPHelper.launchChrome(BargainDetailActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                        webView2.goBack();
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                        intent.putExtra("title", "User Regulations");
                        APPHelper.launchChrome(BargainDetailActivity.this,APPConstants.baseUrlLocal2+"terms-conditions/");
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        // String names =getIntent().getStringExtra("name",null);
    }

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(BargainDetailActivity.this, v);
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
                            img1 = GetPath.createImageFile(BargainDetailActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(BargainDetailActivity.this, getPackageName() + ".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);
                            switch (v.getId()) {
                                case R.id.cardViewAdd:
                                    code[0] = 0;
                                    break;
                            }

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
                      /*  Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }
                        intent.setType("image/*");
                        switch (v.getId()) {
                            case R.id.cardViewAdd:
                                code[0] = 5;
                                break;
                        }
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code[0]);
                        break;*/

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }
                        intent.setType("image/*");
                        switch (v.getId()) {
                            case R.id.cardViewAdd:
                                code[0] = 5;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //APPHelper.showToast(getContext(), "Paid");
                try {
                    JSONObject object = new JSONObject(data.getStringExtra("charge"));
                    String txnId = object.getString("id");
                    String balanceTxn = object.getString("balance_transaction");
                    amount = String.valueOf(object.getInt("amount") / 100);
                    long created = object.getLong("created");
                    String desc = object.getString("description");
                    JSONObject objectSource = object.getJSONObject("source");
                    Date date4 = new java.util.Date(created * 1000L);
                    String dateCreated = sdf.format(date4);
                    new UpdatePayment().execute(txnId, balanceTxn, objectSource.toString(), dateCreated);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 0) {
                imgPath = ResizeImage.getResizedImage(img1.getAbsolutePath());
                Glide.with(getApplicationContext()).load(imgPath).into(imageViewReg);
                ProductImages productImages = new ProductImages();
                productImages.setImage(imgPath);
                productImages.setStatus("0");
                productImages.setColor("#ff000000");
                productImagesArrayList.add(productImages);

                imagesAdapter.notifyDataSetChanged();
                arrayListImages.add(imgPath);
                new UploadBill(productImagesArrayList.size() - 1).execute();
            } else if (requestCode == 5) {

                if (data.getData() != null) {
                    imgPath = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
                    Glide.with(getApplicationContext()).load(imgPath).into(imageViewReg);
                    ProductImages productImages = new ProductImages();
                    productImages.setImage(imgPath);
                    productImages.setStatus("0");
                    productImages.setColor("#ff000000");
                    arrayListImages.add(imgPath);
                    productImagesArrayList.add(productImages);
                    imagesAdapter.notifyDataSetChanged();
                    new UploadBill(productImagesArrayList.size() - 1).execute();
                } else if (data.getClipData() != null) {
                    int size = productImagesArrayList.size();
                    ClipData mClipData = data.getClipData();
                    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        imgPath = GetPath.getPath(getApplicationContext(), uri);
                        ProductImages productImages = new ProductImages();
                        productImages.setImage(imgPath);
                        productImages.setStatus("0");
                        productImages.setColor("#ff000000");
                        productImagesArrayList.add(productImages);
                        arrayListImages.add(imgPath);
                        imagesAdapter.notifyDataSetChanged();
                    }
                    for (int i = size; i < productImagesArrayList.size(); i++) {
                        Log.i("Image", productImagesArrayList.get(i).getImage());
                        new UploadBill(i).execute();
                    }
                }

            } else if (requestCode == 90) {
                HashMap<String, String> map1 = new HashMap<>();
                map1.put("bargain_id", bargainId);
                map1.put("driver_id", driverID);
                apiService.callAPI(map1, MAIN_URL + "request_delivery.php", "request");

            } else if (requestCode == 50) {
                setResult(RESULT_OK);
                finish();
            }
            else if(requestCode == 200)
            {

               // sendNotification();
                //   Toast.makeText(this, String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                //  Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();



                /*try {
                    intent.putExtra("amount", amount);
                    intent.putExtra("receivable", object.getString("bargain_receivable"));
                    intent.putExtra("role", "driver");

                    intent.putExtra("desc", "Bargain" + "_" + orderId);

                    startActivityForResult(intent, 90);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                if (resultCode == RESULT_OK) {
              /*      DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                    PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                    String strNonce = nonce.getNonce();

                    newamt = (Double.valueOf(amount) *//*+ Double.valueOf(tip)*//*);
                    double totalamt1 = newamt + (newamt * charges);
                    Toast.makeText(this, String.valueOf(totalamt1), Toast.LENGTH_SHORT).show();
                    totalamt = Math.round(totalamt1 * 100.0) / 100.0;*/

                    if (!String.valueOf(newprice).isEmpty()) {
                    /*    amount = String.valueOf(newprice);
                        Toast.makeText(getApplicationContext(), String.valueOf(totalamt1), Toast.LENGTH_SHORT).show();
                        // amount = textViewTotal.getText().toString();
                        String value = amount;
                        value = value.substring(1);
                        //    Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
                        paramsHash = new HashMap<>();
                        paramsHash.put("amount",String.valueOf(totalamt1));
                        paramsHash.put("nonce", strNonce);*/



                       // sendPayments();

                    } else {
                        Toast.makeText(getApplicationContext(), "Please Enter an amount", Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "User Cancelled the Request", Toast.LENGTH_SHORT).show();
                } else {
                    Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                    Log.d("EDMT_ERROR", error.toString());
                }

            }
        } else {
           /* if (requestCode == 100) {
                status = "Requested";
                new UpdateRequest().execute("", "", "", "");
            }*/
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
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_bargain_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", String.valueOf(System.currentTimeMillis()));
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, productImagesArrayList.get(index).getImage(), "driver", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    imgPath = jsonObject.getString("response");
                }
                //productImagesArrayList.get(index).setSource("url");

                productImagesArrayList.get(index).setUrlImage(imgPath);
                //imagesAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class UpdateRequest extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", uid/*sharedPreferences.getString("userId", "0")*/);
            map.put("bargain_id", bargainId);
            map.put("status", status);
            map.put("driver_id", driverID);
            map.put("tip", tip);
           /* map.put("txn_id", strings[0]);
            map.put("txn_date", strings[3]);
            map.put("source", strings[2]);
            map.put("txn_balance", strings[1]);*/
            map.put("amount", String.valueOf(Math.round(Double.valueOf(amount))));
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_bargain_request.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    if (status.equalsIgnoreCase("accepted")) {
                       /* APPHelper.showToast(getApplicationContext(), "You have accepted the trip");
                        Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                        newamt = (Double.valueOf(amount) *//*+ Double.valueOf(tip)*//*);
                        double totalamt1 = newamt + (newamt * charges);
                        totalamt = Math.round(totalamt1 * 100.0) / 100.0;
                        intent.putExtra("amount", String.valueOf(Math.round(totalamt * 100)));
                        intent.putExtra("role", "customer");
                        intent.putExtra("charge", charges * 100);
                        intent.putExtra("desc", "Bargain" + "_" + orderId);
                        startActivityForResult(intent, 100);*/
                      //  submitPayment();
                    } else {
                        APPHelper.showToast(getApplicationContext(), "You have rejected the trip");
                        setResult(RESULT_OK);
                        finish();
                    }

                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("message"));
                    setResult(RESULT_OK);
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
            map.put("amount", String.valueOf(totalamt));
            map.put("amount1", String.valueOf(newamt));
            map.put("driver_id", driverID);
            map.put("order_id", orderId);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_bargain_payment.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    setResult(RESULT_OK);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class UpdateTrip extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            String url = MAIN_URL + "update_trip_status.php";
            map.put("bargain_id", bargainId);
            map.put("driver_id", driverID);
            map.put("status", strings[0]);
            map.put("image", strings[1]);
            APPHelper.showLog("Url", url);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    APPHelper.showToast(getApplicationContext(), "Trip status updated");
                    //new MyBargainRequests.BuyerOrders().execute();
                    if (status.equalsIgnoreCase("pickup")) {
                        Intent intent = new Intent(getApplicationContext(), TrackLocationActivity.class);
                        intent.putExtra("driverId", driverID);
                        intent.putExtra("detail", getIntent().getStringExtra("details"));
                        intent.putExtra("role", "driver");
                        startActivityForResult(intent, 50);
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }

                }
                /*JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.add(object);
                }
                mAdapter.notifyDataSetChanged();*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class SendRating extends AsyncTask<String, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            String url = MAIN_URL + "send_bargain_rating.php";
            map.put("bargain_id", bargainId);
            map.put("rating", String.valueOf(rating));
            map.put("feedback", feedback);
            APPHelper.showLog("Url", url);
            return new RequestHandler().sendPostRequest(url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    //new MyBargainRequests.BuyerOrders().execute();
                    setResult(RESULT_OK);
                    ratingBarSend.setIsIndicator(true);
                    editTextRemark.setFocusable(false);
                    findViewById(R.id.buttonSend).setVisibility(View.GONE);
                    findViewById(R.id.buttonEdit).setVisibility(View.VISIBLE);
                }
                /*JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    list.add(object);
                }
                mAdapter.notifyDataSetChanged();*/
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

    public void checkPermissionStorage() {

        granted = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void showAlert(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BargainDetailActivity.this);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BargainDetailActivity.this);
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

    private void submitPayment() {

        //  Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(getApplicationContext()),200);

    }

  /*  @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }*/

    private void sendPayments() {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

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

            mDialog = new ProgressDialog(BargainDetailActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
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
                            //  Toast.makeText(getContext(), token, Toast.LENGTH_SHORT).show();
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


    private void sendNotification(String status) {

        String url = MAIN_URL + "send_bargin_approval_notification_ios.php";
     /*   Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, bargin_id, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, did, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();*/

    //    Toast.makeText(this, send_to, Toast.LENGTH_SHORT).show();
    //    Toast.makeText(this, bargin_id, Toast.LENGTH_SHORT).show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        Toast.makeText(BargainDetailActivity.this, "Requested", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BargainDetailActivity.this,MyBargainRequests.class);
                        startActivity(intent);
                        finish();
             //        Toast.makeText(BargainDetailActivity.this, response, Toast.LENGTH_SHORT).show();

                    }
                },
                error ->
                {
                    //   progressDialog.dismiss();
                     Toast.makeText(BargainDetailActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<String, String>();

                   parameters.put("status",status);
                   parameters.put("send_to",send_to);
                   parameters.put("bargain_id",bargin_id);
                   parameters.put("message",uid);
                  // parameters.put("user_id",uid);

                 //  parameters.put("did",did);



                return parameters;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(BargainDetailActivity.this);
        requestQueue.add(stringRequest);


    }
}