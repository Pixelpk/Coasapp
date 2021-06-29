package com.coasapp.coas.roombook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.ViewHolder> {

    ArrayList<JSONObject> arrayList;
    OnSwitchChanged onSwitchChanged;

    public FacilitiesAdapter(ArrayList<JSONObject> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_facilities, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            final JSONObject object = arrayList.get(position);
            holder.aSwitch.setText(object.getString("amenity"));
            if (object.getBoolean("available")) {
                APPHelper.showLog("Amenity","1");
                holder.aSwitch.setChecked(true);
                onSwitchChanged.onSwitchChanged(holder.getAdapterPosition(), true);

            } else {
                holder.aSwitch.setChecked(false);
                onSwitchChanged.onSwitchChanged(holder.getAdapterPosition(), false);

            }
            holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    onSwitchChanged.onSwitchChanged(holder.getAdapterPosition(), isChecked);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Switch aSwitch;

        public ViewHolder(View itemView) {
            super(itemView);
            aSwitch = itemView.findViewById(R.id.switch1);
        }
    }

    public interface OnSwitchChanged {
        void onSwitchChanged(int position, boolean isChecked);
    }

    public void setOnSwitchChanged(OnSwitchChanged onSwitchChanged) {
        this.onSwitchChanged = onSwitchChanged;
    }
}
