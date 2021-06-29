package com.coasapp.coas.general;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.AppSignatureHelper;
import com.coasapp.coas.utils.GetHash;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.GetRequestAsyncTask;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ForgotPasswordActivity extends AppCompatActivity implements APPConstants {

    LinearLayout layoutPhone, layoutPass, layoutOtp, layoutProgress;
    String phone, otp, entered_otp, password, newPass, countryCode = "1";
    BroadcastReceiver receiver;
    boolean flag = false;


    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayAdapter adapterCountries;

    GetRequestAsyncTask getRequestAsyncTask;

    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        public void taskEnd(String type, String response) {
            arrayListCountries.clear();
            arrayListCountriesSpinner.clear();
            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            try {
                if (type.equalsIgnoreCase("countries")) {
                    JSONArray arrayCountries = new JSONArray(response);
                    for (int i = 0; i < arrayCountries.length(); i++) {
                        JSONObject object = arrayCountries.getJSONObject(i);
                        arrayListCountries.add(object);
                        if(i==0){

                            arrayListCountriesSpinner.add( object.getString("country_name"));

                        }
                        else {
                            arrayListCountriesSpinner.add("(" + object.getString("std_code") + ") " + object.getString("country_name"));
                        }
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("countries", response).apply();
                    adapterCountries.notifyDataSetChanged();
                    spinnerCountry.setSelection(231);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error! Please try later", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };



    private AppCompatSpinner spinnerCountry;




    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-02-01 07:23:02 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        spinnerCountry = (AppCompatSpinner)findViewById( R.id.spinnerCountry );

        adapterCountries = new ArrayAdapter<>(this, R.layout.spinner_reg, arrayListCountriesSpinner);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapterCountries);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        findViews();
        layoutOtp = findViewById(R.id.layout_otp);
        layoutPass = findViewById(R.id.layout_confirm);
        layoutPhone = findViewById(R.id.layout_phone);
        layoutProgress = findViewById(R.id.layoutProgress);
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        EditText editTextOTP = findViewById(R.id.editTextOTP);
        EditText editTextPass = findViewById(R.id.editTextPass);
        EditText editTextCon = findViewById(R.id.editTextConPass);



        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

              if(flag == true) {
                  //String arr = arrayListCountries.get(position).toString();
                  arrayListCountriesSpinner.get(position);
                  try {
                      String ph = arrayListCountries.get(position).getString("std_code");
                     // editTextPhone.setText(ph);
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }

              //    Toast.makeText(ForgotPasswordActivity.this, arr, Toast.LENGTH_SHORT).show();

              }
                flag = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = editTextPhone.getText().toString();
                try {
                    countryCode = arrayListCountries.get(spinnerCountry.getSelectedItemPosition()).getString("std_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (phone.length() < 8) {
                    Toast.makeText(ForgotPasswordActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if(spinnerCountry.getSelectedItemPosition()==0){
                    APPHelper.showToast(getApplicationContext(), "Select Country");
                }
                else {
                    new Forgot().execute();
                }
            }
        });
        findViewById(R.id.buttonNext2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entered_otp = editTextOTP.getText().toString();


                if (otp.equals(entered_otp)) {
                    layoutOtp.setVisibility(View.GONE);
                    layoutPass.setVisibility(View.VISIBLE);

                } else {
                    APPHelper.showToast(getApplicationContext(), "Incorrect OTP");
                }
                //mVerification.verify(otp);
                //new Register().execute();

            }
        });
        findViewById(R.id.buttonNext3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = editTextPass.getText().toString();
                newPass = editTextCon.getText().toString();
                if (password.length() < 6) {
                    APPHelper.showToast(getApplicationContext(), "Enter 6 digit PIN");
                } else if (!password.equals(newPass)) {
                    APPHelper.showToast(getApplicationContext(), "Password Mismatch");

                } else {
                    new Reset().execute();
                }
                /*layoutPhone.setVisibility(View.GONE);
                layoutOtp.setVisibility(View.VISIBLE);*/
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                ///String code = parseCode(intent.getStringExtra("msg"));//Parse verification code
                String code = GetHash.getCode(intent.getStringExtra("msg"));
                APPHelper.showLog("SMSReceived", intent.getStringExtra("msg") + " " + code);
                editTextOTP.setText(code);//set code in edit text

            }
        };

        findViewById(R.id.textViewResend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendSMS().execute();

            }
        });


        getRequestAsyncTask = new GetRequestAsyncTask(getApplicationContext(), apiCallbacks);
        getRequestAsyncTask.setType("countries");
        getRequestAsyncTask.execute(MAIN_URL + "get_countries.php");
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("OTP");

        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        LocalBroadcastManager.getInstance(ForgotPasswordActivity.this).registerReceiver((receiver),
                intentFilter
        );
        APPHelper.showLog("SMR", "Start");
        /*registerReceiver(receiver, new IntentFilter("OTP")
        )*/
        ;
        //smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(ForgotPasswordActivity.this).unregisterReceiver(receiver);

        //unregisterReceiver(receiver);
        //smsVerifyCatcher.onStop();
    }

    class Forgot extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("phone", countryCode+phone);
            return new RequestHandler().sendPostRequest(MAIN_URL + "forgot_password.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);


                if (object.get("response_code").equals("1")) {
                    JSONObject objectUser = object.getJSONObject("User");
                    //countryCode = objectUser.getString("std_code");
                    new SendSMS().execute();

                } else {
                    Toast.makeText(getApplicationContext(), object.getString("response"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class SendSMS extends AsyncTask<Void, Void, String> {
        String hash = new AppSignatureHelper(getApplicationContext()).getAppSignatures().get(0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("phone", phone);
            map.put("country_code", countryCode);
            map.put("app_sign", hash);
            return new RequestHandler().sendPostRequest(MAIN_URL + "sendsms.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                otp = object.getString("otp");
                JSONObject objectNexmo = object.getJSONObject("nexmo");
                JSONArray arrayMsg = objectNexmo.getJSONArray("messages");
                if (arrayMsg.length() > 0) {
                    JSONObject object1 = arrayMsg.getJSONObject(0);
                    if (object1.get("status").equals("0")) {

                        layoutPhone.setVisibility(View.GONE);
                        layoutOtp.setVisibility(View.VISIBLE);

                        SmsRetrieverClient client = SmsRetriever.getClient(ForgotPasswordActivity.this /* context */);

// Starts SmsRetriever, which waits for ONE matching SMS message until timeout
// (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
// action SmsRetriever#SMS_RETRIEVED_ACTION.
                        Task<Void> task = client.startSmsRetriever();

// Listen for success/failure of the start Task. If in a background thread, this
// can be made blocking using Tasks.await(task, [timeout]);
                        task.addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Successfully started retriever, expect broadcast intent
                                // ...
                                layoutProgress.setVisibility(View.GONE);

                            }
                        });

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to start retriever, inspect Exception for more details
                                // ...
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class Reset extends AsyncTask<Void, Void, String> {
        String hash = new AppSignatureHelper(getApplicationContext()).getAppSignatures().get(0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("phone",countryCode+ phone);
            map.put("password", password);
            map.put("app_sign", hash);
            return new RequestHandler().sendPostRequest(MAIN_URL + "reset_password.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);


                if (object.get("response_code").equals("1")) {
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
