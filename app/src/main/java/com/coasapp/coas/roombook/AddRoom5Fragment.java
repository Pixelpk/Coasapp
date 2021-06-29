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
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoom5Fragment extends Fragment implements APPConstants {

    String smoking = "no", children = "no", infants = "no", pets = "no", weapons = "no", surveillance = "no", events = "no", limits = "no", parties = "no";

    public AddRoom5Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room5, container, false);
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

        Switch switchWeapons = view.findViewById(R.id.switchWeapons);
        Switch switchSurvey = view.findViewById(R.id.switchSurveilance);
        Switch switchInfants = view.findViewById(R.id.switchInfants);
        Switch switchLimit = view.findViewById(R.id.switchAmenityLimit);
        final EditText editTextRules = view.findViewById(R.id.editTextHouseRules);
        final EditText editTextTerms = view.findViewById(R.id.editTextTerms);


        switchParties.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    parties = "yes";
                else
                    parties = "no";
            }
        });

        switchPets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    pets = "yes";
                else
                    pets = "no";
            }
        });
        switchEvents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    events = "yes";
                else
                    events = "no";
            }
        });
        switchLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    limits = "yes";
                else
                    limits = "no";
            }
        });
        switchWeapons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    weapons = "yes";
                else
                    weapons = "no";
            }
        });
        switchSurvey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    surveillance = "yes";
                else
                    surveillance = "no";
            }
        });

        switchChildren.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    children = "yes";
                else
                    children = "no";
            }
        });
        switchInfants.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    infants = "yes";
                else
                    infants = "no";
            }
        });
        switchSmoking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    smoking = "yes";
                else
                    smoking = "no";
            }
        });

        editTextRules.setText(sharedPreferences.getString("rules", ""));
        editTextTerms.setText(sharedPreferences.getString("terms", ""));

        if (sharedPreferences.getString("parties", "no").equalsIgnoreCase("yes")) {
            switchParties.setChecked(true);
        }
        if (sharedPreferences.getString("events", "no").equalsIgnoreCase("yes")) {
            switchEvents.setChecked(true);
        }
        if (sharedPreferences.getString("surveillance", "no").equalsIgnoreCase("yes")) {
            switchSurvey.setChecked(true);
        }
        if (sharedPreferences.getString("limits", "no").equalsIgnoreCase("yes")) {
            switchLimit.setChecked(true);
        }
        if (sharedPreferences.getString("smoking", "no").equalsIgnoreCase("yes")) {
            switchSmoking.setChecked(true);
        }
        if (sharedPreferences.getString("children", "no").equalsIgnoreCase("yes")) {
            switchChildren.setChecked(true);
        }
        if (sharedPreferences.getString("infants", "no").equalsIgnoreCase("yes")) {
            switchInfants.setChecked(true);
        }
        if (sharedPreferences.getString("pets", "no").equalsIgnoreCase("yes")) {
            switchPets.setChecked(true);
        }
        if (sharedPreferences.getString("weapons", "no").equalsIgnoreCase("yes")) {
            switchWeapons.setChecked(true);
        }
        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = null;
                if (sharedPreferences.getString("mode", "add").equalsIgnoreCase("edit")) {
                    fragment = new EditRoomFragment();
                } else {
                    fragment = new AddRoom6Fragment();
                }
                if (editTextRules.getText().toString().trim().equals("") || editTextTerms.getText().toString().trim().equals("")) {
                    APPHelper.showToast(getContext(), "Enter terms & conditions");
                } else {
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
                    editor.putString("terms", editTextTerms.getText().toString().trim());
                    editor.apply();
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).addToBackStack(null).commit();
                }

            }
        });
    }
}