package com.coasapp.coas.bargain;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BargainVehiclesFragment extends Fragment {

    VehicleListAdapter vehicleListAdapter;
    ArrayList<JSONObject> arrayListVehicles1 = new ArrayList<>();
    ArrayList<JSONObject> arrayListVehicles = new ArrayList<>();


    public BargainVehiclesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bargain_veh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGrid);
        vehicleListAdapter = new VehicleListAdapter(arrayListVehicles1, getActivity(), getContext());
        recyclerView.setAdapter(vehicleListAdapter);

        try {
            JSONObject object = new JSONObject();
            object.put("vehicle_name", "Innova");
            object.put("vehicle_brand", "Toyota");
            object.put("vehicle_price", "20000");
            object.put("vehicle_image", "https://cdn.drivemag.net/media/default/0001/79/CUPRA-e-Racer-001H-1846-8583-default-large.jpeg");
            object.put("name", "test");
            object.put("image", "http://easemypay.in/coas/profile_pictures/user.png");
            arrayListVehicles1.add(object);
            object = new JSONObject();
            object.put("vehicle_name", "Innova");
            object.put("vehicle_brand", "Toyota");
            object.put("vehicle_price", "20000");
            object.put("vehicle_image", "https://cdn.drivemag.net/media/default/0001/79/CUPRA-e-Racer-001H-1846-8583-default-large.jpeg");
            object.put("name", "test");
            object.put("image", "http://easemypay.in/coas/profile_pictures/user.png");
            arrayListVehicles1.add(object);
            vehicleListAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
