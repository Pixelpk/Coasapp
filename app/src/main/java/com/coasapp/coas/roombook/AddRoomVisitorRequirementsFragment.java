package com.coasapp.coas.roombook;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomVisitorRequirementsFragment extends Fragment implements APPConstants {


    String govtId = "No";

    public AddRoomVisitorRequirementsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);

        Switch switchGov = view.findViewById(R.id.switchGovtId);

        switchGov.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    govtId = "Yes";
                } else {
                    govtId = "No";
                }
            }
        });
        govtId = sharedPreferences.getString("govt_req","X");
        APPHelper.showLog("checkout",govtId);
        if(govtId.equalsIgnoreCase("Yes")){
            switchGov.setChecked(true);
        }
        else {
            switchGov.setChecked(false);
        }
        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putString("govt_req",govtId).apply();
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new AddRoomMoreFragment()).addToBackStack(null).commit();

            }
        });
        if (sharedPreferences.getString("govt", "no").equalsIgnoreCase("yes")) {
            switchGov.setChecked(true);
        }
    }

}
