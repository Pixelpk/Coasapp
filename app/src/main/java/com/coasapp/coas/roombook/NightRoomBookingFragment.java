package com.coasapp.coas.roombook;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.payment.StripePaymentActivity;
import com.coasapp.coas.shopping.CartActivity;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.GetAge;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.webservices.GetCharges;
import com.coasapp.coas.utils.DateFormats;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class NightRoomBookingFragment extends Fragment implements APPConstants {

    String bookDate, orderId = "0", bookType = "", bookDateCheckIn = "", bookDateCheckout = "", bookDate2, bookDate3, bookDate4;

    double unitprice, newprice, charges, originalnewprice, cleaning, totalNewPrice;
    String roomCheckIn, roomCheckout;
    LinearLayout linearLayoutProgress, layoutHour;
    Button buttonBook;
    long nights = 1, hours = 0, night1 = 0, night2 = 0;
    int bookHours = 0;
    int checkInH, checkoutH;
    Calendar calendar, calendar1, calendar2;
    long unixTime1, unixTime2;
    BookContactsAdapter bookContactsAdapter;
    ArrayList<HashMap<String, String>> arrayListContacts = new ArrayList<>();
    NumberPicker numberPickerCheckIn, numberPickerCheckout;
    ArrayList<String> arrayListAdults = new ArrayList<>();
    String timeZone = "";
    String emName, emNum, adults, chidren, infants, guests, userId, govtId = "";
    boolean hourBook;
    TextView textViewCheckIn, textViewCheckInDate, textViewCheckoutDate, textViewCheckout, textViewPayable, textViewAmt;
    int selectDate;
    DateFormats dateFormats = new DateFormats();
    boolean spinnerAdultTouch = false;



    private static final int REQUEST_CODE = 200;
    EditText amountET;
    Button checkoutBtn;

    static final String API_GET_TOKEN = "https://www.coasapp.com/paypal/braintree/main.php";
    final String API_CHECK_OUT = "https://www.coasapp.com/paypal/braintree/checkout.php";

    static String token;
    String amount;

    HashMap<String,String> paramsHash;

    public NightRoomBookingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        APPHelper.showLog("Book", "onCreateView");
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_night_room_booking, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_room_block, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.calendar_room_booked) {
            Intent intent = new Intent(getContext(), BookedDatesActivity.class);
            intent.putExtra("timezone", timeZone);
            intent.putExtra("room_id", getArguments().getString("room_id"));
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        APPHelper.showLog("Book", "onViewCreated");
        numberPickerCheckIn = view.findViewById(R.id.numberPickerCheckIn);
        numberPickerCheckout = view.findViewById(R.id.numberPickerCheckOut);
        layoutHour = view.findViewById(R.id.layoutHour);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        final String name1 = sharedPreferences.getString("firstName", "guest");
        final String name2 = sharedPreferences.getString("lastName", "guest");
        final String name = name1 + " " + name2;
        final String phone = sharedPreferences.getString("phone", "000000000");
        APPHelper.setTerms(getActivity(),view.findViewById(R.id.layoutAgree));
        WebView webView = view.findViewById(R.id.webViewTerms);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal + "termsconditions.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                Log.i("Url",url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();

                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    APPHelper.launchChrome(getActivity(),APPConstants.baseUrlLocal2+"terms-conditions/");
                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("Url",url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();

                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    //startActivity(intent);
                    APPHelper.launchChrome(getActivity(),APPConstants.baseUrlLocal2+"terms-conditions/");
                }
            }
        });
        APPHelper.showLog("Book", "onViewCreated");

        linearLayoutProgress = view.findViewById(R.id.layoutProgress);
        final Spinner spinnerAdults = view.findViewById(R.id.spinnerAdults);
        final Spinner spinnerChildren = view.findViewById(R.id.spinnerChildren);
        final Spinner spinnerInfant = view.findViewById(R.id.spinnerInfants);
        final EditText editTextEmName = view.findViewById(R.id.editTextEmName);
        final EditText editTextEmPhone = view.findViewById(R.id.editTextEmPhone);
        textViewAmt = view.findViewById(R.id.textViewAmount);
        textViewCheckIn = view.findViewById(R.id.textViewDate1);
        textViewCheckout = view.findViewById(R.id.textViewDate2);
        textViewCheckInDate = view.findViewById(R.id.textViewDateCheckIn);
        textViewCheckoutDate = view.findViewById(R.id.textViewDateCheckout);
        TextView textViewRoomRules = view.findViewById(R.id.textViewRules);
        TextView textViewPayRules = view.findViewById(R.id.textViewPayRules);
        buttonBook = view.findViewById(R.id.buttonPay);
        textViewPayable = view.findViewById(R.id.textViewPayable);

        RecyclerView recyclerViewContacts = view.findViewById(R.id.recyclerViewBookContacts);

        textViewRoomRules.setText(getArguments().getString("rules"));

       /* numberPickerCheckIn.setMinValue(0);
        numberPickerCheckIn.setMaxValue(23);
        numberPickerCheckout.setMinValue(0);
        numberPickerCheckout.setMaxValue(23);*/
        unitprice = Double.parseDouble(getArguments().getString("unitprice"));

        newprice = unitprice;
        //textViewAmt.setText("$" + newprice);
        textViewAmt.setText(formatter.format(newprice));


        String details = getArguments().getString("details");
        try {

            JSONObject object = new JSONObject(details);
            cleaning = Double.parseDouble(object.getString("room_cleaning_fee"));
            timeZone = object.getString("room_timezone");
            TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
            dateFormats.setTimezone(timeZone);
            calendar = Calendar.getInstance();
            calendar1 = Calendar.getInstance();
            calendar2 = Calendar.getInstance();

            if (object.getString("room_negotiable").equalsIgnoreCase("Yes")) {
                textViewPayRules.setText("Price/Hours Negotiable");
            } else {
                textViewPayRules.setText("Not Negotiable");
            }

            bookType = object.getString("room_book_type");
            hourBook = bookType.equalsIgnoreCase("hourly");
            roomCheckIn = object.getString("room_checkin");
            roomCheckout = object.getString("room_checkout");
            checkInH = Integer.valueOf(roomCheckIn);
            checkoutH = Integer.valueOf(roomCheckout);

           /* if (checkoutH > checkInH) {
                bookHours = checkoutH - checkInH;
            } else {

                bookHours = 24 - (checkInH - checkoutH);
            }*/
            Log.i("Guests", object.getString("room_TotalGuest"));


            for (int i = 1; i < Integer.valueOf(object.getString("room_TotalGuest")) + 1; i++) {
                arrayListAdults.add(String.valueOf(i));

            }
            ArrayAdapter<String> arrayAdapterAdults = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListAdults);
            arrayAdapterAdults.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAdults.setAdapter(arrayAdapterAdults);

            APPHelper.showLog("Book", "Hours: " + bookHours);
            //calendar.set(Calendar.HOUR_OF_DAY, checkInH);
            /*String check1 = String.valueOf(checkInH);
            if (checkInH < 10) {
                check1 = "0" + checkInH;
            }
            bookDate = sdfDatabaseDate.format(calendar.getTime());
            textViewCheckIn.setText(sdfNativeDate.format(calendar.getTime()));
            bookDateCheckIn = bookDate + " " + check1 + ":00:00";
            calendar1.setTime(sdfDatabaseDateTime.parse(bookDateCheckIn));
            calendar2 = calendar1;
            calendar2.add(Calendar.HOUR_OF_DAY, bookHours - 1);
            calendar2.add(Calendar.MINUTE, 59);
            calendar2.add(Calendar.SECOND, 59);
            bookDateCheckout = sdfDatabaseDateTime.format(calendar2.getTime());
            APPHelper.showLog("Book", "CheckIn: " + bookDateCheckIn + " " + "Checkout: " + bookDateCheckout);
            final Date[] dateCheckIn = {sdfDatabaseDateTime.parse(bookDateCheckIn)};

            textViewCheckInDate.setText("CheckIn: " + sdfNativeDateTime.format(dateCheckIn[0]) + " " + "Checkout: " + sdfNativeDateTime.format(calendar2.getTime()));
            getUnixTime();
            new CheckAvailable().execute(userId);*/

            //setDates();
           /* bookDate = sdfDatabaseDate.format(calendar1.getTime());
            textViewCheckIn.setText(sdfNativeDate.format(calendar1.getTime()));*/
            if (checkoutH <= checkInH)
                calendar2.add(Calendar.DATE, 1);
            calendar2.set(Calendar.HOUR_OF_DAY, checkoutH - 1);
            calendar2.set(Calendar.MINUTE, 59);
            calendar2.set(Calendar.SECOND, 59);

            ChargeAsyncCallbacks chargeAsyncCallbacks = new ChargeAsyncCallbacks() {
                @Override
                public void onTaskStart() {
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTaskEnd(String result) {
                    try {
                        JSONObject object1 = new JSONObject(result);

                        charges = Double.valueOf(object1.getString("charge_percent"))/* / 100*/;

                        //newprice = (unitprice * 1) + cleaning;

                        //totalNewPrice = newprice + (newprice * charges);

                        //textViewAmt.setText(formatter.format(totalNewPrice));
                        setDates();
                        //textViewPayable.setText("Payable Amount: " + "\n" + formatter.format(unitprice) + "+" + "\nCleaning fee: " + formatter.format(cleaning) + "\nPlatform fee (" + charges * 100 + "%): " + formatter.format(newprice * charges));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    linearLayoutProgress.setVisibility(View.GONE);
                }
            };

            HashMap<String, String> map = new HashMap<>();
            map.put("type", "renting");
            new GetCharges(chargeAsyncCallbacks, map).execute();
           /* numberPickerCheckIn.setValue(calendar.get(Calendar.HOUR_OF_DAY) + 1);
            numberPickerCheckout.setValue(numberPickerCheckIn.getValue() + 1);*/
            //getUnixTime();
            bookContactsAdapter = new BookContactsAdapter(getContext(), arrayListContacts);

            recyclerViewContacts.setAdapter(bookContactsAdapter);
            bookContactsAdapter.notifyDataSetChanged();
            spinnerAdults.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    spinnerAdultTouch = true;
                    return false;
                }
            });
            HashMap<String, String> mapG = new HashMap<>();

            mapG.put("guest_name", name);
            mapG.put("guest_phone", phone);
            mapG.put("guest_middle_name", "");
            mapG.put("guest_last_name", "");
            try {
                mapG.put("guest_age", String.valueOf(GetAge.calcAge(sdfDatabaseDate.parse(sharedPreferences.getString("dob", "1900-01-01")))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            arrayListContacts.add(mapG);
            spinnerAdults.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int size = arrayListContacts.size();

                    if (spinnerAdultTouch) {
                        /*arrayListContacts.clear();
                        bookContactsAdapter.notifyDataSetChanged();*/
                        spinnerAdultTouch = false;
                        Log.i("GuestNum", "Select");

                        String[] stringsDob = sharedPreferences.getString("dob", "1900-01-01").split("-");

                        int year = Integer.parseInt(stringsDob[0]);
                        int month = Integer.parseInt(stringsDob[1]);
                        int day = Integer.parseInt(stringsDob[2]);
                        Log.i("GuestNum", "" + (spinnerAdults.getSelectedItemPosition() + 1));
                        Log.i("GuestNum", "" + size);
                        if (spinnerAdults.getSelectedItemPosition() + 1 > size) {
                            for (int i = 0; i < ((spinnerAdults.getSelectedItemPosition() + 1) - size); i++) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("guest_name", "");
                                map.put("guest_middle_name", "");
                                map.put("guest_last_name", "");
                                map.put("guest_phone", "");
                                map.put("guest_age", "");
                                arrayListContacts.add(map);
                            }

                        } else if (spinnerAdults.getSelectedItemPosition() + 1 < size) {
                            Log.i("GuestNumR", "" + (spinnerAdults.getSelectedItemPosition() + 1));
                            Log.i("GuestNumR", "" + size);
                            for (int i = spinnerAdults.getSelectedItemPosition() ; i < size; i++) {
                                arrayListContacts.remove(i);
                            }
                            Log.i("GuestNumR1", "" + (spinnerAdults.getSelectedItemPosition() + 1));
                            Log.i("GuestNumR1", "" + arrayListContacts.size());
                        }
                        bookContactsAdapter.notifyDataSetChanged();

                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerAdultTouch = false;

                }
            });
            bookContactsAdapter.setOnAdapterItemsClick(new BookContactsAdapter.OnAdapterItemsClick() {
                @Override
                public void onDelClick(int i) {
                    spinnerAdultTouch = false;
                    arrayListContacts.remove(i);
                    bookContactsAdapter.notifyDataSetChanged();
                    spinnerAdults.setSelection(arrayListContacts.size() - 1);
                }
            });
            bookContactsAdapter.setOnNameChanged(new BookContactsAdapter.OnNameChanged() {
                @Override
                public void onNameChanged(int position, String name) {
                    arrayListContacts.get(position).put("guest_name", name);
                    //bookContactsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onMNameChanged(int position, String name) {
                    arrayListContacts.get(position).put("guest_middle_name", name);
                }

                @Override
                public void onLNameChanged(int position, String name) {
                    arrayListContacts.get(position).put("guest_last_name", name);
                }
            });

            bookContactsAdapter.setOnPhoneChanged(new BookContactsAdapter.OnPhoneChanged() {
                @Override
                public void onPhoneChanged(int position, String phone) {
                    arrayListContacts.get(position).put("guest_phone", phone);
                    //bookContactsAdapter.notifyDataSetChanged();
                }
            });
            bookContactsAdapter.setOnAgeChanged(new BookContactsAdapter.OnAgeChanged() {
                @Override
                public void onAgeChanged(int position, String age) {
                    Log.d("Guest", age);
                    if (age.equals(""))
                        age = "0";
                    arrayListContacts.get(position).put("guest_age", age);

                }
            });

           /* textViewCheckIn.setText(bookDate1);
            textViewCheckOut.setText(bookDate3);
            APPHelper.showLog("Book", bookDate + " " + bookDate2);*/
            //getDifferenceDays(calendar.getTime(), calendar2.getTime());

            final DatePickerDialog.OnDateSetListener dateSetListener1 = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    //addNights();
                    // TODO Auto-generated method stub
                    calendar1.set(Calendar.YEAR, year);
                    calendar1.set(Calendar.MONTH, monthOfYear);
                    calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar1.set(Calendar.HOUR_OF_DAY, checkInH);
                    calendar2.setTime(calendar1.getTime());
                    if(checkInH>checkoutH)
                    calendar2.add(Calendar.DATE, 1);
                    bookDate = dateFormats.getFormatDateDb().format(calendar1.getTime());
                    selectDate = dayOfMonth;
                    setDatesFromCal();
                    //d1 = true;
                }

            };

            final DatePickerDialog.OnDateSetListener dateSetListener2 = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub

                    calendar2.set(Calendar.YEAR, year);
                    calendar2.set(Calendar.MONTH, monthOfYear);
                    calendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar2.set(Calendar.HOUR_OF_DAY, checkoutH - 1);
                    calendar2.set(Calendar.MINUTE, 59);
                    calendar2.set(Calendar.SECOND, 59);
                    bookDate2 = dateFormats.getFormatDateDb().format(calendar2.getTime());
                    bookDate3 = dateFormats.getFormatDateDb().format(calendar2.getTime());
                    APPHelper.showLog("Book", bookDate + " " + bookDate2);

                    setDatesFromCal();
                    APPHelper.showLog("Book", bookDate + " " + bookDate2);
                    //d1 = true;

                    /*try {
                        hours = getHrs(sdf2.parse(bookDate), sdf2.parse(bookDate2));
                        nights = getDifferenceDays(sdf2.parse(bookDate), sdf2.parse(bookDate2));
                        if (bookType.equalsIgnoreCase("Hourly")) {
                            newprice = unitprice * hours;
                        } else {
                            newprice = unitprice * (nights + night1 + night2);

                        }
                        textViewAmt.setText("$" + newprice);
                        getUnixTime();
                        new CheckAvailable().execute(userId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }*/
                }
            };


            textViewCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog aDatePickerDialog = new DatePickerDialog(
                            getActivity(), dateSetListener1,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

                    aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    aDatePickerDialog.show();
                }
            });
            textViewCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();


                    calendar = calendar1;

                    try {
                        calendar.setTime(dateFormats.getFormatDateDb().parse(bookDate));
                        if (checkoutH <= checkInH)
                            calendar.add(Calendar.DATE, 1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DatePickerDialog aDatePickerDialog = new DatePickerDialog(
                            getActivity(), dateSetListener2,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)

                    );
                    aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    aDatePickerDialog.show();
                }
            });
            ((TextView) view.findViewById(R.id.textViewTerms)).setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView) view.findViewById(R.id.textViewTerms)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    startActivity(intent);
                }
            });
            CheckBox checkBox = view.findViewById(R.id.checkBoxAgree);
            buttonBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean guestValid = true, guestAdults = true;
                    guests = new Gson().toJson(arrayListContacts);

                    getUnixTime();
                    emName = editTextEmName.getText().toString().trim();
                    emNum = editTextEmPhone.getText().toString().trim();
                    adults = String.valueOf(spinnerAdults.getSelectedItemPosition() + 1);
                    chidren = String.valueOf(spinnerChildren.getSelectedItemPosition());
                    infants = String.valueOf(spinnerInfant.getSelectedItemPosition());
                    HashMap<String, String> mapG = arrayListContacts.get(0);
                    Log.i("Map",mapG.toString());
                    if (mapG.get("guest_name").equalsIgnoreCase("") || (mapG.get("guest_last_name").equalsIgnoreCase("") || !InputValidator.isValidMobile(mapG.get("guest_phone"))) || Integer.parseInt(mapG.get("guest_age")) < 18) {
                        guestAdults = false;
                    }
                    if (arrayListContacts.size() > 1) {
                        for (int i = 1; i < arrayListContacts.size(); i++) {
                            HashMap<String, String> mapG1 = arrayListContacts.get(i);
                            if (mapG1.get("guest_name").equalsIgnoreCase("") || (mapG1.get("guest_last_name").equalsIgnoreCase(""))) {
                                guestValid = false;
                            }
                        }
                    }
                    if (!guestAdults) {
                        APPHelper.showToast(getContext(), "Enter Visitor Details (Must be above 18)");
                    } else if (!guestValid) {
                        APPHelper.showToast(getContext(), "Enter names of visitors");
                    } else
                    if (emName.equalsIgnoreCase("") || !InputValidator.isValidMobile(emNum)) {
                        Log.i("Em", String.valueOf(emNum.length()));
                        APPHelper.showToast(getContext(), "Enter emergency Contact Number & Name");

                    } else if (!checkBox.isChecked()) {
                        view.findViewById(R.id.nestedScrollView).post(new Runnable() {
                            @Override
                            public void run() {
                                ((NestedScrollView) view.findViewById(R.id.nestedScrollView)).fullScroll(View.FOCUS_DOWN);
                            }
                        });
                        APPHelper.showToast(getContext(), "Agree to terms and conditions");
                    } else {
                        guests = new Gson().toJson(arrayListContacts);
                        try {
                            if (object.getString("govt_id_required").equalsIgnoreCase("yes")) {
                                startActivityForResult(new Intent(getContext(), UploadGovtIDActivity.class), 500);
                            } else {
                                Log.d("guests", guests + " " + String.valueOf(Math.round(newprice)));

                                new BookRoom().execute();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /*private void submitPayment() {

        //  Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(getActivity()),REQUEST_CODE);

    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {

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
                    String dateCreated = dateFormats.getFormatDateTimeDb().format(date4);
                    new UpdatePayment().execute(txnId, balanceTxn, objectSource.toString(), dateCreated);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 500) {
                govtId = data.getStringExtra("image");
                new BookRoom().execute();

            }
            else if(requestCode == 200)
            {
                //   Toast.makeText(this, String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                //  Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();


                    if (resultCode == RESULT_OK) {
                        DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                        PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                        String strNonce = nonce.getNonce();

                        if (!String.valueOf(newprice).isEmpty()) {
                            amount = String.valueOf(newprice);
                            Toast.makeText(getActivity(), amount, Toast.LENGTH_SHORT).show();
                            // amount = textViewTotal.getText().toString();
                            String value = amount;
                            value = value.substring(1);
                            //    Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
                            paramsHash = new HashMap<>();
                            paramsHash.put("amount",amount);
                            paramsHash.put("nonce", strNonce);

                            sendPayments();

                        } else {
                            Toast.makeText(getContext(), "Please Enter an amount", Toast.LENGTH_SHORT).show();
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(getContext(), "User Cancelled the Request", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                        Log.d("EDMT_ERROR", error.toString());
                    }

            }
        }
    }

    class CheckAvailable extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", strings[0]);
            map.put("room_id", getArguments().getString("room_id"));
            map.put("checkin", String.valueOf(unixTime1));
            map.put("checkout", String.valueOf(unixTime2));

            APPHelper.showLog("room", String.valueOf(map));
            return new RequestHandler().sendPostRequest(MAIN_URL + "check_room_available.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    buttonBook.setText("Room available - Click to Book");
                    buttonBook.setEnabled(true);
                } else {
                    buttonBook.setText(jsonObject.getString("response"));
                    buttonBook.setEnabled(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class BookRoom extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("checkin", String.valueOf(unixTime1));
            map.put("checkout", String.valueOf(unixTime2));
            map.put("amount", String.valueOf(totalNewPrice));
            map.put("room_id", getArguments().getString("room_id"));
            map.put("em_name", emName);
            map.put("em_phone", emNum);
            map.put("adults", adults);
            map.put("children", chidren);
            map.put("infants", infants);
            map.put("book_type", "night");
            map.put("govt_id", govtId);
            map.put("guests", guests);

            if (getArguments().getString("from").equalsIgnoreCase("link")) {
                map.put("book_id", getArguments().getString("book_id"));
                return new RequestHandler().sendPostRequest(MAIN_URL + "book_negotiable_room.php", map);
            } else {
                return new RequestHandler().sendPostRequest(MAIN_URL + "book_room_pay.php", map);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    /*orderId = jsonObject.getString("order_id");
                    Intent intent = new Intent(getContext(), StripePaymentActivity.class);
                    intent.putExtra("amount", String.valueOf(Math.round(totalNewPrice * 100)));
                    Log.i("amt", String.valueOf(Math.round(totalNewPrice * 100)));
                    //intent.putExtra("amount", String.valueOf(totalNewPrice * 100));
                    intent.putExtra("desc", "Room" + "_" + orderId);
                    startActivityForResult(intent, 100);*/
                    submitPayment();


                } else {
                    buttonBook.setText(jsonObject.getString("response"));
                    buttonBook.setEnabled(false);
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
            map.put("amount", String.valueOf(totalNewPrice));
            map.put("amount1", String.valueOf(newprice));
            map.put("room_id", getArguments().getString("room_id"));
            map.put("user_id", userId);
            map.put("order_id", orderId);
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_room_payment.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearLayoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                buttonBook.setText("Room Booked");
                buttonBook.setEnabled(false);
                if (object.getString("response_code").equalsIgnoreCase("1")) {
                    TimeZone.setDefault(TimeZone.getDefault());
                    Intent intent = new Intent(getContext(), BookingHistoryActivity.class);
                    intent.putExtra("checkout", true);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public long getDifferenceDays(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        Log.d("Nights", dateFormats.getFormatDateTimeDb().format(d1) + " " + dateFormats.getFormatDateTimeDb().format(d2));

        Log.d("Nights", String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
        long nights = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (nights == 0) {
            return 1;
        }
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    void getUnixTime() {
        Date date1, date2;
        try {

            date1 = dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn);
            date2 = dateFormats.getFormatDateTimeDb().parse(bookDateCheckout);


            unixTime1 = APPHelper.getUnixTimeZone(bookDateCheckIn, timeZone);
            unixTime2 = APPHelper.getUnixTimeZone(bookDateCheckout, timeZone);
            APPHelper.showLog("Time", String.valueOf(date1));
            APPHelper.showLog("Time", String.valueOf(date2));
            APPHelper.showLog("TimeU", String.valueOf(unixTime1));
            APPHelper.showLog("TimeU", String.valueOf(unixTime2));
            Date date3 = new java.util.Date(unixTime1 * 1000L);
            Date date4 = new java.util.Date(unixTime2 * 1000L);
// the format of your date
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            APPHelper.showLog("Time1", sdf.format(date3));
            APPHelper.showLog("Time1", sdf.format(date4));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        hours = elapsedHours;
        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
    }

    public long getHrs(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        hours = elapsedHours;
        APPHelper.showLog("Hours", "" + elapsedHours);
        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
        return elapsedHours;
    }

    void setDates() {
        String check1 = String.valueOf(checkInH);
        if (checkInH < 10) {
            check1 = "0" + checkInH;
        }
        try {
            bookDate = dateFormats.getFormatDateDb().format(calendar1.getTime());
            textViewCheckIn.setText(dateFormats.getFormatDateDb().format(calendar1.getTime()));
            textViewCheckout.setText(dateFormats.getFormatDateDb().format(calendar2.getTime()));
            bookDateCheckIn = bookDate + " " + check1 + ":00:00";
            calendar1.setTime(dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn));
            //calendar2 = calendar1;
            /*calendar2.add(Calendar.HOUR_OF_DAY, bookHours - 1);
            calendar2.add(Calendar.MINUTE, 59);
            calendar2.add(Calendar.SECOND, 59);*/
            bookDateCheckout = dateFormats.getFormatDateTimeDb().format(calendar2.getTime());
            APPHelper.showLog("Book", "CheckIn: " + bookDateCheckIn + " " + "Checkout: " + bookDateCheckout);
            final Date dateCheckIn = dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn);
            final Date dateCheckout = calendar2.getTime();
            nights = getDifferenceDays(dateCheckIn, dateCheckout);
            textViewCheckInDate.setText("CheckIn:\n" + dateFormats.getFormatDateTimeNative().format(dateCheckIn));
            textViewCheckoutDate.setText("Checkout:\n" + dateFormats.getFormatDateTimeNative().format(dateCheckout));

            getUnixTime();
            newprice = (unitprice * nights) + cleaning;

            double totalNewPrice1 = newprice + (/*newprice **/ charges);
            totalNewPrice = Math.round(totalNewPrice1 * 100.0) / 100.0;
            APPHelper.showLog("Charge", "" + totalNewPrice1);
            APPHelper.showLog("Charge", "" + ((unitprice * nights) + cleaning));
            //textViewAmt.setText(object.getString("room_currency") + newprice);
            textViewPayable.setText("Payable Amount: " + "\n" + formatter.format(unitprice) + " x " + nights + " nights +" + "\nCleaning fee: " + formatter.format(cleaning) + "\nPlatform fee (" + formatter.format(charges)/* * 100*/ + ") " /*+ formatter.format(newprice * charges)*/);
            textViewAmt.setText(formatter.format(totalNewPrice));
            new CheckAvailable().execute(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void setDatesFromCal() {
        String check1 = String.valueOf(checkInH);
        if (checkInH < 10) {
            check1 = "0" + checkInH;
        }
        try {
            textViewCheckout.setText(dateFormats.getFormatDateDb().format(calendar2.getTime()));
            bookDateCheckIn = bookDate + " " + check1 + ":00:00";
            textViewCheckIn.setText(dateFormats.getFormatDateDb().format((dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn))));
            calendar1.setTime(dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn));
            //calendar2 = calendar1;
            calendar2.set(Calendar.HOUR_OF_DAY, checkoutH - 1);
            calendar2.set(Calendar.MINUTE, 59);
            calendar2.set(Calendar.SECOND, 59);
            bookDateCheckout = dateFormats.getFormatDateTimeDb().format(calendar2.getTime());
            APPHelper.showLog("Book", "CheckIn: " + bookDateCheckIn + " " + "Checkout: " + bookDateCheckout);
            final Date dateCheckIn = dateFormats.getFormatDateTimeDb().parse(bookDateCheckIn);
            final Date dateCheckout = dateFormats.getFormatDateTimeDb().parse(bookDateCheckout);
            nights = getDifferenceDays(dateCheckIn, dateCheckout);
            textViewCheckInDate.setText("CheckIn:\n" + dateFormats.getFormatDateTimeNative().format(dateCheckIn));
            textViewCheckoutDate.setText("Checkout:\n" + dateFormats.getFormatDateTimeNative().format(dateCheckout));

            getUnixTime();
            newprice = (unitprice * nights) + cleaning;

            double totalNewPrice1 = newprice + (/*newprice **/ charges);
            totalNewPrice = Math.round(totalNewPrice1 * 100.0) / 100.0;
            APPHelper.showLog("Charge", "" + totalNewPrice1);
            APPHelper.showLog("Charge", "" + ((unitprice * nights) + cleaning));
            //textViewAmt.setText(object.getString("room_currency") + newprice);
            textViewPayable.setText("Payable Amount:" + "\n" + formatter.format(unitprice) + " x " + nights + " nights +" + "\nCleaning fee: " + formatter.format(cleaning) + "\nPlatform fee (" + formatter.format(charges)/** 100*/ + ") " /*+ formatter.format(newprice * charges)*/);
            textViewAmt.setText(formatter.format(totalNewPrice));
            new CheckAvailable().execute(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void addNights() {
        night1 = 0;
        night2 = 0;
        if (checkInH < Integer.valueOf(roomCheckIn))
            night1++;
        if (checkoutH > Integer.valueOf(roomCheckout))
            night2++;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TimeZone.setDefault(TimeZone.getDefault());
    }

    void addGuests() {

    }





    private void submitPayment() {

        //  Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(getContext()),REQUEST_CODE);

    }

  /*  @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }*/

    private void sendPayments() {

        RequestQueue queue = Volley.newRequestQueue(getContext());

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

            mDialog = new ProgressDialog(getContext(), android.R.style.Theme_DeviceDefault_Dialog);
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
                    getActivity().runOnUiThread(new Runnable() {
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
