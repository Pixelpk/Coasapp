package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GuestsAdapter extends RecyclerView.Adapter<GuestsAdapter.VH> {

    ArrayList<JSONObject> guests;
    Activity activity;
    Context context;

    public GuestsAdapter(ArrayList<JSONObject> guests, Activity activity, Context context) {
        this.guests = guests;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_guests, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        JSONObject jsonObject = guests.get(position);
        try {
            holder.textViewPhone.setText(jsonObject.getString("guest_phone"));
            holder.textViewName.setText(jsonObject.getString("guest_name") + " " + jsonObject.getString("guest_middle_name") + " " + jsonObject.getString("guest_last_name"));
            if (Integer.valueOf(jsonObject.getString("guest_age")) > 0)
                holder.textViewAge.setText(jsonObject.getString("guest_age") + "years");
            else holder.textViewAge.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return guests.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView textViewName, textViewPhone, textViewAge;

        public VH(View itemView) {
            super(itemView);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
        }
    }
}
