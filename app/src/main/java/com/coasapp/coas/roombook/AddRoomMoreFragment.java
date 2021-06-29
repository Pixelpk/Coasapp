package com.coasapp.coas.roombook;


import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.general.WebViewActivity;
import com.coasapp.coas.utils.ChargeAsyncCallbacks;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.webservices.GetCommission;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomMoreFragment extends Fragment implements APPConstants {
    String date1 = "", date2 = "", date3 = "", date4 = "", from = "0", to = "1", negotiable = "No";

    String returned = "", bookType = "", status = "0", imgPath = "", ppn = "0.00", pph = "0.00", cleaning = "0.00", checkin = "0", checkout = "0";
    ImageView imageViewReg;
    File img1;
    ArrayList<JSONObject> arrayListDates1 = new ArrayList<>();
    ArrayList<JSONObject> arrayListDates2 = new ArrayList<>();
    ArrayList<JSONObject> arrayListTimes = new ArrayList<>();
    ArrayList<String> arrayListTimesSpinner = new ArrayList<>();
    ArrayList<JSONObject> arrayListTimes1 = new ArrayList<>();
    ArrayList<String> arrayListTimesSpinner1 = new ArrayList<>();
    BlockedDatesAdapter blockedDatesAdapter;
    LinearLayout layoutProgress, layoutHour;
    ArrayList<JSONObject> arrayListCountries = new ArrayList<>();
    ArrayList<String> arrayListCountriesSpinner = new ArrayList<>();
    ArrayList<String> arrayListCurrencySpinner = new ArrayList<>();
    ArrayAdapter<String> adapterCountries, adapterCurrency;
    Spinner spinnerCheckIn, spinnerCheckout;
    String currency = "USD";
    String blockedDates = "";

    public AddRoomMoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        blockedDatesAdapter = new BlockedDatesAdapter(arrayListDates1, getActivity(), getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerDates);
        recyclerView.setAdapter(blockedDatesAdapter);
        EditText editTextAmt = view.findViewById(R.id.editTextPph);
        EditText editTextClean = view.findViewById(R.id.editTextCleaning);
        Switch switchNeg = view.findViewById(R.id.switchNegotiate);
        RadioButton radioButtonNew = view.findViewById(R.id.radioButtonNew);
        RadioButton radioButtonRet = view.findViewById(R.id.radioButtonReturned);
        RadioButton radioButtonHour = view.findViewById(R.id.radioButtonHour);
        RadioButton radioButtonNight = view.findViewById(R.id.radioButtonNight);
        CheckBox checkBoxAgree = view.findViewById(R.id.checkBoxAgree);
        spinnerCheckIn = view.findViewById(R.id.spinnerCheckIn);
        spinnerCheckout = view.findViewById(R.id.spinnerCheckout);
        NumberPicker numberPickerCheckIn = view.findViewById(R.id.numberPickerCheckIn);
        NumberPicker numberPickerCheckout = view.findViewById(R.id.numberPickerCheckOut);
        Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        adapterCurrency = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListCurrencySpinner);
        adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapterCurrency);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences sharedPreferencesR = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        returned = sharedPreferencesR.getString("returned", "");
        bookType = sharedPreferencesR.getString("book_type", "");
        checkin = sharedPreferencesR.getString("room_checkin", "0");
        checkout = sharedPreferencesR.getString("room_checkout", "1");
        imgPath = sharedPreferencesR.getString("room_govt_id", "");
        pph = sharedPreferencesR.getString("priceperhour", "0.00");
        ppn = sharedPreferencesR.getString("pricepernight", "0.00");
        cleaning = sharedPreferencesR.getString("cleaning", "0.00");
        negotiable = sharedPreferencesR.getString("negotiable", "No");

        layoutHour = view.findViewById(R.id.layoutHour);
        if (sharedPreferencesR.getString("mode", "edit").equalsIgnoreCase("edit")) {
            checkBoxAgree.setChecked(true);
        }
        try {
            JSONArray arrayCountries = new JSONArray(sharedPreferences.getString("countries", "[]"));
            APPHelper.showLog("Tag", arrayCountries.toString());
            for (int i = 0; i < arrayCountries.length(); i++) {
                JSONObject object = arrayCountries.getJSONObject(i);
                arrayListCountries.add(object);
                //arrayListCurrencySpinner.add(object.getString("currency"));
            }
            arrayListCurrencySpinner.add("USD");

            adapterCurrency.notifyDataSetChanged();
            spinnerCurrency.setSelection(adapterCurrency.getPosition(sharedPreferencesR.getString("currency", sharedPreferences.getString("currency", "USD"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("h a");
        for (int i = 0; i < 24; i++) {
            JSONObject object = new JSONObject();
            try {
                object.put("hour", i);
                Date date1 = format.parse(i + ":00:00");
                object.put("12hour", format2.format(date1));
                arrayListTimes1.add(object);
                arrayListTimesSpinner1.add(object.getString("12hour"));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //arrayListHrsFrom.add(i);
        }

        ArrayAdapter<String> arrayAdapterFrom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner1);
        arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCheckIn.setAdapter(arrayAdapterFrom);
        ArrayAdapter<String> arrayAdapterTo = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner1);
        arrayAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCheckout.setAdapter(arrayAdapterTo);
        /*numberPickerCheckIn.setMinValue(0);
        numberPickerCheckIn.setMaxValue(23);
        numberPickerCheckout.setMinValue(0);
        numberPickerCheckout.setMaxValue(23);*/
        imageViewReg = view.findViewById(R.id.imageViewCarReg);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        blockedDatesAdapter.setDelClick(new BlockedDatesAdapter.DelClick() {
            @Override
            public void onDelClick(int i) {
                arrayListDates1.remove(i);
                blockedDatesAdapter.notifyDataSetChanged();
            }
        });

        switchNeg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    negotiable = "Yes";
                else
                    negotiable = "No";
            }
        });
        view.findViewById(R.id.radioButtonNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returned = "New";
            }
        });
        view.findViewById(R.id.radioButtonReturned).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returned = "Returned";
            }
        });
        view.findViewById(R.id.radioButtonHour).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookType = "Hourly";
                ppn = "0.00";
                layoutHour.setVisibility(View.GONE);
            }

        });

        view.findViewById(R.id.textViewBlocked).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if(bookType.equals("")){
                    Toast.makeText(getContext(),"Select Book Type",Toast.LENGTH_LONG).show();
                }
                else*/

                showDate();
            }

        });

        view.findViewById(R.id.radioButtonNight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookType = "Nightly";
                pph = "0.00";
                layoutHour.setVisibility(View.VISIBLE);
            }
        });

        view.findViewById(R.id.buttonCarReg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp(v);
            }
        });


        try {
            JSONArray arrayBook = new JSONArray(sharedPreferencesR.getString("bookings", "[]"));
            APPHelper.showLog("book", arrayBook.toString());
            if (arrayBook.length() > 0) {
                for (int i = 0; i < arrayBook.length(); i++) {
                    arrayListDates1.add(arrayBook.getJSONObject(i));
                }
                blockedDatesAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (returned.equalsIgnoreCase("New")) {
            radioButtonNew.setChecked(true);
        } else if (returned.equalsIgnoreCase("Returned")) {
            radioButtonRet.setChecked(true);
        }
        if (bookType.equalsIgnoreCase("Hourly")) {
            radioButtonHour.setChecked(true);
            editTextAmt.setText(pph);
            layoutHour.setVisibility(View.GONE);

        } else if (bookType.equalsIgnoreCase("Nightly")) {
            layoutHour.setVisibility(View.VISIBLE);
            radioButtonNight.setChecked(true);
            editTextAmt.setText(ppn);

        }
        if (!cleaning.equals("0.00"))
            editTextClean.setText(cleaning);
        APPHelper.showLog("book", bookType + pph + ppn);

        if (negotiable.equalsIgnoreCase("Yes")) {
            switchNeg.setChecked(true);
        }

        if (!imgPath.equals("")) {
            Glide.with(getActivity().getApplicationContext()).load(MAIN_URL_IMAGE + imgPath).into(imageViewReg);
        }
        spinnerCheckIn.setSelection(Integer.valueOf(checkin));
        spinnerCheckout.setSelection(Integer.valueOf(checkout));
        spinnerCurrency.setSelection(adapterCurrency.getPosition("room_currency"));
        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    currency = arrayListCountries.get(spinnerCurrency.getSelectedItemPosition()).getString("currency");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String amt = editTextAmt.getText().toString();
                cleaning = editTextClean.getText().toString();
                if (cleaning.equals(""))
                    cleaning = "0.00";
                if (bookType.equalsIgnoreCase("") || returned.equalsIgnoreCase("") || imgPath.equalsIgnoreCase("")) {
                    APPHelper.showToast(getContext(), "Fill all fields");
                } else if (!InputValidator.isValidPrice(String.valueOf(amt))) {
                    APPHelper.showToast(getContext(), "Enter Amount");

                } else if (!InputValidator.isValidNumber(String.valueOf(cleaning))) {
                    APPHelper.showToast(getContext(), "Enter Cleaning Price");

                } else if (!checkBoxAgree.isChecked()) {
                    APPHelper.showToast(getContext(), "Agree to the terms & Conditions");
                } else {

                    if (bookType.equalsIgnoreCase("Nightly")) {
                        pph = "0.00";
                        ppn = String.valueOf(amt);
                    } else {
                        ppn = "0.00";
                        pph = String.valueOf(amt);
                    }
                    APPHelper.showLog("book", bookType + pph + ppn);

                    if (bookType.equalsIgnoreCase("Hourly")) {
                        checkin = "0";
                        checkout = "0";
                    } else {
                        checkin = String.valueOf(spinnerCheckIn.getSelectedItemPosition());
                        checkout = String.valueOf(spinnerCheckout.getSelectedItemPosition());
                    }

                    JSONArray array = new JSONArray();
                    for (int i = 0; i < arrayListDates1.size(); i++) {
                        array.put(arrayListDates1.get(i));
                    }
                    blockedDates = array.toString();
                    APPHelper.showLog("block", blockedDates);
                    if (sharedPreferencesR.getString("mode", "edit").equalsIgnoreCase("add"))
                        new AddRoom().execute();
                    else
                        new EditRoom().execute();
                }

            }


        });
        int currentNightMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        String url="";
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                url ="termsconditionsrent";
                // Night mode is not active, we're using the light theme
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                url ="termsconditionsrentnight";
                // Night mode is active, we're using dark theme
                break;
        }
        WebView webView = view.findViewById(R.id.webViewTerms);
        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(baseUrlLocal +url+".html");
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
                    //startActivity(intent);
                    APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
                if (url.equalsIgnoreCase(baseUrlLocal + "rental.htm")) {
                    webView.goBack();
                    APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "short-term-city-rental-regulations/");

                    /*Intent intent = new Intent(getContext(), WebViewProfileActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "short-term-city-rental-regulations");
                    intent.putExtra("title", "Short Term City Rental Regulations");
                    startActivity(intent);*/
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
                    //startActivity(intent);
                    APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "terms-conditions/");

                }
                if (url.equalsIgnoreCase(baseUrlLocal + "rental.htm")) {
                    webView.goBack();
                    APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "short-term-city-rental-regulations/");

                    /*Intent intent = new Intent(getContext(), WebViewProfileActivity.class);
                    intent.putExtra("url", baseUrlLocal2 + "short-term-city-rental-regulations");
                    intent.putExtra("title", "Short Term City Rental Regulations");
                    startActivity(intent);*/
                }
            }
        });
        view.findViewById(R.id.textViewTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal2 + "regulation.htm");
                intent.putExtra("title", "User Regulations");
                APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "terms-conditions/");
            }
        });
        view.findViewById(R.id.textViewCommission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("url", baseUrlLocal2 + "short-term-city-rental-regulations");
                intent.putExtra("title", "Short Term City Rental Regulations");
                //startActivity(intent);
                APPHelper.launchChrome(getActivity(), APPConstants.baseUrlLocal2 + "short-term-city-rental-regulations");

            }
        });
        ChargeAsyncCallbacks chargeAsyncCallbacks = new ChargeAsyncCallbacks() {
            @Override
            public void onTaskStart() {
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTaskEnd(String result) {
                try {
                    JSONObject object1 = new JSONObject(result);
                    String charges = object1.getString("commission_value");
                    view.findViewById(R.id.textViewCommission).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.textViewCommission)).setText("Platform fee is  " + charges + "% for service fee");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                layoutProgress.setVisibility(View.GONE);
            }
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("type", "Room");
        new GetCommission(chargeAsyncCallbacks, map).execute();
    }


    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());

        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:

                        ((AddRoomActivity) getActivity()).checkPermissionStorage(0);

                        break;
                    case R.id.menu_gallery:
                        ((AddRoomActivity) getActivity()).checkPermissionStorage(1);
                        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), code[0]);
                        break;

                }

                return true;
            }
        });

        popupMenu.show();


    }

    public void addImage(String imageEncoded) {


        imgPath = imageEncoded;
        Glide.with(getActivity().getApplicationContext()).load(imgPath).into(imageViewReg);

        new UploadBill(0).execute();
    }


    public void showDate() {
        Calendar calendar = Calendar.getInstance();
        LayoutInflater li = LayoutInflater.from(getActivity());
        View confirmDialog = li.inflate(R.layout.dialog_room_dates, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box


        //Creating an alertdialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Adding our dialog box to the view of alert dialog
        builder.setView(confirmDialog);
        final AlertDialog alertDialog = builder.create();

        alertDialog.setCanceledOnTouchOutside(false);
        //Creating a LayoutInflater object for the dialog box

       /* WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        alertDialog.getWindow().setAttributes(lp);*/
        // initiateVerification(false);
        TextView textViewDate = confirmDialog.findViewById(R.id.textViewDate);
        TextView textViewDateTo = confirmDialog.findViewById(R.id.textViewDateTo);
        Spinner spinnerFrom = confirmDialog.findViewById(R.id.spinnerFrom);
        Spinner spinnerTo = confirmDialog.findViewById(R.id.spinnerTo);
       /* if(bookType.equals("Nightly")) {
            spinnerFrom.setVisibility(View.GONE);
            spinnerTo.setVisibility(View.GONE);
        }*/


        arrayListTimes.clear();
        arrayListTimesSpinner.clear();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("h a");
        for (int i = 0; i < 24; i++) {
            JSONObject object = new JSONObject();
            try {
                object.put("hour", i);
                Date date1 = format.parse(i + ":00:00");
                object.put("12hour", format2.format(date1));
                arrayListTimes.add(object);
                arrayListTimesSpinner.add(object.getString("12hour"));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //arrayListHrsFrom.add(i);
        }

        ArrayAdapter<String> arrayAdapterFrom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner);
        arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(arrayAdapterFrom);
        ArrayAdapter<String> arrayAdapterTo = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrayListTimesSpinner);
        arrayAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(arrayAdapterTo);
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                //d1 = true;
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                date1 = sdfNativeDate.format(calendar.getTime());
                date2 = sdfDatabaseDate.format(calendar.getTime());
                date3 = "";
                textViewDateTo.setText("Select Date");
                textViewDate.setText(date1);
            }
        };
        final DatePickerDialog.OnDateSetListener dateSetListener2 = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                //d1 = true;
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                date3 = sdfNativeDate.format(calendar.getTime());
                date4 = sdfDatabaseDate.format(calendar.getTime());
                textViewDateTo.setText(date3);
            }
        };
        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(new Date());
                DatePickerDialog aDatePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));

                aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                aDatePickerDialog.show();
            }
        });
        textViewDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog aDatePickerDialog = new DatePickerDialog(getActivity(), dateSetListener2, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));

                aDatePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                aDatePickerDialog.show();
            }
        });

        confirmDialog.findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fromH = 0, toH = 0;
                try {
                    /*if(bookType.equals("Hourly")) {
                        fromH = arrayListTimes.get(spinnerFrom.getSelectedItemPosition()).getInt("hour");
                        toH = (arrayListTimes.get(spinnerTo.getSelectedItemPosition()).getInt("hour")) - 1;
                    }
                    else {
                        fromH =spinnerCheckIn.getSelectedItemPosition();
                        toH = spinnerCheckout.getSelectedItemPosition();
                    }*/
                    fromH = arrayListTimes.get(spinnerFrom.getSelectedItemPosition()).getInt("hour");
                    toH = (arrayListTimes.get(spinnerTo.getSelectedItemPosition()).getInt("hour")) - 1;
                    if (toH < 0) toH = 23;
                    from = String.valueOf(fromH);
                    to = String.valueOf(toH);
                    APPHelper.showLog("Time", fromH + " " + toH);
                    if (fromH < 10) {
                        from = "0" + from;

                    }
                    if (toH < 10) {
                        to = "0" + to;
                    }
                    String fromDate = date2 + " " + from + ":00:00";
                    String toDate = date4 + " " + to + ":59:59";
                    JSONObject object = new JSONObject();

                    if (date1.equalsIgnoreCase("") || date3.equalsIgnoreCase("")) {
                        APPHelper.showToast(getContext(), "Select Date");
                    } else if ((date1.equals(date3)) && ((Integer.valueOf(to) <= Integer.valueOf(from)))) {
                        APPHelper.showToast(getContext(), "Invalid hour");
                    } else {
                        object.put("book_from_date", date2);
                        object.put("book_to_date", date4);
                        object.put("from_hour", from);
                        object.put("to_hour", Integer.parseInt(to) + 1);
                        object.put("from_date", fromDate);
                        object.put("to_date", toDate);
                        object.put("from_unix", APPHelper.getUnixTimeZone(fromDate, getActivity().getSharedPreferences(ROOM_DETAILS, 0).getString("roomTimeZone", "")));
                        object.put("to_unix", APPHelper.getUnixTimeZone(toDate, getActivity().getSharedPreferences(ROOM_DETAILS, 0).getString("roomTimeZone", "")));

                        arrayListDates1.add(object);
                        blockedDatesAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        alertDialog.show();

    }


    class UploadBill extends AsyncTask<Integer, Integer, String> {

        int index;

        public UploadBill(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_room_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", String.valueOf(System.currentTimeMillis()));
            UploadMultipart multipart = new UploadMultipart(getContext());
            res = multipart.multipartRequest(url, map, imgPath, "room", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    imgPath = jsonObject.getString("response");
                }
            } catch (JSONException e) {
                imgPath = "";
                imageViewReg.setImageResource(R.drawable.placeholder);
                e.printStackTrace();
            }

        }
    }

    class AddRoom extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            HashMap<String, String> map = new HashMap<>();
            map.put("room_name", sharedPreferences.getString("title", "New Room"));
            map.put("room_desc", sharedPreferences.getString("desc", "New Room"));
            map.put("pph", pph);
            map.put("ppn", ppn);
            map.put("type", sharedPreferences.getString("roomType", "General"));
            map.put("apt", sharedPreferences.getString("apt", ""));
            map.put("share", sharedPreferences.getString("roomAccType", "Private"));
            map.put("guests", sharedPreferences.getString("guests", "1"));
            map.put("bath_num", sharedPreferences.getString("bathNum", "1"));
            map.put("bath_type", sharedPreferences.getString("bathType", "General"));
            map.put("address", sharedPreferences.getString("roomAddress", "General"));
            map.put("city", sharedPreferences.getString("roomCity", "General"));
            map.put("state", sharedPreferences.getString("roomState", "General"));
            map.put("zip", sharedPreferences.getString("roomZip", "General"));
            map.put("country", sharedPreferences.getString("roomCountry", "General"));
            map.put("lat", sharedPreferences.getString("roomLat", "General"));
            map.put("lng", sharedPreferences.getString("roomLng", "General"));
            map.put("parties", sharedPreferences.getString("parties", "no"));
            map.put("events", sharedPreferences.getString("events", "no"));
            map.put("children", sharedPreferences.getString("children", "no"));
            //map.put("infants", sharedPreferences.getString("infants", "no"));
            map.put("pets", sharedPreferences.getString("pets", "no"));
            //map.put("surveillance", sharedPreferences.getString("surveillance", "no"));
            map.put("smoking", sharedPreferences.getString("smoking", "no"));
            //map.put("weapons", sharedPreferences.getString("weapons", "no"));
            map.put("bedrooms", sharedPreferences.getString("roomBedrooms", "[]"));
            //map.put("amenity_limits", sharedPreferences.getString("limits", "no"));
            map.put("rules", sharedPreferences.getString("rules", "no"));
            //map.put("terms", sharedPreferences.getString("terms", "no"));
            map.put("amenities", sharedPreferences.getString("roomAmenities", "[]"));
            map.put("timezone", sharedPreferences.getString("roomTimeZone", "GMT"));
            map.put("images", sharedPreferences.getString("images", "[]"));
            map.put("govt_id_image", imgPath);
            map.put("book_type", bookType);
            map.put("hoster", returned);
            map.put("blocked", blockedDates);
            map.put("checkin", checkin);
            map.put("checkout", checkout);
            map.put("currency", currency);
            map.put("negotiable", negotiable);
            map.put("cleaning", cleaning);
            map.put("govt_id_required", sharedPreferences.getString("govt_req", "No"));
            map.put("user_id", sharedPreferences2.getString("userId", "0"));
            APPHelper.showLog("book", map.toString());
            return new RequestHandler().sendPostRequest(MAIN_URL + "add_room.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    APPHelper.showToast(getContext(), jsonObject.getString("response"));
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    getActivity().setResult(RESULT_OK);
                    getActivity().finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class EditRoom extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            HashMap<String, String> map = new HashMap<>();
            map.put("room_id", sharedPreferences.getString("roomId", "0"));
            map.put("room_name", sharedPreferences.getString("title", "New Room"));
            map.put("room_desc", sharedPreferences.getString("desc", "New Room"));
            map.put("pph", pph);
            map.put("ppn", ppn);
            map.put("cleaning", cleaning);
            map.put("apt", sharedPreferences.getString("apt", ""));
            map.put("type", sharedPreferences.getString("roomType", "General"));
            map.put("share", sharedPreferences.getString("roomAccType", "Private"));
            map.put("guests", sharedPreferences.getString("guests", "1"));
            map.put("bath_num", sharedPreferences.getString("bathNum", "1"));
            map.put("bath_type", sharedPreferences.getString("bathType", "General"));
            map.put("address", sharedPreferences.getString("roomAddress", "General"));
            map.put("city", sharedPreferences.getString("roomCity", "General"));
            map.put("state", sharedPreferences.getString("roomState", "General"));
            map.put("zip", sharedPreferences.getString("roomZip", "General"));
            map.put("country", sharedPreferences.getString("roomCountry", "General"));
            map.put("lat", sharedPreferences.getString("roomLat", "General"));
            map.put("lng", sharedPreferences.getString("roomLng", "General"));
            map.put("parties", sharedPreferences.getString("parties", "no"));
            map.put("events", sharedPreferences.getString("events", "no"));
            map.put("children", sharedPreferences.getString("children", "no"));
            map.put("infants", sharedPreferences.getString("infants", "no"));
            map.put("pets", sharedPreferences.getString("pets", "no"));
            map.put("surveillance", sharedPreferences.getString("surveillance", "no"));
            map.put("smoking", sharedPreferences.getString("smoking", "no"));
            map.put("weapons", sharedPreferences.getString("weapons", "no"));
            map.put("bedrooms", sharedPreferences.getString("roomBedrooms", "[]"));
            map.put("amenity_limits", sharedPreferences.getString("limits", "no"));
            map.put("rules", sharedPreferences.getString("rules", "no"));
            map.put("terms", sharedPreferences.getString("terms", "no"));
            map.put("amenities", sharedPreferences.getString("roomAmenities", "[]"));
            map.put("images", sharedPreferences.getString("images", "[]"));
            map.put("timezone", sharedPreferences.getString("roomTimeZone", "GMT"));
            map.put("govt_id_image", imgPath);
            map.put("book_type", bookType);
            map.put("hoster", returned);
            map.put("blocked", blockedDates);
            map.put("checkin", checkin);
            map.put("checkout", checkout);
            map.put("currency", currency);
            map.put("negotiable", negotiable);
            map.put("govt_id_required", sharedPreferences.getString("govt_req", "No"));
            map.put("user_id", sharedPreferences2.getString("userId", "0"));
            APPHelper.showLog("book", map.toString());
            return new RequestHandler().sendPostRequest(MAIN_URL + "edit_room.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equalsIgnoreCase("1")) {
                    APPHelper.showToast(getContext(), jsonObject.getString("response"));
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    getActivity().setResult(RESULT_OK);
                    getActivity().finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
