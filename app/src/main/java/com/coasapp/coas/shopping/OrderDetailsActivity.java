package com.coasapp.coas.shopping;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.roombook.BookingDetailsActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.utils.RequestHandler;

import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrderDetailsActivity extends AppCompatActivity implements APPConstants {

    String checkout;
    JSONObject object;
    AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CircleImageView imageViewBuyer = findViewById(R.id.imageViewBuyer);
        CircleImageView imageViewSeller = findViewById(R.id.imageViewHosted);
        final NestedScrollView scrollView = findViewById(R.id.scrollView);
        TextView textViewOrderId = findViewById(R.id.textViewOrderID);
        TextView textViewCourierTrack = findViewById(R.id.textViewCourierTrack);
        TextView textViewProduct = findViewById(R.id.textViewProduct);
        TextView textViewAmount = findViewById(R.id.textViewAmount);
        TextView textViewBuyer = findViewById(R.id.textViewBuyer);
        TextView textViewSeller = findViewById(R.id.textViewSeller);
        TextView textViewOrderDate = findViewById(R.id.textViewOrderDate);
        TextView textViewDelivery = findViewById(R.id.textViewDelivery);
        TextView textViewCourier = findViewById(R.id.textViewCourier);
        TextView textViewEst = findViewById(R.id.textViewEstimated);
        TextView textViewOrderStatus = findViewById(R.id.textViewOrderStatus);

        Button buttonReport = findViewById(R.id.buttonReport);
        final Button buttonSend = findViewById(R.id.buttonSend);
        final EditText editTextReport = findViewById(R.id.editTextReason);

        TextView textViewAddress = findViewById(R.id.textViewAddress);
        LinearLayout linearLayoutBuyer = findViewById(R.id.buyer);
        LinearLayout linearLayoutSeller = findViewById(R.id.seller);

        try {

            object = new JSONObject(getIntent().getStringExtra("details"));
            if (!object.getString("order_est").equalsIgnoreCase("0000-00-00")) {
                textViewEst.setText(sdfNativeDate.format(sdfDatabaseDate.parse(object.getString("order_est"))));
                textViewCourierTrack.setText(object.getString("order_courier_track"));
            }
            textViewOrderId.setText("Order ID: " + object.getString("order_track_id"));
            textViewOrderStatus.setText(object.getString("order_status"));
            if (object.getString("order_approved").equals("1")) {
                textViewOrderStatus.setText("Delivery Confirmed");
            }
            if (getIntent().getStringExtra("role").equals("buyer")) {
                APPHelper.showLog("Sales", "buyer");

                textViewSeller.setText(object.getString("name"));
                if (object.getString("order_buyer").equalsIgnoreCase(object.getString("order_seller"))) {
                    textViewSeller.setText("You");
                    Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageViewSeller);
                }
                linearLayoutBuyer.setVisibility(View.GONE);
                linearLayoutSeller.setVisibility(View.VISIBLE);
                APPHelper.showLog("Status", "Delivered");
                if (object.getString("order_status").equalsIgnoreCase("delivered")) {
                    if (object.getString("order_report_status").equals("")) {
                        buttonReport.setVisibility(View.VISIBLE);
                    } else {
                        APPHelper.showLog("Report", object.getString("order_report_status"));
                        textViewOrderStatus.setText(object.getString("order_report_status"));
                        /*editTextReport.setText(object.getString("order_reason"));
                        editTextReport.setVisibility(View.VISIBLE);
                        editTextReport.setFocusable(false);*/
                    }
                }
            } else {
                APPHelper.showLog("Sales", "seller");
                linearLayoutBuyer.setVisibility(View.VISIBLE);

                textViewBuyer.setText(object.getString("name"));
                if (object.getString("order_buyer").equalsIgnoreCase(object.getString("order_seller"))) {
                    textViewBuyer.setText("You");

                    Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageViewBuyer);

                }
                linearLayoutSeller.setVisibility(View.GONE);
                if (!object.getString("order_report_status").equals("")) {
                    APPHelper.showLog("Report", object.getString("order_report_status"));
                    textViewOrderStatus.setText(object.getString("order_report_status"));
                    /*editTextReport.setText(object.getString("order_reason"));
                    editTextReport.setVisibility(View.VISIBLE);
                    editTextReport.setFocusable(false);*/
                }
            }

            imageViewSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (!object.getString("coas_id").equals(new MyPrefs(getApplicationContext(), APP_PREF).getString("coasId"))) {
                            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                            new LaunchChatUtils(getApplicationContext(), OrderDetailsActivity.this, new LaunchChatCallbacks() {
                                @Override
                                public void onChatCreatedSuccess(Intent intent) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            intent.putExtra("from", "shopping");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                                    LaunchChatUtils.launchChatMessageActivity(OrderDetailsActivity.this,intent);

                                                }
                                            });
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

            imageViewBuyer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (!object.getString("coas_id").equals(new MyPrefs(getApplicationContext(), APP_PREF).getString("coasId"))) {
                            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                            new LaunchChatUtils(getApplicationContext(), OrderDetailsActivity.this, new LaunchChatCallbacks() {
                                @Override
                                public void onChatCreatedSuccess(Intent intent) {
                                    intent.putExtra("from", "shopping");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LaunchChatUtils.launchChatMessageActivity(OrderDetailsActivity.this, intent);

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
            /*if (object.getString("order_report_status").equals("")) {
                buttonReport.setVisibility(View.VISIBLE);
            } else {
                APPHelper.showLog("Report", object.getString("order_report_status"));
                textViewOrderStatus.setText(object.getString("order_report_status"));
                editTextReport.setText(object.getString("order_reason"));
                editTextReport.setVisibility(View.VISIBLE);
                editTextReport.setFocusable(false);
            }*/
            textViewAddress.setText(object.getString("order_address"));
            /*if(!object.getString("order_est").equals("0000-00-00")){
                textViewEst.setText(object.getString("order_est"));
            }*/
            textViewAmount.setText(formatter.format(Double.valueOf(object.getString("order_amt"))));
            textViewCourier.setText(object.getString("order_courier"));
            textViewOrderDate.setText(sdfNativeDate.format(sdfDatabaseDate.parse(object.getString("order_created"))));
            textViewProduct.setText(object.getString("pro_name") + " x" + object.getString("order_qty"));
            if (!object.getString("order_delivered").equals("0000-00-00")) {
                textViewDelivery.setText(object.getString("order_delivered"));
            }
            if (object.getString("order_approved").equals("1")) {
                textViewOrderStatus.setText("Delivery Confirmed");
            }
            buttonReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    editTextReport.setVisibility(View.VISIBLE);
                    editTextReport.setFocusable(true);
                    buttonSend.setVisibility(View.VISIBLE);*/
                    try {
                        if (getIntent().getStringExtra("role").equalsIgnoreCase("buyer")) {
                            if (!object.getString("order_report_status").equalsIgnoreCase("")) {
                                Intent intent = new Intent(getApplicationContext(), ReportMessagesActivity.class);
                                intent.putExtra("details", object.toString());
                                intent.putExtra("role", getIntent().getStringExtra("role"));
                                startActivity(intent);
                            } else {
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                //then we will inflate the custom alert dialog xml that we created
                                View dialogView = LayoutInflater.from(OrderDetailsActivity.this).inflate(R.layout.dialog_report, viewGroup, false);
                                final AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                                final Button buttonSend = dialogView.findViewById(R.id.buttonSend);
                                final EditText editTextReport = dialogView.findViewById(R.id.editTextReason);


                                builder.setView(dialogView);
                                buttonSend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String reason = editTextReport.getText().toString().trim();
                                        try {
                                            alertDialog.dismiss();
                                            Log.i("order", "clicked");
                                            new ReportOrder().execute(object.getString("order_id"), reason);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                alertDialog = builder.create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {

                                    }
                                });
                                alertDialog.show();

                            }
                        } else {
                            if (!object.getString("order_report_status").equalsIgnoreCase("")) {
                                Intent intent = new Intent(getApplicationContext(), ReportMessagesActivity.class);
                                intent.putExtra("details", object.toString());
                                intent.putExtra("role", getIntent().getStringExtra("role"));
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
            textViewSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                    if (APPHelper.checkSelf(getApplicationContext(), object)) {
                        try {
                            intent.putExtra("takeOrder", true);

                            intent.putExtra(ConversationUIService.USER_ID, object.getString("coas_id"));
                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }*/
                }
            });

            textViewBuyer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                    if (APPHelper.checkSelf(getApplicationContext(), object)) {
                        try {

                            intent.putExtra("takeOrder", true);

                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);

                            intent.putExtra(ConversationUIService.USER_ID, object.getString("coas_id"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);


                    }*/

                }
            });
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reason = editTextReport.getText().toString().trim();
                    try {
                        new ReportOrder().execute(object.getString("order_id"), reason);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            textViewProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), ProductDetailsActivity.class);
                    intent.putExtra("details", object.toString());

                    startActivity(intent);
                }
            });

            if (object.getString("order_status").equalsIgnoreCase("delivered")) {
                buttonReport.setVisibility(View.VISIBLE);
            } else {
                buttonReport.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
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

    class ReportOrder extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("order_id", strings[0]);
            map.put("reason", strings[1]);
            map.put("status", "Reported");
            return new RequestHandler().sendPostRequest(MAIN_URL + "report_order.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    object.put("order_report_status", "Reported");
                    Intent intent = new Intent(getApplicationContext(), ReportMessagesActivity.class);
                    intent.putExtra("details", object.toString());
                    intent.putExtra("role", getIntent().getStringExtra("role"));
                    startActivity(intent);
                   /* setResult(RESULT_OK);
                    finish();*/
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
