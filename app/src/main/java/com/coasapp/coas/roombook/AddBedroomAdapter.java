package com.coasapp.coas.roombook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddBedroomAdapter extends RecyclerView.Adapter<AddBedroomAdapter.MyViewHolder> {

    ArrayList<JSONObject> arrayList;
    OnAddClick onAddClick;

    public AddBedroomAdapter(ArrayList<JSONObject> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_add_beds, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject object = arrayList.get(position);
        try {

            Map<String,String> map=new HashMap<>();

            holder.textViewBedroom.setText(object.getString("bedroom"));
            String beds = "";

            if (object.getInt("singlebed") > 0) {
                beds = beds + " Single " + object.getInt("singlebed");
            }
            if (object.getInt("doublebed") > 0) {
                beds = beds + " Double " + object.getInt("doublebed");
            }
            if (object.getInt("king") > 0) {
                beds = beds + " King " + object.getInt("king");
            }
            if (object.getInt("queen") > 0) {
                beds = beds + " Queen " + object.getInt("queen");
            }
            holder.textViewBedCount.setText(beds.trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnAddClick {
        void onAddClick(int position);
    }

    public void setOnAddClick(OnAddClick onAddClick) {
        this.onAddClick = onAddClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewBedroom, textViewBedCount;
        Button buttonAddBed;
        Spinner spinnerSingle, spinnerDouble, spinnerKing, spinnerQueen;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewBedCount = itemView.findViewById(R.id.textViewBedCount);
            textViewBedroom = itemView.findViewById(R.id.textViewBedroom);
            buttonAddBed = itemView.findViewById(R.id.buttonAddBed);
            spinnerDouble = itemView.findViewById(R.id.spinnerDouble);
            spinnerSingle = itemView.findViewById(R.id.spinnerSingle);
            spinnerKing = itemView.findViewById(R.id.spinnerKing);
            spinnerQueen = itemView.findViewById(R.id.spinnerQueen);

            buttonAddBed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddClick.onAddClick(getAdapterPosition());
                }
            });
        }
    }
}
