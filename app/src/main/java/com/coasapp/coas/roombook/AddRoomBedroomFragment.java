package com.coasapp.coas.roombook;


import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRoomBedroomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRoomBedroomFragment extends Fragment implements APPConstants {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    AddBedroomAdapter addBedroomAdapter;
    ArrayList<JSONObject> arrayList = new ArrayList<>();

    public AddRoomBedroomFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AddRoomBedroomFragment newInstance() {
        AddRoomBedroomFragment fragment = new AddRoomBedroomFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        RecyclerView recyclerViewBedrooms = view.findViewById(R.id.recyclerViewBedrooms);
        Button button = view.findViewById(R.id.buttonNext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                ArrayList<JSONObject> arrayList1 = new ArrayList<>();
                JSONArray array = new JSONArray();
                for (int i = 0; i < arrayList.size(); i++) {
                    array.put(arrayList.get(i));
                }
                editor.putString("roomBedrooms", array.toString());
                editor.apply();
                APPHelper.showLog("roomBedrooms", array.toString());
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content, new AddRoomAmenitiesFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        addBedroomAdapter = new AddBedroomAdapter(arrayList);
        recyclerViewBedrooms.setAdapter(addBedroomAdapter);
        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString("roomBedrooms", "[]"));
            APPHelper.showLog("roomBedrooms", sharedPreferences.getString("roomBedrooms", "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.getJSONObject(i));
            }
            addBedroomAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        addBedroomAdapter.setOnAddClick(new AddBedroomAdapter.OnAddClick() {
            @Override
            public void onAddClick(int position) {
                JSONObject object = arrayList.get(position);
                showBedAlert(position);
            }
        });
    }

    void showBedAlert(final int position) {
        final JSONObject object = arrayList.get(position);

        LayoutInflater li = LayoutInflater.from(getActivity());
        //Creating a view to get the dialog box
        View view = li.inflate(R.layout.dialog_add_beds, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Adding our dialog box to the view of alert dialog
        builder.setView(view);


        //Creating an alert dialog
        final AlertDialog alertDialog = builder.create();
        //Displaying the alert dialog
        alertDialog.show();

        final Spinner spinnerSingle = view.findViewById(R.id.spinnerSingle);
        final Spinner spinnerQueen = view.findViewById(R.id.spinnerQueen);
        final Spinner spinnerKing = view.findViewById(R.id.spinnerKing);
        final Spinner spinnerDouble = view.findViewById(R.id.spinnerDouble);
        try {
            spinnerKing.setSelection(object.getInt("king"));
            spinnerQueen.setSelection(object.getInt("queen"));
            spinnerSingle.setSelection(object.getInt("singlebed"));
            spinnerDouble.setSelection(object.getInt("doublebed"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Button buttonAdd = view.findViewById(R.id.buttonOK);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    object.put("king", spinnerKing.getSelectedItemPosition());
                    object.put("queen", spinnerQueen.getSelectedItemPosition());
                    object.put("singlebed", spinnerSingle.getSelectedItemPosition());
                    object.put("doublebed", spinnerDouble.getSelectedItemPosition());
                    addBedroomAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                alertDialog.dismiss();
            }
        });
    }

}
