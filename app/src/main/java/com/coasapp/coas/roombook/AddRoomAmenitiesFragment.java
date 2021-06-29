package com.coasapp.coas.roombook;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomAmenitiesFragment extends Fragment implements APPConstants {

    ArrayList<JSONObject> arrayList = new ArrayList<>();
    ArrayList<JSONObject> arrayList2 = new ArrayList<>();
    FacilitiesAdapter facilitiesAdapter;
    LinearLayout layoutProgress;

    public AddRoomAmenitiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        RecyclerView recyclerViewFacilities = view.findViewById(R.id.recyclerViewFacilities);
        facilitiesAdapter = new FacilitiesAdapter(arrayList);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        recyclerViewFacilities.setAdapter(facilitiesAdapter);
        new GetAmenities().execute();
        facilitiesAdapter.setOnSwitchChanged(new FacilitiesAdapter.OnSwitchChanged() {
            @Override
            public void onSwitchChanged(int position, boolean checked) {
                if (checked) {
                    //arrayList2.add(arrayList.get(position));
                    try {
                        arrayList.get(position).put("available", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        arrayList.get(position).put("available", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //arrayList2.remove(arrayList.get(position));
                }
            }
        });
        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray array = new JSONArray();
                arrayList2.clear();
                for (int i = 0; i < arrayList.size(); i++) {
                    try {
                        if (arrayList.get(i).getBoolean("available")) {
                            arrayList2.add(arrayList.get(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < arrayList2.size(); i++) {
                    array.put(arrayList2.get(i));
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("roomAmenities", array.toString());
                APPHelper.showLog("Amenities", array.toString());
                editor.apply();
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new AddRoomTitleImageFragment()).addToBackStack(null).commit();
            }
        });
    }

    class GetAmenities extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new RequestHandler().sendGetRequest(MAIN_URL + "get_amenities.php");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                String jsonArray = sharedPreferences.getString("roomAmenities", "[]");
                APPHelper.showLog("Amenities", jsonArray);
                JSONArray array = new JSONArray(s);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    APPHelper.showLog("AmenitiesID", "\"amenity_id\":\"" + object.getString("amenity_id") + "\"");

                    if (jsonArray.contains("\"amenity_id\":\"" + object.getString("amenity_id") + "\"")) {
                        APPHelper.showLog("AmenitiesID", object.getString("amenity_id"));
                        object.put("available", true);

                    } else {
                        object.put("available", false);
                    }
                    arrayList.add(object);
                    facilitiesAdapter.notifyDataSetChanged();
                }

                facilitiesAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
