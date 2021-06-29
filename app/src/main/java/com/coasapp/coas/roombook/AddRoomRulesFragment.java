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
import android.widget.EditText;
import android.widget.Switch;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomRulesFragment extends Fragment implements APPConstants {

    String smoking = "No", children = "No", infants = "No", pets = "No", weapons = "No", surveillance = "No", events = "No", limits = "No", parties = "No";

    public AddRoomRulesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        Switch switchPets = view.findViewById(R.id.switchPets);
        Switch switchParties = view.findViewById(R.id.switchDangerAnimal);
        Switch switchEvents = view.findViewById(R.id.switchNoise);
        Switch switchSmoking = view.findViewById(R.id.switchClimb);
        Switch switchChildren = view.findViewById(R.id.switchChildren);
        final EditText editTextRules = view.findViewById(R.id.editTextHouseRules);
        switchParties.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    parties = "Yes";
                else
                    parties = "No";
            }
        });

        switchPets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    pets = "Yes";
                else
                    pets = "No";
            }
        });
        switchEvents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    events = "Yes";
                else
                    events = "No";
            }
        });
        switchChildren.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    children = "Yes";
                else
                    children = "No";
            }
        });

        switchSmoking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    smoking = "Yes";
                else
                    smoking = "No";
            }
        });
        editTextRules.setText(sharedPreferences.getString("rules", ""));

        if (sharedPreferences.getString("parties", "No").equalsIgnoreCase("Yes")) {
            switchParties.setChecked(true);
        }
        if (sharedPreferences.getString("events", "No").equalsIgnoreCase("Yes")) {
            switchEvents.setChecked(true);
        }

        if (sharedPreferences.getString("smoking", "No").equalsIgnoreCase("Yes")) {
            switchSmoking.setChecked(true);
        }
        if (sharedPreferences.getString("children", "No").equalsIgnoreCase("Yes")) {
            switchChildren.setChecked(true);
        }

        if (sharedPreferences.getString("pets", "No").equalsIgnoreCase("Yes")) {
            switchPets.setChecked(true);
        }

        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new AddRoomVisitorRequirementsFragment();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("parties", parties);
                editor.putString("events", events);
                editor.putString("surveillance", surveillance);
                editor.putString("limits", limits);
                editor.putString("weapons", weapons);
                editor.putString("smoking", smoking);
                editor.putString("children", children);
                editor.putString("infants", infants);
                editor.putString("pets", pets);
                editor.putString("rules", editTextRules.getText().toString().trim());
                editor.apply();
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).addToBackStack(null).commit();
            }
        });

    }
}
