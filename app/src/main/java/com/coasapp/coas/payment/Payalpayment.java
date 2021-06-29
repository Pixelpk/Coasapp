package com.coasapp.coas.payment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagingRequestHelper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.coasapp.coas.sendbird.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class Payalpayment extends AppCompatActivity {

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
        setContentView(R.layout.activity_payalpayment);

        amountET = findViewById(R.id.paypalcheckoutET);
        checkoutBtn = findViewById(R.id.paypalcheckoutBtn);

        new getToken().execute();

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPayment();
            }

            
        });
    }

    private void submitPayment() {

        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNonce = nonce.getNonce();

                if (!amountET.getText().toString().isEmpty()) {
                    amount = amountET.getText().toString();
                    paramsHash = new HashMap<>();
                    paramsHash.put("amount", amount);
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

        RequestQueue queue = Volley.newRequestQueue(Payalpayment.this);

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

            mDialog = new ProgressDialog(Payalpayment.this,android.R.style.Theme_DeviceDefault_Dialog);
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
}