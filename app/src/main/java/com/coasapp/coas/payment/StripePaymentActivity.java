package com.coasapp.coas.payment;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;

import com.coasapp.coas.utils.MyPrefs;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardMultilineWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class StripePaymentActivity extends AppCompatActivity implements APPConstants {

    CardMultilineWidget mCardInputWidget;
    LinearLayout layoutProgress;
    ChargeUser chargeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_stripe_payment);
        layoutProgress = findViewById(R.id.layoutProgress);
        mCardInputWidget = findViewById(R.id.cardInputWidget);
        TextInputEditText editTextName = findViewById(R.id.editTextCardholderName);
        TextInputEditText editTextNameM = findViewById(R.id.editTextCardholderNameMiddle);
        TextInputEditText editTextNameL = findViewById(R.id.editTextCardholderNameLast);
        TextInputEditText editTextZip = findViewById(R.id.editTextZip);
        findViewById(R.id.buttonPay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentFocus().clearFocus();
                final Card cardToSave = mCardInputWidget.getCard();
                String zip = editTextZip.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String nameM = editTextNameM.getText().toString().trim();
                String nameL = editTextNameL.getText().toString().trim();
                if (cardToSave == null || !InputValidator.isName(name) || zip.equals("") || !InputValidator.isName(nameL)) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Card");
                    Log.i("Card", name + SPACE + nameL + SPACE + nameM + SPACE + zip);
                } else {
                    nameM += SPACE;
                    cardToSave.setName(name + SPACE + nameM + nameL);
                    cardToSave.setAddressZip(zip);
                    //Toast.makeText(StripePaymentActivity.this, "Valid", Toast.LENGTH_SHORT).show();
                    layoutProgress.setVisibility(View.VISIBLE);
                    String key = STRIPE_KEY_LIVE;
                    if (new MyPrefs(getApplicationContext(), APP_PREF).getString("payment_mode").equalsIgnoreCase("test"))
                        key = STRIPE_KEY;

                    Log.i("Card", key);
                    Stripe stripe = new Stripe(StripePaymentActivity.this, key);
                    stripe.createToken(
                            cardToSave,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    APPHelper.showLog("token", token.getId());
                                    //Toast.makeText(StripePaymentActivity.this, "Success: " + token.getId(), Toast.LENGTH_SHORT).show();
                                    // Send token to your server
                                    chargeUser = new ChargeUser();
                                    chargeUser.execute(token.getId());
                                }

                                public void onError(Exception error) {
                                    // Show localized error message
                                    layoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),
                                            error.getLocalizedMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            }
                    );
                }
            }
        });

        if (getIntent().hasExtra("role")) {
            findViewById(R.id.buttonRequest).setVisibility(View.VISIBLE);
            if (getIntent().getStringExtra("role").equalsIgnoreCase("driver")) {
                ((TextView) findViewById(R.id.textViewAmount)).append("\nFare: " + formatter.format(Double.valueOf(getIntent().getStringExtra("amount"))));

                ((TextView) findViewById(R.id.textViewAmount)).append("\nReceivable: " + formatter.format(Double.valueOf(getIntent().getStringExtra("receivable"))));

                findViewById(R.id.layoutCard).setEnabled(false);
                findViewById(R.id.buttonPay).setEnabled(false);
                findViewById(R.id.buttonPay).setBackgroundColor(Color.LTGRAY);
                for (int i = 0; i < ((LinearLayout) findViewById(R.id.layoutCard)).getChildCount(); i++) {
                    View child = ((LinearLayout) findViewById(R.id.layoutCard)).getChildAt(i);
                    child.setEnabled(false);
                }
                editTextNameL.setFocusable(false);
                editTextNameM.setFocusable(false);
                editTextName.setFocusable(false);
                editTextName.setEnabled(false);
                editTextNameM.setEnabled(false);
                editTextNameL.setEnabled(false);
            } else {
                ((TextView) findViewById(R.id.textViewAmount)).append("\nFare (including service charge " + getIntent().getDoubleExtra("charge", 5) + "%): " + formatter.format(Double.valueOf(getIntent().getStringExtra("amount")) / 100));

                findViewById(R.id.buttonRequest).setBackgroundColor(Color.LTGRAY);
                findViewById(R.id.buttonRequest).setEnabled(false);
            }
        }
        findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chargeUser != null)
            chargeUser.cancel(true);
    }

    class ChargeUser extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            String amt = getIntent().getStringExtra("amount");
            Log.i("amt", amt);
            if (amt.contains(".")) {
                String[] decimalAmt = amt.split("\\.");
                Log.i("amt", Arrays.toString(decimalAmt));
                if (decimalAmt[1].length() == 1) {
                    amt = amt + "0";
                }
            }
            map.put("method", "charge");
            map.put("amount", amt);
            map.put("source", strings[0]);
            map.put("description", getIntent().getStringExtra("desc"));
            return new RequestHandler().sendPostRequest(MAIN_URL_PAY + "charge.php", map);
            //return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response").equalsIgnoreCase("1")) {
                    String charge = object.getString("charge");
                    Intent intent = new Intent();
                    intent.putExtra("charge", charge);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    APPHelper.showToast(getApplicationContext(), object.getString("message"));
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
}
