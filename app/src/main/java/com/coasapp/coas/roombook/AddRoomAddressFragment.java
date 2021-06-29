package com.coasapp.coas.roombook;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;

import com.coasapp.coas.utils.GetRequestAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomAddressFragment extends Fragment implements APPConstants {

    EditText editTextAddress, editTextZip, editTextCountry, editTextState, editTextCity;
    Geocoder geocoder;
    String postal, country, city, state, timezone="GMT";
    double lat, lng;

    APICallbacks apiCallbacks;

    public AddRoomAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        geocoder = new Geocoder(getContext());
        Button buttonNext = view.findViewById(R.id.buttonNext);
        final Spinner spinnerRoomType = view.findViewById(R.id.spinnerType);
        final Spinner spinnerShared = view.findViewById(R.id.spinnerShare);
        final Spinner spinnerGuests = view.findViewById(R.id.spinnerGuests);
        final Spinner spinnerBedrooms = view.findViewById(R.id.spinnerBedrooms);
        final Spinner spinnerBathroomType = view.findViewById(R.id.spinnerBathroomType);
        final Spinner spinnerNumBath = view.findViewById(R.id.spinnerNumBath);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        EditText editTextApt = view.findViewById(R.id.editTextSuit);
        editTextZip = view.findViewById(R.id.editTextZip);
        editTextCountry = view.findViewById(R.id.editTextCountry);
        editTextState = view.findViewById(R.id.editTextState);
        editTextCity = view.findViewById(R.id.editTextCity);
        apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {

            }

            @Override
            public void taskEnd(String type, String response) {
                if(type.equals("timezone")){
                    //Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject object = new JSONObject(response);

                        timezone=object.getString("timezoneId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        /*editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ((AddRoomActivity) getActivity()).showPlace();
            }
        });*/
        editTextAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddRoomActivity) getActivity()).showPlace1();

            }
        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        timezone=sharedPreferences.getString("roomTimeZone","GMT");
        lat = Double.parseDouble(sharedPreferences.getString("roomLat", "0.0"));
        lng = Double.parseDouble(sharedPreferences.getString("roomLng", "0.0"));
        if (sharedPreferences.getString("roomType", "Select Room Type").equalsIgnoreCase("room")) {

            spinnerRoomType.setSelection(1);
        }
        if (sharedPreferences.getString("roomType", "Select Room Type").equalsIgnoreCase("house")) {
            spinnerRoomType.setSelection(2);
        }
        if (sharedPreferences.getString("roomType", "Select Room Type").equalsIgnoreCase("hotel")) {
            spinnerRoomType.setSelection(3);
        }
        if (sharedPreferences.getString("roomType", "Select Room Type").equalsIgnoreCase("apartment")) {
            spinnerRoomType.setSelection(2);
        }
        if (sharedPreferences.getString("roomAccType", "Select").contains("Entire")) {
            spinnerShared.setSelection(1);
        }
        if (sharedPreferences.getString("roomAccType", "Select").contains("Private")) {
            spinnerShared.setSelection(3);
        }
        if (sharedPreferences.getString("roomAccType", "Select").contains("Shared")) {
            spinnerShared.setSelection(2);
        }
        if (sharedPreferences.getString("bathType", "Private").contains("Private")) {
            spinnerBathroomType.setSelection(0);
        }
        if (sharedPreferences.getString("bathType", "Private").contains("Shared")) {
            spinnerBathroomType.setSelection(1);
        }

        spinnerGuests.setSelection(Integer.valueOf(sharedPreferences.getString("guests", "1")) - 1);

        spinnerNumBath.setSelection(Integer.valueOf(sharedPreferences.getString("bathNum", "1")) - 1);

        spinnerBedrooms.setSelection(Integer.valueOf(sharedPreferences.getString("bedrooms", "1")) - 1);
        editTextAddress.setText(sharedPreferences.getString("roomAddress", ""));
        editTextApt.setText(sharedPreferences.getString("apt", ""));
        editTextCity.setText(sharedPreferences.getString("roomCity", ""));
        editTextState.setText(sharedPreferences.getString("roomState", ""));
        editTextCountry.setText(sharedPreferences.getString("roomCountry", ""));
        editTextZip.setText(sharedPreferences.getString("roomZip", ""));


        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                JSONArray jsonArray = new JSONArray();
                try {
                    JSONArray arrayBedrooms = new JSONArray(sharedPreferences.getString("roomBedrooms1", "[]"));
                    APPHelper.showLog("bedroom", arrayBedrooms.toString());
                    APPHelper.showLog("bedroom", "" + (spinnerBedrooms.getSelectedItemPosition() + 1));
                    if (arrayBedrooms.length() == 0) {
                        APPHelper.showLog("bedroom", "0" + arrayBedrooms.length());
                        for (int i = 0; i < spinnerBedrooms.getSelectedItemPosition() + 1; i++) {

                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("bedroom", "Bedroom " + (i + 1));
                            jsonObject.put("king", 0);
                            jsonObject.put("queen", 0);
                            jsonObject.put("singlebed", 0);
                            jsonObject.put("doublebed", 0);
                            jsonArray.put(jsonObject);
                        }
                    } else if (arrayBedrooms.length() == spinnerBedrooms.getSelectedItemPosition() + 1) {
                        APPHelper.showLog("bedroom", "equal" + arrayBedrooms.length());
                        for (int i = 0; i < spinnerBedrooms.getSelectedItemPosition() + 1; i++) {

                            JSONObject jsonObject = new JSONObject();
                            JSONObject object = arrayBedrooms.getJSONObject(i);
                            jsonObject.put("bedroom", "Bedroom " + (i + 1));
                            jsonObject.put("king", object.getInt("king"));
                            jsonObject.put("queen", object.getInt("queen"));
                            jsonObject.put("singlebed", object.getInt("singlebed"));
                            jsonObject.put("doublebed", object.getInt("doublebed"));
                            jsonArray.put(jsonObject);
                        }
                    } else if (arrayBedrooms.length() < spinnerBedrooms.getSelectedItemPosition() + 1) {
                        APPHelper.showLog("bedroom", "more" + arrayBedrooms.length());
                        for (int i = 0; i < arrayBedrooms.length(); i++) {

                            JSONObject jsonObject = new JSONObject();
                            JSONObject object = arrayBedrooms.getJSONObject(i);
                            jsonObject.put("bedroom", "Bedroom " + (i + 1));
                            jsonObject.put("king", object.getInt("king"));
                            jsonObject.put("queen", object.getInt("queen"));
                            jsonObject.put("singlebed", object.getInt("singlebed"));
                            jsonObject.put("doublebed", object.getInt("doublebed"));
                            jsonArray.put(jsonObject);
                        }
                        for (int i = arrayBedrooms.length(); i < spinnerBedrooms.getSelectedItemPosition() + 1; i++) {

                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("bedroom", "Bedroom " + (i + 1));
                            jsonObject.put("king", 0);
                            jsonObject.put("queen", 0);
                            jsonObject.put("singlebed", 0);
                            jsonObject.put("doublebed", 0);
                            jsonArray.put(jsonObject);
                        }
                    } else if (arrayBedrooms.length() > spinnerBedrooms.getSelectedItemPosition() + 1) {
                        APPHelper.showLog("bedroom", "less" + arrayBedrooms.length());
                        for (int i = 0; i < spinnerBedrooms.getSelectedItemPosition() + 1; i++) {

                            JSONObject jsonObject = new JSONObject();
                            JSONObject object = arrayBedrooms.getJSONObject(i);
                            jsonObject.put("bedroom", "Bedroom " + (i + 1));
                            jsonObject.put("king", object.getInt("king"));
                            jsonObject.put("queen", object.getInt("queen"));
                            jsonObject.put("singlebed", object.getInt("singlebed"));
                            jsonObject.put("doublebed", object.getInt("doublebed"));
                            jsonArray.put(jsonObject);
                        }
                    }

                    if (spinnerRoomType.getSelectedItemPosition() == 0 || spinnerShared.getSelectedItemPosition() == 0
                    ) {
                        APPHelper.showToast(getContext(), "Select room type & what will guests have");
                    } else if (editTextZip.getText().toString().trim().length() == 0
                            || editTextCity.getText().toString().trim().length() == 0
                            || editTextState.getText().toString().trim().length() == 0) {
                        APPHelper.showToast(getContext(), "Enter valid address");

                    } else {
                        editor.putString("roomTimeZone",timezone);
                        editor.putString("roomType", spinnerRoomType.getSelectedItem().toString());
                        editor.putString("roomAccType", spinnerShared.getSelectedItem().toString());
                        editor.putString("guests", String.valueOf(spinnerGuests.getSelectedItemPosition() + 1));
                        editor.putString("bedrooms", String.valueOf(spinnerBedrooms.getSelectedItemPosition()));
                        editor.putString("bathType", spinnerBathroomType.getSelectedItem().toString());
                        editor.putString("bathNum", spinnerNumBath.getSelectedItem().toString());
                        editor.putString("roomAddress", editTextAddress.getText().toString());
                        editor.putString("roomZip", editTextZip.getText().toString());
                        editor.putString("roomCity", editTextCity.getText().toString());
                        editor.putString("roomState", editTextState.getText().toString());
                        editor.putString("roomCountry", editTextCountry.getText().toString());
                        editor.putString("apt", editTextApt.getText().toString());
                        editor.putString("roomBedrooms", jsonArray.toString());
                        editor.putString("roomLat", String.valueOf(lat));
                        editor.putString("roomLng", String.valueOf(lng));
                        editor.apply();
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, AddRoomBedroomFragment.newInstance()).addToBackStack(null).commit();
                    }


                } catch (Exception e) {
                    APPHelper.showLog("RoomE", e.getMessage());
                }
            }
        });
    }



    void setAddress1(String address1, double lat1, double lng1) {

        editTextAddress.setText(address1);
        List<Address> addresses = null;
        lat = lat1;
        lng = lng1;

        GetRequestAsyncTask getRequestAsyncTask = new GetRequestAsyncTask(getContext(), apiCallbacks);
        getRequestAsyncTask.setType("timezone");
        Log.i("Timezone",""+APPHelper.getTimeZoneUrl(lat1, lng1));
        getRequestAsyncTask.execute(APPHelper.getTimeZoneUrl(lat1, lng1));

        //APPHelper.showToast(getContext(),APPHelper.getTimezone(lat, lng));
        try {
            addresses = geocoder.getFromLocation(
                    lat1, lng1,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e("Location", ioException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.

        }

        postal = "";
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {

            postal = "";
            state = "";
            city = "";
            country = "";

        } else {

            postal = "";
            Address address = addresses.get(0);
            Log.i("Address", address.toString());
            ArrayList<String> addressFragments = new ArrayList<String>();
            postal = address.getPostalCode();
            country = address.getCountryName();
            state = address.getAdminArea();
            city = address.getLocality();
            editTextCity.setText(city);
            editTextState.setText(state);
            editTextCountry.setText(country);
            editTextZip.setText(postal);
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.


        }
    }
}
