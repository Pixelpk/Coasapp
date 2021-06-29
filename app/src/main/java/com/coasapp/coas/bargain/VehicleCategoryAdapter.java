package com.coasapp.coas.bargain;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VehicleCategoryAdapter extends RecyclerView.Adapter<VehicleCategoryAdapter.MyViewHolder> {

    ArrayList<JSONObject> arrayList;
    Activity activity;
    Context context;
    int index = 0;

    public VehicleCategoryAdapter(ArrayList<JSONObject> arrayList, Activity activity, Context context) {
        this.arrayList = arrayList;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_vehicle_type, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        JSONObject jsonObject = arrayList.get(position);
        try {
            holder.textViewVehicleName.setText(jsonObject.getString("veh_cat"));
            APPHelper.showLog("VehCat", jsonObject.getString("veh_cat"));
            holder.imageViewVehicle.setImageResource(jsonObject.getInt("image"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onItemClick(holder.getAdapterPosition());
                    index = holder.getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
            if (index == holder.getAdapterPosition()) {
                holder.layoutVeh.setBackgroundColor(Color.RED);
            } else {
                holder.layoutVeh.setBackgroundColor(ContextCompat.getColor(context,R.color.white));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    OnItemClick onItemClick;

    public interface OnItemClick {
        void onItemClick(int i);
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewVehicleName, textViewVehiclePrice, textViewFrom, textViewTo, textViewAmt, textViewOrderId;
        ImageView imageViewVehicle;
        LinearLayout layoutVeh;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewVehicleName = itemView.findViewById(R.id.textViewVehicleCat);
            imageViewVehicle = itemView.findViewById(R.id.imageViewVehicleCat);
            layoutVeh = itemView.findViewById(R.id.layoutVehCat);
        }
    }
}
