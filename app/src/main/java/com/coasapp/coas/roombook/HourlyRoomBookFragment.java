package com.coasapp.coas.roombook;

//Syed Uzair Haider

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import com.coasapp.coas.shopping.CartActivity;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.GetAge;

import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.webservices.GetCharges;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.payment.StripePaymentActivity;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class HourlyRoomBookFragment extends Fragment implements APPConstants {

    Calendar calendar, calendar1, calendar2;

    String bookDate, bookDate1, bookDate2, checkInTime, checkoutTime, currentDate = "";
    TimePickerDialog mTimePicker;
    double newprice, unitprice, originalnewprice, cleaning, totalNewPrice;
    LinearLayout linearLayoutProgress;
    Button buttonBook;
    int checkIn, bookHours = 1, selectDate;
    String emName, emNum, adults, chidren, infants, guests, userId, govtId = "", timeZone = "";
    long unixTime1, unixTime2;
    String orderId = "0";
    ArrayList<JSONObject> arrayListTimes = new ArrayList<>();
    List<String> arrayListTimesSpinner = new ArrayList<>();
    ArrayList<String> arrayListHoursSpinner = new ArrayList<>();
    ArrayList<String> arrayListAdults = new ArrayList<>();
    NumberPicker numberPickerCheckIn, numberPickerCheckout;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    TextView textViewCheckInDate, textViewCheckoutDate;
    Spinner spinnerCheckIn;
    BookContactsAdapter bookContactsAdapter;
    ArrayList<HashMap<String, String>> arrayListContacts = new ArrayList<>();
    ArrayAdapter<String> arrayAdapterFrom;
    double charges;
    boolean spinnerAdultTouch = false;
    APIService apiService;
    APICallbacks apiCallbacks;


    private static final int REQUEST_CODE = 200;
    EditText amountET;
    Button checkoutBtn;

    static final String API_GET_TOKEN = "https://www.coasapp.com/paypal/braintree/main.php";
    final String API_CHECK_OUT = "https://www.coasapp.com/paypal/braintree/checkout.php";

    static String token;
    String amount;

    HashMap<String,String> paramsHash;

    public HourlyRoomBookFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        APPHelper.showLog("Book", "onCreateView");
        return inflater.inflate(R.layout.fragment_hourly_room_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        final String name1 = sharedPreferences.getString("firstName", "guest");
        final String name2 = sharedPreferences.getString("lastName", "guest");
        final String name = name1 + " " + name2;

        final String phone = sharedPreferences.getString("phone", "000000000");

        APPHelper.showLog("user", name + phone);
        APPHelper.showLog("Book", "onViewCreated");
        linearLayoutProgress = view.findViewById(R.id.layoutProgress);
        RecyclerView recyclerViewContacts = view.findViewById(R.id.recyclerViewBookContacts);
        textViewCheckInDate = view.findViewById(R.id.textViewDateCheckIn);
        textViewCheckoutDate = view.findViewById(R.id.textViewDateCheckout);
        TextView textViewPayable = view.findViewById(R.id.textViewPayable);
        final Spinner spinnerAdults = view.findViewById(R.id.spinnerAdults);
        spinnerCheckIn = view.findViewById(R.id.spinnerFrom);
        final Spinner spinnerHours = view.findViewById(R.id.spinnerHours);
        final Spinner spinnerChildren = view.findViewById(R.id.spinnerChildren);
        final Spinner spinnerInfant = view.findViewById(R.id.spinnerInfants);
        final EditText editTextEmName = view.findViewById(R.id.editTextEmName);
        final EditText editTextEmPhone = view.findViewById(R.id.editTextEmPhone);
        final TextView textViewAmt = view.findViewById(R.id.textViewAmount);
        final TextView textViewDate = view.findViewById(R.id.textViewDate);
        TextView textViewCheckIn = view.findViewById(R.id.textViewCheckIn);
        TextView textViewRoomRules = view.findViewById(R.id.textViewRules);
        TextView textViewPayRules = view.findViewById(R.id.textViewPayRules);
        TextView textViewCheckOut = view.findViewById(R.id.textViewCheckOut);
        APPHelper.setTerms(getActivity(),view.findViewById(R.id.layoutAgree));

        new getToken().execute();

        WebView webView = view.findViewById(R.id.webViewTerms);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal + "termsconditions.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    APPHelper.launchChrome(getActivity(),APPConstants.baseUrlLocal2+"terms-conditions/");

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equalsIgnoreCase(baseUrlLocal + "regulation.htm")) {
                    webView.goBack();
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                    intent.putExtra("title", "User Regulations");
                    APPHelper.launchChrome(getActivity(),APPConstants.baseUrlLocal2+"terms-conditions/");

                }
            }
        });


        try {
            JSONObject object = new JSONObject(getArguments().getString("details"));
            textViewRoomRules.setText(getArguments().getString("rules"));
            textViewPayRules.setText(getArguments().getString("terms"));
            unitprice = Double.parseDouble(object.getString("priceperhour"));
            cleaning = Double.parseDouble(object.getString("room_cleaning_fee"));
            timeZone = object.getString("room_timezone");
            TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
            calendar = Calendar.getInstance();

            calendar1 = calendar;
            selectDate = calendar.get(Calendar.DAY_OF_MONTH);
            currentDate = sdfDatabaseDate.format(calendar.getTime());
            if (object.getString("room_negotiable").equalsIgnoreCase("Yes")) {
                textViewPayRules.setText("Price/Hours Negotiable");
            } else {
                textViewPayRules.setText("Not Negotiable");
            }
            ArrayAdapter<String> arrayAdapterHours = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListHoursSpinner);
            arrayAdapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHours.setAdapter(arrayAdapterHours);
            arrayAdapterFrom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner);
            arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCheckIn.setAdapter(arrayAdapterFrom);
            ChargeAsyncCallbacks chargeAsyncCallbacks = new ChargeAsyncCallbacks() {
                @Override
                public void onTaskStart() {
                    linearLayoutProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTaskEnd(String result) {
                    try {
                        JSONObject object1 = new JSONObject(result);
                        charges = Double.valueOf(object1.getString("charge_percent")) /*/ 100*/;
                        addHours(calendar.get(Calendar.HOUR_OF_DAY) + 1);
                        for (int i = 1; i < 21; i++) {
                            arrayListHoursSpinner.add(String.valueOf(i));
                        }
                        arrayAdapterHours.notifyDataSetChanged();
                        /*totalNewPrice = unitprice+cleaning;
                        newprice = totalNewPrice + (totalNewPrice * charges);
                        originalnewprice = unitprice;
                        textViewAmt.setText(formatter.format(newprice));*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    linearLayoutProgress.setVisibility(View.GONE);
                }
            };

            HashMap<String, String> map = new HashMap<>();
            map.put("type", "renting");
            new GetCharges(chargeAsyncCallbacks, map).execute();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("h a");
            /*for (int i = 0; i < 24; i++) {
                JSONObject objectHours = new JSONObject();
                try {
                    objectHours.put("hour", i);
                    Date date1 = format.parse(i + ":00:00");
                    objectHours.put("12hour", format2.format(date1));
                    arrayListTimes.add(objectHours);
                    arrayListTimesSpinner.add(objectHours.getString("12hour"));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //arrayListHrsFrom.add(i);
            }*/
           /* arrayAdapterFrom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner);
            arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCheckIn.setAdapter(arrayAdapterFrom);*/
            //addHours(calendar.get(Calendar.HOUR_OF_DAY) + 1);

            Log.i("Guests", object.getString("room_TotalGuest"));
            for (int i = 1; i < (Integer.valueOf(object.getString("room_TotalGuest")) + 1); i++) {
                arrayListAdults.add(String.valueOf(i));
            }

            ArrayAdapter<String> arrayAdapterAdults = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListAdults);
            arrayAdapterAdults.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAdults.setAdapter(arrayAdapterAdults);

            bookContactsAdapter = new BookContactsAdapter(getContext(), arrayListContacts);
            recyclerViewContacts.setAdapter(bookContactsAdapter);
            bookContactsAdapter.notifyDataSetChanged();
            spinnerCheckIn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        checkIn = arrayListTimes.get(position).getInt("hour");

                        //textViewAmt.setText(object.getString("room_currency") + newprice);
                        textViewAmt.setText(formatter.format(totalNewPrice));
                        getUnixTime();
                        new CheckAvailable().execute(userId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    bookHours = Integer.parseInt(arrayListHoursSpinner.get(position));
                    //originalnewprice = bookHours * unitprice;

                    //newprice = originalnewprice + (newprice * charges);
                    newprice = (unitprice * bookHours) + cleaning;

                    //totalNewPrice = newprice + (newprice * charges);
                    double totalNewPrice1 = newprice + (/*newprice **/ charges);
                    totalNewPrice = Math.round(totalNewPrice1 * 100.0) / 100.0;
                    APPHelper.showLog("Charge", "" + totalNewPrice);
                    APPHelper.showLog("Charge", "" + ((unitprice * bookHours) + cleaning));
                    //textViewAmt.setText(object.getString("room_currency") + newprice);
                    textViewPayable.setText("Payable Amount:" + "\n" + formatter.format(unitprice) + " x " + bookHours + " hours " + "+" + "\nCleaning fee: " + formatter.format(cleaning) + "\nPlatform fee (" + formatter.format(charges) /** 100*/ + ") "/* + formatter.format(newprice * charges)*/);
                    textViewAmt.setText(formatter.format(totalNewPrice));

                    getUnixTime();
                    new CheckAvailable().execute(userId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerAdults.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    spinnerAdultTouch = true;
                    return false;
                }
            });
            HashMap<String, String> mapG = new HashMap<>();

            mapG.put("guest_name", name);
            mapG.put("guest_middle_name", "");
            mapG.put("guest_last_name", "");
            mapG.put("guest_phone", phone);
            try {
                mapG.put("guest_age", String.valueOf(GetAge.calcAge(sdfDatabaseDate.parse(sharedPreferences.getString("dob", "1900-01-01")))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            arrayListContacts.add(mapG);
            bookContactsAdapter.notifyDataSetChanged();
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
                            for (int i = spinnerAdults.getSelectedItemPosition(); i < size; i++) {
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
                    if (age.equals(""))
                        age = "0";
                    arrayListContacts.get(position).put("guest_age", age);

                }
            });

            buttonBook = view.findViewById(R.id.buttonPay);
      /*  numberPickerCheckIn = view.findViewById(R.id.numberPickerCheckIn);
        numberPickerCheckout = view.findViewById(R.id.numberPickerCheckOut);*/
            bookDate = sdfDatabaseDate.format(calendar.getTime());
            bookDate1 = sdfNativeDate.format(calendar.getTime());
            //textViewAmt.setText("$" + getArguments().getString("unitprice"));
            /*unitprice = Double.parseDouble(getArguments().getString("unitprice"));
            newprice = unitprice;*/

            textViewDate.setText(bookDate1);
        /*numberPickerCheckIn.setMinValue(0);
        numberPickerCheckIn.setMaxValue(23);
        numberPickerCheckout.setMinValue(0);
        numberPickerCheckout.setMaxValue(23);
        numberPickerCheckIn.setValue(calendar.get(Calendar.HOUR_OF_DAY) + 1);
        numberPickerCheckout.setValue(numberPickerCheckIn.getValue() + 1);*/
           /* getUnixTime();
            new CheckAvailable().execute(userId);*/
            final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    calendar1.set(Calendar.YEAR, year);
                    calendar1.set(Calendar.MONTH, monthOfYear);
                    calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    bookDate = sdfDatabaseDate.format(calendar1.getTime());
                    bookDate1 = sdfNativeDate.format(calendar1.getTime());
                    selectDate = dayOfMonth;

                    textViewDate.setText(bookDate1);
                    if (bookDate.equals(currentDate)) {
                        addHours(calendar.get(Calendar.HOUR_OF_DAY) + 1);
                    } else {
                        addHours(0);

                    }
                    spinnerCheckIn.setSelection(0);

                    try {
                        checkIn = arrayListTimes.get(spinnerCheckIn.getSelectedItemPosition()).getInt("hour");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   /* calendar2 = calendar;
                    calendar2.add(Calendar.DATE, 1);
                    bookDate2 = sdf2.format(calendar2.getTime());*/
                    APPHelper.showLog("Book", bookDate);
                    //d1 = true;
                    getUnixTime();
                    new CheckAvailable().execute(userId);
                }
            };


            textViewDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog aDatePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, calendar
                            .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    calendar.setTime(new Date());

                    aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    aDatePickerDialog.show();
                }
            });

            textViewCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        eReminderTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setE;
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();*/


                }
            });

        /*numberPickerCheckIn.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
               *//* if (newVal == 23) {
                    numberPickerCheckout.setValue(0);
                } else {
                    numberPickerCheckout.setValue(newVal + 1);
                }

*//*
                if (numberPickerCheckIn.getValue() < numberPickerCheckout.getValue()) {
                    newprice = unitprice * (numberPickerCheckout.getValue() - numberPickerCheckIn.getValue());
                } else {
                    newprice = unitprice * (24 - (numberPickerCheckIn.getValue() - numberPickerCheckout.getValue()));
                }
                textViewAmt.setText("$" + newprice);
                *//*int checkIn = numberPickerCheckIn.getValue();
                int checkout = numberPickerCheckout.getValue() - 1;
                String check1 = String.valueOf(checkIn);
                String check2 = String.valueOf(checkout);
                if (checkIn < 10) {
                    check1 = "0" + checkIn;
                }
                if (checkout < 10) {
                    check2 = "0" + checkout;
                }
                checkInTime = bookDate + " " + check1 + ":00:00.000";
                if (checkout <= checkIn) {
                    checkoutTime = bookDate2 + " " + check1 + ":59:59.000";
                } else {
                    checkoutTime = bookDate + " " + check2 + ":59:59.000";
                }
                APPHelper.showLog("Time", checkInTime);
                APPHelper.showLog("Time", checkoutTime);
                try {
                    Date date1 = sdf3.parse(checkInTime);
                    Date date2 = sdf3.parse(checkoutTime);
                    long unixTime1 = date1.getTime() / 1000;
                    long unixTime2 = date2.getTime() / 1000;

                    APPHelper.showLog("Time", String.valueOf(unixTime1));
                    APPHelper.showLog("Time", String.valueOf(unixTime2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }*//*
                getUnixTime();
                new CheckAvailable().execute(userId);
            }
        });

        numberPickerCheckout.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (numberPickerCheckIn.getValue() < numberPickerCheckout.getValue()) {
                    newprice = unitprice * (numberPickerCheckout.getValue() - numberPickerCheckIn.getValue());
                } else {
                    newprice = unitprice * (24 - (numberPickerCheckIn.getValue() - numberPickerCheckout.getValue()));
                }
                textViewAmt.setText("$" + newprice);
                *//*int checkIn = numberPickerCheckIn.getValue();
                int checkout = numberPickerCheckout.getValue() - 1;
                String check1 = String.valueOf(checkIn);
                String check2 = String.valueOf(checkout);
                if (checkIn < 10) {
                    check1 = "0" + checkIn;
                }
                if (checkout < 10) {
                    check2 = "0" + checkout;
                }
                checkInTime = bookDate + " " + check1 + ":00:00.000";
                if (checkout <= checkIn) {
                    checkoutTime = bookDate2 + " " + check1 + ":59:59.000";
                } else {
                    checkoutTime = bookDate + " " + check2 + ":59:59.000";
                }
                APPHelper.showLog("Time", checkInTime);
                APPHelper.showLog("Time", checkoutTime);
                try {
                    Date date1 = sdf3.parse(checkInTime);
                    Date date2 = sdf3.parse(checkoutTime);
                    long unixTime1 = date1.getTime() / 1000;
                    long unixTime2 = date2.getTime() / 1000;

                    APPHelper.showLog("Time", String.valueOf(unixTime1));
                    APPHelper.showLog("Time", String.valueOf(unixTime2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }*//*
                getUnixTime();
                new CheckAvailable().execute(userId);

            }
        });*/

            //((TextView)view.findViewById(R.id.textViewTerms)).setText(Html.fromHtml(getString(R.string.terms)));
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
                /*int checkIn = numberPickerCheckIn.getValue();
                int checkout = numberPickerCheckout.getValue() - 1;
                String check1 = String.valueOf(checkIn);
                String check2 = String.valueOf(checkout);
                if (checkIn < 10) {
                    check1 = "0" + checkIn;
                }
                if (checkout < 10) {
                    check2 = "0" + checkout;
                }
                checkInTime = bookDate + " " + check1 + ":00:00.000";
                if (checkout <= checkIn) {
                    checkoutTime = bookDate2 + " " + check1 + ":59:59.000";
                } else {
                    checkoutTime = bookDate + " " + check2 + ":59:59.000";
                }
                APPHelper.showLog("Time", checkInTime);
                APPHelper.showLog("Time", checkoutTime);
                try {
                    Date date1 = sdf3.parse(checkInTime);
                    Date date2 = sdf3.parse(checkoutTime);
                    long unixTime1 = date1.getTime() / 1000;
                    long unixTime2 = date2.getTime() / 1000;

                    APPHelper.showLog("Time", String.valueOf(unixTime1));
                    APPHelper.showLog("Time", String.valueOf(unixTime2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
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
                    } else if (emName.equalsIgnoreCase("") || !InputValidator.isValidMobile(emNum)) {
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
                    Date date4 = new Date(created * 1000L);
                    String dateCreated = sdfDatabaseDateTime.format(date4);
                    //new BookRoom().execute();
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
            map.put("book_type", "hour");
            map.put("govt_id", govtId);
            map.put("guests", guests);
            if (getArguments().getString("from").equalsIgnoreCase("link")) {
                map.put("book_id", getArguments().getString("book_id"));
                Log.i("map", map.toString());

                return new RequestHandler().sendPostRequest(MAIN_URL + "book_room_negotiable.php", map);

            } else {
                Log.i("map", map.toString());

                return new RequestHandler().sendPostRequest(MAIN_URL + "book_room_pay.php", map);

            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {

                  /*  orderId = jsonObject.getString("order_id");

                    Intent intent = new Intent(getContext(), StripePaymentActivity.class);
                    intent.putExtra("amount", String.valueOf(Math.round(totalNewPrice * 100)));
                    intent.putExtra("desc", "Room" + "_" + orderId);
                    startActivityForResult(intent, 100);
                    *//*Intent intent = new Intent(getContext(), BookingHistoryActivity.class);
                    startActivity(intent);*/
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
            map.put("room_id", getArguments().getString("room_id"));
            map.put("user_id", userId);
            map.put("amount1", String.valueOf(newprice));
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

    void getUnixTime() {

        String check1 = String.valueOf(checkIn);
        if (checkIn < 10) {
            check1 = "0" + checkIn;
        }
        checkInTime = bookDate + " " + check1 + ":00:00";

        /*String check2 = String.valueOf(checkout);
        if (checkIn < 10) {
            check1 = "0" + checkIn;
        }
        if (checkout < 10) {
            check2 = "0" + checkout;
        }
        checkInTime = bookDate + " " + check1 + ":00:00";

        if (checkout < checkIn) {
            checkoutTime = bookDate2 + " " + check1 + ":59:59";
        } else {
            checkoutTime = bookDate + " " + check2 + ":59:59";
        }*/
        try {
            Date date1 = sdfDatabaseDateTime.parse(checkInTime);
            Calendar calendarCheckin = Calendar.getInstance();
            Calendar calendarCheckout;
            calendarCheckin.setTime(date1);
            calendarCheckout = calendarCheckin;
            calendarCheckout.add(Calendar.HOUR_OF_DAY, bookHours - 1);
            calendarCheckout.add(Calendar.MINUTE, 59);
            calendarCheckout.add(Calendar.SECOND, 59);
            checkoutTime = sdfDatabaseDateTime.format(calendarCheckout.getTime());
            Date date2 = sdfDatabaseDateTime.parse(checkoutTime);
            //date2.setTime(date1.getTime() + (bookHours * 3600000));
            unixTime1 = APPHelper.getUnixTimeZone(checkInTime, timeZone);
            unixTime2 = APPHelper.getUnixTimeZone(checkoutTime, timeZone);
            Date date3 = new java.util.Date(unixTime1 * 1000L);
            Date date4 = new java.util.Date(unixTime2 * 1000L);
            APPHelper.showLog("TimeI", checkInTime);
            APPHelper.showLog("TimeO", checkoutTime);
            APPHelper.showLog("TimeUI", String.valueOf(unixTime1));
            APPHelper.showLog("TimeUO", String.valueOf(unixTime2));
            APPHelper.showLog("TimeIn", sdfDatabaseDateTime.format(date3));
            APPHelper.showLog("TimeOut", sdfDatabaseDateTime.format(date4));
            textViewCheckInDate.setText("CheckIn:\n" + sdfNativeDateTime.format(date1));
            textViewCheckoutDate.setText("Checkout:\n" + sdfNativeDateTime.format(date2));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addHours(int start) {
        arrayListTimes.clear();
        arrayListTimesSpinner.clear();
        arrayAdapterFrom.notifyDataSetChanged();
        for (int i = start; i < 24; i++) {
            JSONObject objectHours = new JSONObject();
            try {
                objectHours.put("hour", i);
                Date date1 = sdfDatabaseTime.parse(i + ":00:00");
                objectHours.put("12hour", sdfNativeTime.format(date1));
                arrayListTimes.add(objectHours);
                arrayListTimesSpinner.add(objectHours.getString("12hour"));
                APPHelper.showLog("Hour", "" + i);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            arrayAdapterFrom.notifyDataSetChanged();
            //arrayListHrsFrom.add(i);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TimeZone.setDefault(TimeZone.getDefault());
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

}
